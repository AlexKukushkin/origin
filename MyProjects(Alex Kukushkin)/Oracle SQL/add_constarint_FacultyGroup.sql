alter table faculty_group
add foreign key (id_faculty) references faculty(id_faculty);

alter table faculty_group
add foreign key (id_group) references ugroup(id_group);