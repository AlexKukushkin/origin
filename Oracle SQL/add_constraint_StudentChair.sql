alter table student_chair
add foreign key (id_student) references student(id_student);

alter table student_chair
add foreign key (id_chair) references chair(id_chair);