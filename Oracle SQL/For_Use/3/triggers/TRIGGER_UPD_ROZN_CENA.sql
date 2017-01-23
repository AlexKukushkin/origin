CREATE OR REPLACE TRIGGER TRIGGER_UPD_ROZN_CENA 
AFTER UPDATE OF ROZNICH_CENA ON TOVAR 
FOR EACH ROW 
BEGIN
  UPDATE tovar_group SET roznich_st = (SELECT SUM(:new.roznich_cena * kol_sklad) FROM tovar WHERE id_group = :old.id_group) WHERE id_group = :old.id_group;
  COMMIT;
END;