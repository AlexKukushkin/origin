select *
from
ugroup g
where g.id_chair = (select id_chair from chair c where c.chair_name = '����������� �����������'); 

select * from ugroup;
select * from student;