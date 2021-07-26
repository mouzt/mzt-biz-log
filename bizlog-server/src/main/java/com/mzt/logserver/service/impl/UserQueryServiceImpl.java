package com.mzt.logserver.service.impl;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecordAnnotation;
import com.mzt.logserver.constants.LogRecordType;
import com.mzt.logserver.service.UserQueryService;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author muzhantong
 * create on 2021/2/10 11:13 上午
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {

    @Override
    @LogRecordAnnotation(success = "获取用户列表,内层方法调用人{{#user}}", prefix = LogRecordType.ORDER, bizNo = "{{#user}}")
    public List<User> getUserList(List<String> userIds) {
        LogRecordContext.putVariable("user", "mzt");
        return null;
    }
//
//    public void modifyAddress(ModifyAddressRequest request){
//
//        //do something
//
//        String template = "用户%s修改了订单的配送地址：从“%s”修改到“%s”";
//
//        logUtil.log(orderNo, String.format(tempalte, "小明", "金灿灿小区", "银盏盏小区"), "小明");
//
//    }
//
//    private OnesIssueDO updateAddress(updateDeliveryRequest request) {
//        DeliveryOrder deliveryOrder = deliveryQueryService.queryOldAddress(request.getDeliveryOrderNo());
//        // 更新派送信息 电话，收件人、地址
//        doUpdate(request);
//        String logContent = getLogContent(request, deliveryOrder);
//        LogUtils.logRecord(request.getOrderNo(), logContent, request.getOperator);
//        return onesIssueDO;
//    }
//
//    private String getLogContent(updateDeliveryRequest request, DeliveryOrder deliveryOrder) {
//
//        String template = "用户%s修改了订单的配送地址：从“%s”修改到“%s”";
//        String.format(tempalte, request.getUserName(), deliveryOrder.getAddress(), request.getAddress);
//        return null;
//    }
//
//
//    @LogRecord(content="修改了配送地址")
//    public void modifyAddress(updateDeliveryRequest request){
//        // 更新派送信息 电话，收件人、地址
//        doUpdate(request);
//    }
//
//    @LogRecord(
//            content = "修改了订单的配送地址：从“%s”修改到“%s”")
//    public void modifyAddress(updateDeliveryRequest request){
//        // 更新派送信息 电话，收件人、地址
//        doUpdate(request);
//    }
//
//    @LogRecord(content = "修改了订单的配送地址：从“#request.oldAddress”, 修改到“request.getAddress”",
//            bizNo="#request.getDeliveryOrderNo()")
//    public void modifyAddress(updateDeliveryRequest request){
//        // 查询出原来的地址是什么
//        LogRecordContext.putVariable("oldAddress", DeliveryService.queryOldAddress(request.getDeliveryOrderNo()));
//        // 更新派送信息 电话，收件人、地址
//        doUpdate(request);
//    }
//
//    @LogRecord(content = "修改了订单的配送员：从“{queryOldUser{#request.getDeliveryOrderNo()}}”, 修改到“{deveryUser{#request.getUserId()}}”",
//            bizNo="#request.getDeliveryOrderNo()")
//    public void modifyAddress(updateDeliveryRequest request){
//        // 更新派送信息 电话，收件人、地址
//        doUpdate(request);
//    }
}
