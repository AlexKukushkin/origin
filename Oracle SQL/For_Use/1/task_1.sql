select *
from
student s
where s.id_group = (select id_group from ugroup u where u.group_name = '���11'); 

select * from ugroup;
select * from student;