create table student
(
    id_student integer not null primary key,
    initials   varchar2(100) not null,
    id_group integer not null,
    id_chair integer not null,
    id_faculty integer not null
);