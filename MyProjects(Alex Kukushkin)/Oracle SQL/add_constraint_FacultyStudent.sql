alter table faculty_student
add foreign key (id_faculty) references faculty(id_faculty);

alter table faculty_student
add foreign key (id_student) references student(id_student);