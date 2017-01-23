select * from 
student s inner join ugroup u 
on s.id_group = u.id_group
where u.course = 3;

select * from ugroup;
select * from student;