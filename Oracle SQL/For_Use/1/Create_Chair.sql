create table chair
(
   id_chair integer not null primary key,
   chair_name varchar2(100),
   chair_head varchar2(100),
   id_faculty integer not null
);

select * from chair