select *
from
student s
where s.id_faculty = (select id_faculty from faculty f where f.faculty_name = 'Информатика и вычислительная техника'); 

select * from ugroup;
select * from student;