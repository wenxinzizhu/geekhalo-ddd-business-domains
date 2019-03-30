create table tb_user
(
	id bigint auto_increment primary key,
	uid bigint not null,
	name varchar(32) null,
	birth_at date null
);

