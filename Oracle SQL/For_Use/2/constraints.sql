alter table journal
add foreign key (id_student) references student(id_student);


alter table journal
add foreign key (id_subject) references training_course(id_c);

alter table student
add foreign key (id_group) references ugroup(id_group);
