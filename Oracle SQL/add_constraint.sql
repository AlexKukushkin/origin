alter table student 
add constraint id_group_fk 
foreign key (id_group) references ugroup(id_group) on delete cascade;