create table tovar
( 
    id_tovar integer not null primary key,
    tovar_name varchar2(30),
    id_group integer,
    prihod_cena number,
    roznich_cena number,
    kol_sklad integer
);