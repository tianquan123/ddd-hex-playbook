drop table if exists sample_order;

create table sample_order (
    id varchar(64) primary key,
    product_code varchar(64) not null,
    quantity integer not null,
    status varchar(32) not null,
    create_time timestamp not null,
    update_time timestamp not null
);
