alter table group_chair
add foreign key (id_group) references ugroup(id_group);

alter table group_chair
add foreign key (id_chair) references chair(id_chair);