insert into group_chair
(id_group, id_chair)
values
(61, 1);


select * from chair;
select * from ugroup;
select * from group_chair;


delete from group_chair
where id_group = 61 and id_chair = 1;