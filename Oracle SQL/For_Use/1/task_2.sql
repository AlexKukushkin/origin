select *
from
student s
where s.id_chair = (select id_chair from chair c where c.chair_name = '���������� � ����������'); 

select * from ugroup;
select * from student;

select * from chair;