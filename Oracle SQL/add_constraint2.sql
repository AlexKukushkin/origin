alter table student
add foreign key (id_group) references ugroup(id_group) on delete cascade;

alter table student drop constraint id_group_fk;

delete from ugroup
where id_group = 22;

select * from student;