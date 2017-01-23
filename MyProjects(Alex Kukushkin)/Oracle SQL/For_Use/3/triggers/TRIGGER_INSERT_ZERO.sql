CREATE OR REPLACE TRIGGER TRIGGER_INSERT_ZERO 
BEFORE INSERT OR UPDATE ON TOVAR 
FOR EACH ROW 
BEGIN
  if :new.tovar_name is NULL then
     :new.tovar_name := '0';
  end if;
  if :new.id_group is NULL then
     :new.id_group := 0;
  end if;
  if :new.prihod_cena is NULL then
     :new.prihod_cena := 0;
  end if;
  if :new.roznich_cena is NULL then
     :new.roznich_cena := 0;
  end if;
  if :new.kol_sklad is NULL then
     :new.kol_sklad := 0;
  end if;
END;