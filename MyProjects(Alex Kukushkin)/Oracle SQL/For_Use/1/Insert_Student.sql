insert into Student
(id_student, initials, id_group, id_chair, id_faculty) 
values
(1006, 'Кирсанов Руслан Сергеевич', 31, 3, 2);

select * from student;

select * from ugroup;

delete from student
where initials = 'Петров Сергей Петрович';

update student 
set id_group = 11
where initials = 'Иванова Ирина Валерьевна';

