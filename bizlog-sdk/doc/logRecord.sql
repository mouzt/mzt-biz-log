CREATE TABLE tb_log_record(
  id  INT(20) UNSIGNED AUTO_INCREMENT COMMENT '主键',
  biz_key varchar(100) NOT NULL DEFAULT '' COMMENT '日志业务标识',
  biz_no varchar(100) NOT NULL DEFAULT '' COMMENT '业务businessNo',
  operator varchar(50) NOT NULL DEFAULT '' COMMENT '操作人',
  operator_id  varchar(50) NOT NULL DEFAULT '' COMMENT '操作人ID',
  action varchar(100) NOT NULL DEFAULT '' COMMENT '动作',
  category  varchar(100) NOT NULL DEFAULT '' COMMENT '描述',
  app_key  varchar(100) NOT NULL DEFAULT '' COMMENT '记录来源系统',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
  primary key pk_id(id),
  key idx_bizkey(biz_key)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '操作日志表';
