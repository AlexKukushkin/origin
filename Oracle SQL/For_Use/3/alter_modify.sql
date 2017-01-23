select * from tovar;

select * from tovar_group;

drop trigger trigger_updt_kol;

ALTER TABLE tovar
MODIFY kol_sklad  number;