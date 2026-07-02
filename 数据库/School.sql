create table address
(
    id            int auto_increment
        primary key,
    user_id       bigint                             not null,
    receiver      varchar(50)                        null,
    contact       varchar(20)                        null,
    province_code varchar(50)                        null,
    city_code     varchar(50)                        null,
    county_code   varchar(50)                        null,
    address       varchar(255)                       null,
    full_location varchar(255)                       null,
    is_default    tinyint  default 0                 null,
    create_time   datetime default CURRENT_TIMESTAMP null,
    lat           decimal(10, 7)                     null comment '用户地址纬度',
    lng           decimal(10, 7)                     null comment '用户地址经度'
);

create index idx_user_id
    on address (user_id);

create table admin
(
    id          int auto_increment comment 'id'
        primary key,
    username    varchar(255)                        null comment '用户名称',
    password    varchar(255)                        null comment '密码',
    nickname    varchar(255)                        null comment '昵称',
    avatar_url  varchar(255)                        null comment '头像',
    tel         varchar(255)                        null comment '手机号',
    email       varchar(255)                        null comment '邮箱',
    roles       varchar(128)                        null comment '身份',
    create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    address     varchar(255)                        null comment '地址',
    status      varchar(10)                         null,
    age         int                                 null
)
    collate = utf8mb4_unicode_ci
    row_format = DYNAMIC;

create table after_sale
(
    id          int auto_increment
        primary key,
    order_id    varchar(64)                             not null,
    user_id     bigint                                  not null,
    merchant_id int                                     null,
    rider_id    int                                     null,
    type        tinyint                                 not null comment '1=取消/改单 2=退款 3=投诉差评 4=发票申请',
    reason      varchar(500)  default ''                null,
    img_urls    varchar(1000) default ''                null,
    status      tinyint       default 0                 null comment '0=待处理 1=处理中 2=已完成 3=已驳回',
    create_time datetime      default CURRENT_TIMESTAMP null,
    update_time datetime      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '售后服务表';

create index idx_order_id
    on after_sale (order_id);

create index idx_user_id
    on after_sale (user_id);

create table bid_strategy
(
    id          int auto_increment
        primary key,
    merchant_id int                         not null,
    name        varchar(50)                 not null,
    base_bid    decimal(10, 2) default 0.00 null comment '基础竞价',
    min_bid     decimal(10, 2) default 0.00 null comment '最低竞价',
    max_bid     decimal(10, 2) default 0.00 null comment '最高竞价',
    status      int            default 1    null comment '1启用 0禁用',
    priority    int            default 10   null comment '策略优先级，数值越小越优先'
);

create table cart
(
    id           int auto_increment
        primary key,
    user_id      bigint         null,
    sku_id       int            null,
    goods_id     int            null,
    name         varchar(255)   null,
    picture      varchar(255)   null,
    count        int            null,
    price        decimal(10, 2) null,
    now_price    decimal(10, 2) null,
    stock        int            null,
    selected     tinyint(1)     null,
    attrs_text   varchar(255)   null,
    is_effective tinyint(1)     null,
    create_time  datetime       null
);

create index fk_cart_goods
    on cart (goods_id);

create index fk_cart_sku
    on cart (sku_id);

create index idx_cart_user
    on cart (user_id);

create table category
(
    category_id   int auto_increment
        primary key,
    merchant_id   int           not null comment '所属商家',
    category_name varchar(50)   not null comment '分类名',
    sort          int default 0 null comment '排序'
);

create index fk_category_merchant
    on category (merchant_id);

create table comment
(
    comment_id  int auto_increment
        primary key,
    order_id    varchar(64)                            not null,
    user_id     bigint                                 not null,
    merchant_id int                                    null,
    rider_id    int                                    null,
    score       int          default 5                 not null,
    content     varchar(500) default ''                null,
    create_time datetime     default CURRENT_TIMESTAMP null,
    constraint uk_order_id
        unique (order_id)
)
    comment '订单评价表';

create index fk_comment_user
    on comment (user_id);

create table coupon
(
    id           int auto_increment
        primary key,
    merchant_id  int                         not null,
    name         varchar(50)                 not null,
    type         int            default 1    null comment '1满减 2折扣',
    min_amount   decimal(10, 2) default 0.00 null,
    discount     decimal(10, 2) default 0.00 null,
    reduce_price decimal(10, 2) default 0.00 null,
    total        int            default 0    null,
    used         int            default 0    null,
    start_time   datetime                    null,
    end_time     datetime                    null,
    status       int            default 1    null
);

create table errand
(
    errand_id     int auto_increment
        primary key,
    order_id      varchar(32)             not null comment '关联订单',
    user_id       bigint                  not null comment '发布人',
    delivery_id   int                     null comment '接单配送员',
    errand_type   tinyint                 not null comment '1-代取快递 2-代买 3-代送 4-其他',
    start_address varchar(255)            not null comment '取件地址',
    end_address   varchar(255)            not null comment '送达地址',
    remark        varchar(255) default '' null comment '任务说明',
    status        tinyint      default 0  null comment '0-待接单 1-配送中 2-已完成 3-取消'
);

create table freight_template
(
    id            int auto_increment
        primary key,
    merchant_id   int                                      not null,
    name          varchar(50)                              not null comment '模板名称',
    base_price    decimal(10, 2) default 0.00              null comment '起步价',
    base_distance int            default 1000              null comment '起步距离（米）',
    extra_price   decimal(10, 2) default 0.00              null comment '每超1km加价',
    max_price     decimal(10, 2) default 0.00              null comment '最高运费',
    create_time   datetime       default CURRENT_TIMESTAMP null
);

create table goods
(
    goods_id       int auto_increment
        primary key,
    merchant_id    int                 not null,
    category_id    int                 not null,
    goods_name     varchar(100)        not null comment '商品名',
    price          decimal(10, 2)      not null comment '原价',
    discount_price decimal(10, 2)      null comment '优惠价',
    img            varchar(255)        null comment '图片',
    stock          int     default 999 null comment '库存',
    status         tinyint default 1   null comment '1-上架 0-下架',
    `desc`         varchar(50)         null
);

create index idx_goods_category
    on goods (category_id);

create index idx_goods_merchant
    on goods (merchant_id);

create table goods_sku
(
    id             int auto_increment
        primary key,
    goods_id       int                                not null comment '所属商品ID',
    sku_name_arr   varchar(255)                       null comment '规格值数组（逗号分隔）',
    image          varchar(255)                       null comment 'SKU图片',
    price          decimal(10, 2)                     not null comment '售价',
    discount_price decimal(10, 2)                     null comment '折扣价',
    stock          int      default 0                 not null comment '库存',
    create_time    datetime default CURRENT_TIMESTAMP null
);

create index idx_goods_id
    on goods_sku (goods_id);

create table goods_spec
(
    id       int auto_increment
        primary key,
    goods_id int         not null comment '所属商品ID',
    name     varchar(50) not null comment '规格名称（如：颜色、尺寸）'
);

create index idx_goods_id
    on goods_spec (goods_id);

create table goods_spec_item
(
    id      int auto_increment
        primary key,
    spec_id int         not null comment '所属规格ID',
    name    varchar(50) not null comment '规格值（如：红色、XL）'
);

create index idx_spec_id
    on goods_spec_item (spec_id);

create table invoice_apply
(
    id             bigint auto_increment
        primary key,
    invoice_no     varchar(64)                              not null comment '开票申请单号',
    apply_type     tinyint                                  null comment '1商家 2骑手',
    target_id      int                                      not null comment '骑手/商家ID',
    settle_no      varchar(64)                              null comment '关联结算单号',
    invoice_amount decimal(12, 2) default 0.00              null comment '开票金额',
    invoice_title  varchar(200)                             null comment '发票抬头',
    tax_no         varchar(100)                             null comment '纳税人识别号',
    status         tinyint        default 0                 null comment '0待开票 1已开票 2作废',
    create_time    datetime       default CURRENT_TIMESTAMP null,
    update_time    datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    order_id       varchar(50)                              null comment '订单id',
    rider_name     varchar(50)                              null comment '骑手姓名',
    constraint uk_invoice_no
        unique (invoice_no)
);

create table merchant
(
    merchant_id   int auto_increment comment '商家ID'
        primary key,
    user_id       bigint                                  not null comment '所属用户ID',
    merchant_name varchar(100)                            not null comment '店铺名称',
    cover_img     varchar(255)                            null comment '店铺封面',
    address       varchar(255)                            not null comment '店铺位置',
    score         decimal(2, 1) default 5.0               null comment '评分',
    status        tinyint       default 1                 null comment '1-营业 0-停业',
    create_time   datetime      default CURRENT_TIMESTAMP null,
    alt           varchar(100)                            null,
    postFee       double                                  null comment '配送费',
    lat           decimal(12, 8)                          null,
    lng           decimal(12, 8)                          null
);

create index idx_merchant_user
    on merchant (user_id);

create table order_item
(
    item_id     int auto_increment
        primary key,
    order_id    varchar(32)    not null,
    goods_id    int            not null,
    goods_name  varchar(100)   not null comment '商品名（快照）',
    price       decimal(10, 2) not null comment '下单时价格',
    quantity    int default 1  not null comment '购买数量',
    sku_id      int            null comment '商品规格ID',
    sku_attrs   varchar(255)   null comment '规格属性：颜色/尺寸',
    picture     varchar(500)   null comment '商品图片',
    pay_price   decimal(10, 2) null comment '实付单价',
    total_price decimal(10, 2) null comment '小计金额'
);

create index fk_item_goods
    on order_item (goods_id);

create index fk_item_sku
    on order_item (sku_id);

create index idx_oi_order_id
    on order_item (order_id);

create table order_master
(
    order_id           varchar(32)                              not null comment '订单号（唯一）'
        primary key,
    user_id            bigint                                   not null comment '下单用户',
    merchant_id        int                                      null comment '商家ID',
    address_id         varchar(100)                             not null comment '收货地址',
    order_type         tinyint                                  not null comment 
    total_amount       decimal(10, 2)                           not null comment '总金额',
    order_status       tinyint        default 0                 not null comment '0-待付款 1-待接单 2-待配送 3-配送中 4-已完成 5-已取消',
    delivery_id        int                                      null comment '配送员ID',
    delivery_fee       decimal(10, 2) default 0.00              null comment '配送费',
    note               varchar(255)   default ''                null comment '订单备注',
    create_time        datetime       default CURRENT_TIMESTAMP null,
    pay_time           datetime                                 null,
    finish_time        datetime                                 null,
    pay_type           tinyint        default 1                 null comment '支付方式 1在线支付 2货到付款',
    pay_channel        tinyint                                  null comment '支付渠道 1支付宝 2微信',
    pay_money          decimal(10, 2)                           null comment '实付总金额',
    post_fee           decimal(10, 2) default 0.00              null comment '运费',
    delivery_time_type tinyint        default 1                 null comment '配送时间 1不限 2工作日 3双休',
    mainOrderNo        varchar(50)                              null,
    finish_img         varchar(255)                             null comment '送达凭证照片'
);

create index idx_om_address
    on order_master (address_id);

create index idx_om_create_time
    on order_master (create_time);

create index idx_om_delivery
    on order_master (delivery_id);

create index idx_om_merchant
    on order_master (merchant_id);

create index idx_om_status
    on order_master (order_status);

create index idx_om_user
    on order_master (user_id);

create table peak_price
(
    id          int auto_increment
        primary key,
    merchant_id int                         not null,
    name        varchar(50)                 not null,
    time_range  varchar(100)                null comment '时间段 11:00-13:00',
    week_days   varchar(50)                 null comment '1,2,3,4,5',
    rate        decimal(10, 2) default 1.00 null comment '溢价倍率',
    status      int            default 1    null
);

create table platform_commission_config
(
    id              int auto_increment
        primary key,
    order_type      tinyint                                 null comment
    commission_rate decimal(5, 2) default 15.00             not null comment '平台抽成比例 %',
    create_time     datetime      default CURRENT_TIMESTAMP null,
    update_time     datetime      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table rider
(
    rider_id          int auto_increment comment '骑手ID'
        primary key,
    account           varchar(50)                              not null comment '骑手账号（登录用）',
    password          varchar(100)                             not null comment '密码',
    name              varchar(20)                              not null comment '姓名',
    phone             varchar(20)                              not null comment '手机号',
    student_id        varchar(20)                              null comment '学号',
    status            tinyint        default 1                 null comment '状态：1-正常 0-禁用',
    create_time       datetime       default CURRENT_TIMESTAMP null comment '创建时间',
    balance           decimal(10, 2) default 0.00              null comment '骑手余额/可提现工资',
    lat               decimal(10, 6)                           null comment '纬度',
    lng               decimal(10, 6)                           null comment '经度',
    work_status       tinyint        default 0                 null comment '0休息 1在线',
    today_order_count int            default 0                 null comment '今日已接单',
    daily_order_limit int            default 15                null comment '每日接单上限',
    credit_score      int            default 100               null comment '信用分',
    frozen            tinyint        default 0                 null comment '0正常 1冻结',
    constraint account
        unique (account)
)
    comment '骑手表';

create table rider_balance_flow
(
    id            bigint auto_increment
        primary key,
    rider_id      int                                      not null,
    settle_no     varchar(64)                              null comment '关联结算单号',
    flow_type     tinyint                                  null comment '1佣金入账 2提现 3扣款',
    amount        decimal(12, 2) default 0.00              not null,
    balance_after decimal(12, 2) default 0.00              not null comment '变动后余额',
    remark        varchar(255)                             null,
    create_time   datetime       default CURRENT_TIMESTAMP null
);

create index fk_flow_rider
    on rider_balance_flow (rider_id);

create table rider_settlement
(
    id                  bigint auto_increment
        primary key,
    settle_no           varchar(64)                              not null comment '结算单号',
    rider_id            int                                      not null comment '骑手id',
    settle_start        date                                     null comment '结算开始日期',
    settle_end          date                                     null comment '结算结束日期',
    total_order_num     int            default 0                 null comment '完成订单数',
    total_order_amount  decimal(12, 2) default 0.00              null comment '订单总金额',
    platform_commission decimal(12, 2) default 0.00              null comment '平台抽成合计',
    rider_commission    decimal(12, 2) default 0.00              null comment '骑手应结佣金',
    status              tinyint        default 0                 null comment '0待审核 1待打款 2已打款 3作废',
    create_time         datetime       default CURRENT_TIMESTAMP null,
    update_time         datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_settle_no
        unique (settle_no)
);

create index fk_settle_rider
    on rider_settlement (rider_id);

create table user
(
    id            bigint auto_increment comment '用户ID（主键）'
        primary key,
    username      varchar(50)                            not null comment '用户名（唯一）',
    password      varchar(100)                           not null comment '密码',
    nickname      varchar(50)  default ''                null comment '用户昵称',
    avatar_url    varchar(255) default ''                null comment '头像URL',
    tel           varchar(20)  default ''                null comment '手机号码',
    email         varchar(100) default ''                null comment '邮箱地址',
    status        varchar(10)  default 'NORMAL'          not null comment '用户状态',
    create_time   datetime                               not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    address       varchar(100)                           null,
    gender        varchar(10)                            null,
    birthday      varchar(20)                            null,
    province_code varchar(50)                            null,
    city_code     varchar(50)                            null,
    county_code   varchar(50)                            null,
    constraint uk_username
        unique (username)
);

create index idx_create_time
    on user (create_time);

create index idx_email
    on user (email);

create index idx_tel
    on user (tel);


