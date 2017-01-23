alter table chair
add foreign key (id_faculty) references faculty(id_faculty);

alter table ugroup
add foreign key (id_chair) references chair(id_chair);

alter table ugroup
add foreign key (id_faculty) references faculty(id_faculty);

alter table student
add foreign key (id_group) references ugroup(id_group);

alter table student
add foreign key (id_chair) references chair(id_chair);

alter table student
add foreign key (id_faculty) references faculty(id_faculty);