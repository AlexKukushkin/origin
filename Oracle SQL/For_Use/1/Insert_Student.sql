insert into Student
(id_student, initials, id_group, id_chair, id_faculty) 
values
(1006, '�������� ������ ���������', 31, 3, 2);

select * from student;

select * from ugroup;

delete from student
where initials = '������ ������ ��������';

update student 
set id_group = 11
where initials = '������� ����� ����������';

