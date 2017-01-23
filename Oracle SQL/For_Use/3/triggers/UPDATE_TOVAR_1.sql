create or replace
TRIGGER UPDATE_TOVAR_1 
BEFORE INSERT OR UPDATE ON TOVAR 
FOR EACH ROW 

DECLARE
  CURSOR curs1(ID_GRUP in tovar.id_group % TYPE) IS
    SELECT tovar_group.nacenka
    FROM tovar_group
    WHERE tovar_group.id_group = ID_GRUP;
    
  get_nacenka tovar_group.nacenka % TYPE;
  
BEGIN
  OPEN curs1(:new.id_group);
    FETCH curs1 into get_nacenka;
  CLOSE curs1;
  :new.roznich_cena := :new.prihod_cena * (1 + get_nacenka); 
END;