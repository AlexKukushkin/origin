select *
from
ugroup g
where g.id_faculty = (select id_faculty from faculty f where f.faculty_name = '����������� � �������������� �������'); 

select * from ugroup;
select * from student;