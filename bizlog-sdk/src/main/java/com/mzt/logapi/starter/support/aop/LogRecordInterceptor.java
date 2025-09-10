package com.mzt.logapi.starter.support.aop;

import com.mzt.logapi.beans.CodeVariableType;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.beans.MethodExecuteResult;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.IFunctionService;
import com.mzt.logapi.service.ILogRecordPerformanceMonitor;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.service.IOperatorGetService;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.support.parse.LogFunctionParser;
import com.mzt.logapi.starter.support.parse.LogRecordValueParser;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mzt.logapi.service.ILogRecordPerformanceMonitor.*;

/**
 * DATE 5:39 PM
 *
 * @author mzt.
 */
@Slf4j
public class LogRecordInterceptor extends LogRecordValueParser implements MethodInterceptor,
    Serializable, SmartInitializingSingleton {

  private LogRecordOperationSource logRecordOperationSource;

  private String tenantId;

  private ILogRecordService bizLogService;

  private IOperatorGetService operatorGetService;

  private ILogRecordPerformanceMonitor logRecordPerformanceMonitor;

  private boolean joinTransaction;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Method method = invocation.getMethod();
    return execute(invocation, invocation.getThis(), method, invocation.getArguments());
  }

  private Object execute(MethodInvocation invoker, Object target, Method method, Object[] args)
      throws Throwable {
    if (AopUtils.isAopProxy(target)) {
      return invoker.proceed();
    }
    StopWatch stopWatch = new StopWatch(MONITOR_NAME);
    stopWatch.start(MONITOR_TASK_BEFORE_EXECUTE);
    Class<?> targetClass = getTargetClass(target);
    Object ret = null;
    MethodExecuteResult methodExecuteResult = new MethodExecuteResult(method, args, targetClass);

    // 提前获取操作者信息
    String preFetchedOperatorId = null;
    try {
      if (operatorGetService != null) {
        preFetchedOperatorId = operatorGetService.getUser().getOperatorId();
      }
    } catch (Exception e) {
      log.warn("Failed to pre-fetch operator info", e);
    }

    Collection<LogRecordOps> operations = new ArrayList<>();
    Map<String, String> functionNameAndReturnMap = new HashMap<>();

    try {
      operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
      List<String> spElTemplates = getBeforeExecuteFunctionTemplate(operations);
      functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(spElTemplates, targetClass,
          method, args);

      // 将预取的操作者信息添加到解析后的map中
      if (preFetchedOperatorId != null) {
        functionNameAndReturnMap.put("PRE_FETCHED_OPERATOR_ID", preFetchedOperatorId);
      }
    } catch (Exception e) {
      log.error("log record parse before function exception", e);
    } finally {
      stopWatch.stop();
    }

    // 在方法执行前创建空的上下文
    LogRecordContext.putEmptySpan();

    try {
      ret = invoker.proceed();
      methodExecuteResult.setResult(ret);
      methodExecuteResult.setSuccess(true);

      // 处理异步返回值
      if (ret instanceof Mono) {
        // 在方法执行完成后捕获上下文
        Map<String, Object> capturedContext = captureContext();
        return handleMonoResult((Mono<?>) ret, methodExecuteResult, functionNameAndReturnMap,
            operations, stopWatch, capturedContext);
      } else if (ret instanceof Flux) {
        // 在方法执行完成后捕获上下文
        Map<String, Object> capturedContext = captureContext();
        return handleFluxResult((Flux<?>) ret, methodExecuteResult, functionNameAndReturnMap,
            operations, stopWatch, capturedContext);
      } else if (ret instanceof CompletionStage || ret instanceof CompletableFuture) {
        // 在方法执行完成后捕获上下文
        Map<String, Object> capturedContext = captureContext();
        return handleFutureResult((CompletionStage<?>) ret, methodExecuteResult,
            functionNameAndReturnMap, operations, stopWatch, capturedContext);
      }
    } catch (Exception e) {
      methodExecuteResult.setSuccess(false);
      methodExecuteResult.setThrowable(e);
      methodExecuteResult.setErrorMsg(e.getMessage());
    }

    // 同步执行或非异步类型
    stopWatch.start(MONITOR_TASK_AFTER_EXECUTE);
    recordLogAndClean(methodExecuteResult, functionNameAndReturnMap, operations, stopWatch);

    if (methodExecuteResult.getThrowable() != null) {
      throw methodExecuteResult.getThrowable();
    }
    return ret;
  }

  // 捕获当前线程的所有上下文信息
  private Map<String, Object> captureContext() {
    Map<String, Object> context = new HashMap<>();
    try {
      // 获取当前栈顶的变量
      Map<String, Object> variables = LogRecordContext.getVariables();
      if (variables != null) {
        context.putAll(variables);
      }
    } catch (Exception e) {
      log.warn("Failed to capture context", e);
    }
    return context;
  }

  // 恢复上下文信息
  private void restoreContext(Map<String, Object> context) {
    try {
      if (LogRecordContext.getVariables() != null && LogRecordContext.getVariables().isEmpty()) {
        // 清除当前上下文
        LogRecordContext.clear();
        // 创建一个新的空上下文
        LogRecordContext.putEmptySpan();
      }
      // 恢复所有上下文变量
      if (context != null && !context.isEmpty()) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
          LogRecordContext.putVariable(entry.getKey(), entry.getValue());
        }
      }
    } catch (Exception e) {
      log.warn("Failed to restore context", e);
    }
  }

  private Mono<?> handleMonoResult(Mono<?> mono, MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap,
      Collection<LogRecordOps> operations, StopWatch stopWatch,
      Map<String, Object> capturedContext) {
    return mono.doOnSuccess(result -> {
          restoreContext(capturedContext);
          methodExecuteResult.setResult(result);
          methodExecuteResult.setSuccess(true);
          if (stopWatch.isRunning()) {
            stopWatch.stop();
          }
          stopWatch.start(MONITOR_TASK_AFTER_EXECUTE);
          recordLogAndClean(methodExecuteResult, functionNameAndReturnMap, operations, stopWatch);
        })
        .doOnError(error -> {
          restoreContext(capturedContext);
          methodExecuteResult.setSuccess(false);
          methodExecuteResult.setThrowable(error);
          methodExecuteResult.setErrorMsg(error.getMessage());
          if (stopWatch.isRunning()) {
            stopWatch.stop();
          }
          stopWatch.start(MONITOR_TASK_AFTER_EXECUTE);
          recordLogAndClean(methodExecuteResult, functionNameAndReturnMap, operations, stopWatch);
        });
  }

  private Flux<?> handleFluxResult(Flux<?> flux, MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap,
      Collection<LogRecordOps> operations, StopWatch stopWatch,
      Map<String, Object> capturedContext) {
    return flux.doOnComplete(() -> {
          restoreContext(capturedContext);
          methodExecuteResult.setSuccess(true);
          if (stopWatch.isRunning()) {
            stopWatch.stop();
          }
          stopWatch.start(MONITOR_TASK_AFTER_EXECUTE);
          recordLogAndClean(methodExecuteResult, functionNameAndReturnMap, operations, stopWatch);
        })
        .doOnError(error -> {
          restoreContext(capturedContext);
          methodExecuteResult.setSuccess(false);
          methodExecuteResult.setThrowable(error);
          methodExecuteResult.setErrorMsg(error.getMessage());
          if (stopWatch.isRunning()) {
            stopWatch.stop();
          }
          stopWatch.start(MONITOR_TASK_AFTER_EXECUTE);
          recordLogAndClean(methodExecuteResult, functionNameAndReturnMap, operations, stopWatch);
        });
  }

  private CompletionStage<?> handleFutureResult(CompletionStage<?> future,
      MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap,
      Collection<LogRecordOps> operations, StopWatch stopWatch,
      Map<String, Object> capturedContext) {
    return future.whenComplete((result, error) -> {
      restoreContext(capturedContext);
      if (error != null) {
        methodExecuteResult.setSuccess(false);
        methodExecuteResult.setThrowable(error);
        methodExecuteResult.setErrorMsg(error.getMessage());
      } else {
        methodExecuteResult.setResult(result);
        methodExecuteResult.setSuccess(true);
      }
      if (stopWatch.isRunning()) {
        stopWatch.stop();
      }
      stopWatch.start(MONITOR_TASK_AFTER_EXECUTE);
      recordLogAndClean(methodExecuteResult, functionNameAndReturnMap, operations, stopWatch);
    });
  }

  private void recordLogAndClean(MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap,
      Collection<LogRecordOps> operations,
      StopWatch stopWatch) {
    try {
      if (!CollectionUtils.isEmpty(operations)) {
        recordExecute(methodExecuteResult, functionNameAndReturnMap, operations);
      }
    } catch (Exception t) {
      log.error("log record parse exception", t);
      if (joinTransaction) {
        throw t;
      }
    } finally {
      LogRecordContext.clear();
      stopWatch.stop();
      try {
        if (logRecordPerformanceMonitor != null) {
          logRecordPerformanceMonitor.print(stopWatch);
        }
      } catch (Exception e) {
        log.error("execute exception", e);
      }
    }
  }

  private List<String> getBeforeExecuteFunctionTemplate(Collection<LogRecordOps> operations) {
    List<String> spElTemplates = new ArrayList<>();
    for (LogRecordOps operation : operations) {
      //执行之前的函数，失败模版不解析
      List<String> templates = getSpElTemplates(operation, operation.getSuccessLogTemplate());
      if (!CollectionUtils.isEmpty(templates)) {
        spElTemplates.addAll(templates);
      }
    }
    return spElTemplates;
  }

  private void recordExecute(MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap,
      Collection<LogRecordOps> operations) {
    for (LogRecordOps operation : operations) {
      try {
        if (StringUtils.isEmpty(operation.getSuccessLogTemplate())
            && StringUtils.isEmpty(operation.getFailLogTemplate())) {
          continue;
        }
        if (exitsCondition(methodExecuteResult, functionNameAndReturnMap, operation)) {
          continue;
        }
        if (!methodExecuteResult.isSuccess()) {
          failRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
        } else {
          successRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
        }
      } catch (Exception t) {
        log.error("log record execute exception", t);
        if (joinTransaction) {
          throw t;
        }
      }
    }
  }

  private void successRecordExecute(MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap,
      LogRecordOps operation) {
    // 若存在 isSuccess 条件模版，解析出成功/失败的模版
    String action = "";
    boolean flag = true;
    if (!StringUtils.isEmpty(operation.getIsSuccess())) {
      String condition = singleProcessTemplate(methodExecuteResult, operation.getIsSuccess(),
          functionNameAndReturnMap);
      if (StringUtils.endsWithIgnoreCase(condition, "true")) {
        action = operation.getSuccessLogTemplate();
      } else {
        action = operation.getFailLogTemplate();
        flag = false;
      }
    } else {
      action = operation.getSuccessLogTemplate();
    }
    if (StringUtils.isEmpty(action)) {
      // 没有日志内容则忽略
      return;
    }
    List<String> spElTemplates = getSpElTemplates(operation, action);
    String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates,
        functionNameAndReturnMap);
    Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult,
        functionNameAndReturnMap);
    saveLog(methodExecuteResult.getMethod(), !flag, operation, operatorIdFromService, action,
        expressionValues);
  }

  private void failRecordExecute(MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap,
      LogRecordOps operation) {
    if (StringUtils.isEmpty(operation.getFailLogTemplate())) {
      return;
    }

    String action = operation.getFailLogTemplate();
    List<String> spElTemplates = getSpElTemplates(operation, action);
    String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates,
        functionNameAndReturnMap);

    Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult,
        functionNameAndReturnMap);
    saveLog(methodExecuteResult.getMethod(), true, operation, operatorIdFromService, action,
        expressionValues);
  }

  private boolean exitsCondition(MethodExecuteResult methodExecuteResult,
      Map<String, String> functionNameAndReturnMap, LogRecordOps operation) {
    if (!StringUtils.isEmpty(operation.getCondition())) {
      String condition = singleProcessTemplate(methodExecuteResult, operation.getCondition(),
          functionNameAndReturnMap);
      if (StringUtils.endsWithIgnoreCase(condition, "false")) {
        return true;
      }
    }
    return false;
  }

  private void saveLog(Method method, boolean flag, LogRecordOps operation,
      String operatorIdFromService,
      String action, Map<String, String> expressionValues) {
    if (StringUtils.isEmpty(expressionValues.get(action)) ||
        (!diffSameWhetherSaveLog && action.contains("#") && Objects.equals(action,
            expressionValues.get(action)))) {
      return;
    }
    LogRecord logRecord = LogRecord.builder()
        .tenant(tenantId)
        .type(expressionValues.get(operation.getType()))
        .bizNo(expressionValues.get(operation.getBizNo()))
        .operator(getRealOperatorId(operation, operatorIdFromService, expressionValues))
        .subType(expressionValues.get(operation.getSubType()))
        .extra(expressionValues.get(operation.getExtra()))
        .codeVariable(getCodeVariable(method))
        .action(expressionValues.get(action))
        .fail(flag)
        .createTime(new Date())
        .build();

    bizLogService.record(logRecord);
  }

  private Map<CodeVariableType, Object> getCodeVariable(Method method) {
    Map<CodeVariableType, Object> map = new HashMap<>();
    map.put(CodeVariableType.ClassName, method.getDeclaringClass());
    map.put(CodeVariableType.MethodName, method.getName());
    return map;
  }

  private List<String> getSpElTemplates(LogRecordOps operation, String... actions) {
    List<String> spElTemplates = new ArrayList<>();
    spElTemplates.add(operation.getType());
    spElTemplates.add(operation.getBizNo());
    spElTemplates.add(operation.getSubType());
    spElTemplates.add(operation.getExtra());
    spElTemplates.addAll(Arrays.asList(actions));
    return spElTemplates;
  }

  private String getRealOperatorId(LogRecordOps operation, String operatorIdFromService,
      Map<String, String> expressionValues) {
    return !StringUtils.isEmpty(operatorIdFromService) ? operatorIdFromService
        : expressionValues.get(operation.getOperatorId());
  }

  private String getOperatorIdFromServiceAndPutTemplate(LogRecordOps operation,
      List<String> spElTemplates, Map<String, String> functionNameAndReturnMap) {
    // 优先使用预取的操作者信息
    String preFetchedOperatorId = functionNameAndReturnMap.get("PRE_FETCHED_OPERATOR_ID");
    if (!StringUtils.isEmpty(preFetchedOperatorId)) {
      return preFetchedOperatorId;
    }

    String realOperatorId = "";
    if (StringUtils.isEmpty(operation.getOperatorId())) {
      realOperatorId = operatorGetService.getUser().getOperatorId();
      if (StringUtils.isEmpty(realOperatorId)) {
        throw new IllegalArgumentException("[LogRecord] operator is null");
      }
    } else {
      spElTemplates.add(operation.getOperatorId());
    }
    return realOperatorId;
  }

  private Class<?> getTargetClass(Object target) {
    return AopProxyUtils.ultimateTargetClass(target);
  }


  public void setLogRecordOperationSource(LogRecordOperationSource logRecordOperationSource) {
    this.logRecordOperationSource = logRecordOperationSource;
  }

  public void setTenant(String tenant) {
    this.tenantId = tenant;
  }

  public void setLogRecordService(ILogRecordService bizLogService) {
    this.bizLogService = bizLogService;
  }

  public void setLogRecordPerformanceMonitor(
      ILogRecordPerformanceMonitor logRecordPerformanceMonitor) {
    this.logRecordPerformanceMonitor = logRecordPerformanceMonitor;
  }

  public void setJoinTransaction(boolean joinTransaction) {
    this.joinTransaction = joinTransaction;
  }

  public void setDiffSameWhetherSaveLog(boolean diffLog) {
    this.diffSameWhetherSaveLog = diffLog;
  }

  @Override
  public void afterSingletonsInstantiated() {
    bizLogService = beanFactory.getBean(ILogRecordService.class);
    operatorGetService = beanFactory.getBean(IOperatorGetService.class);
    this.setLogFunctionParser(new LogFunctionParser(beanFactory.getBean(IFunctionService.class)));
    this.setDiffParseFunction(beanFactory.getBean(DiffParseFunction.class));
  }
}
