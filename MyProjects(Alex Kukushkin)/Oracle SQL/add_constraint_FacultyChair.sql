alter table faculty_chair
add foreign key (id_faculty) references faculty(id_faculty);

alter table faculty_chair
add foreign key (id_chair) references chair(id_chair);