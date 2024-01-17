-- auto-generated definition
create table user
(
    id           tinyint auto_increment
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '用户头像
',
    gender       tinyint                            null comment '性别',
    userPassword varchar(512)                       null comment '密码',
    email        varchar(512)                       null comment '邮箱',
    userStatus   tinyint  default 0                 null comment '用户状态 0-正常',
    phone        varchar(128)                       null comment '电话',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete     tinyint  default 0                 null comment '是否删除 0-不删除',
    userRole     tinyint  default 0                 not null comment '0-普通用户 1-管理员',
    planetCode   varchar(128)                       not null comment '星球编号'
)
    comment '用户表';

-- yupi daoyupi 密码都是12345678  其他密码都是123456789

