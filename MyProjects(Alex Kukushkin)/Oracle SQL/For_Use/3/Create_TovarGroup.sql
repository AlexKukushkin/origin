create table tovar_group
( 
    id_group integer not null primary key,
    group_name varchar2(10),
    count_on_sklad number(10),
    roznich_st number(25, 2),
    nacenka number(5, 2)
);