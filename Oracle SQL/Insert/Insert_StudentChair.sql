insert into student_chair
(id_student, id_chair)
values
(1011, 5);


select * from student;
select * from chair;
select * from ugroup;
select * from student_chair;

select * from student inner join student_chair
on student.id_student = student_chair.id_student
where student_chair.id_chair = 2;