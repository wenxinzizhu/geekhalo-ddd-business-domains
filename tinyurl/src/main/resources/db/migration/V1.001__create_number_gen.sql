create table tb_number_gen
(
	id bigint auto_increment primary key,
	`version` bigint not null,
	type varchar(16) not null,
	current_number bigint not null
);
CREATE UNIQUE INDEX unq_type ON tb_number_gen (type);
