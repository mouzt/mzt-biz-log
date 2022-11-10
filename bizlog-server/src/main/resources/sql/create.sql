create table t_logrecord
(
    `id`            bigint(11) unsigned not null auto_increment comment 'id',
    `tenant`        varchar(63)         not null default '' comment '租户标识',
    `type`          varchar(63)         not null default '' comment '保存的操作日志的类型，比如：订单类型、商品类型',
    `sub_type`      varchar(63)         not null default '' comment '日志的子类型，比如订单的C端日志，和订单的B端日志，type都是订单类型，但是子类型不一样',
    `biz_no`        varchar(63)         not null default '' comment '日志绑定的业务标识',
    `operator`      varchar(63)         not null default '' comment '操作人',
    `action`        varchar(1023)       not null default '' comment '日志内容',
    `fail`          tinyint(1) unsigned not null default 0 comment '记录是否是操作失败的日志',
    `create_time`   datetime(3)         not null default current_timestamp(3) comment '创建时间',
    `extra`         varchar(2000)       not null default '' comment '扩展信息',
    `code_variable` varchar(2000)       not null default '' comment '代码变量信息',
    primary key (id)
);