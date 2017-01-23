delete from tovar_group where id_group = 4;


select * from tovar;
select * from tovar_group;

insert into tovar_group
(id_group, group_name, count_on_sklad, roznich_st, nacenka)
values
(4, 'Компьютер', null, null, 0.5);

insert into tovar
(tovar_name, id_group, prihod_cena, roznich_cena, kol_sklad)
values
('ASUS', 4, null, null, null);