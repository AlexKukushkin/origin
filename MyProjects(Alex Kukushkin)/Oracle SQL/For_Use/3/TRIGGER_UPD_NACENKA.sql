create or replace
TRIGGER TRIGGER2
AFTER UPDATE OF NACENKA ON TOVAR_GROUP
FOR EACH ROW
DECLARE
BEGIN
  UPDATE tovar
   SET roznich_cena = prihod_cena * (1 + :NEW.nacenka) WHERE id_group = :new.id_group;
END;