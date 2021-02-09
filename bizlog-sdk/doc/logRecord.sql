CREATE TABLE t_log_record
(
    id          INT(20) UNSIGNED AUTO_INCREMENT COMMENT '主键',
    tenant      varchar(100)  NOT NULL DEFAULT '' COMMENT '租户标识',
    biz_key     varchar(100)  NOT NULL DEFAULT '' COMMENT '日志业务标识',
    biz_no      varchar(100)  NOT NULL DEFAULT '' COMMENT '业务businessNo',
    operator    varchar(50)   NOT NULL DEFAULT '' COMMENT '操作人',
    action      varchar(100)  NOT NULL DEFAULT '' COMMENT '动作',
    category    varchar(100)  NOT NULL DEFAULT '' COMMENT '种类',
    detail      varchar(2000) NOT NULL DEFAULT '' COMMENT '修改的详细信息，可以为json',
    create_time DATETIME               DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    primary key (id),
    key idx_biz_key (biz_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '操作日志表';
