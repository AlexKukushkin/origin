create or replace
TRIGGER TRIGGER1
AFTER UPDATE OF KOL_SKLAD ON tovar
FOR EACH ROW
declare
BEGIN
  UPDATE tovar_group
  SET tovar_group.count_on_sklad = 
         (SELECT SUM(KOL_SKLAD) FROM tovar
                 WHERE id_group = :OLD.id_group),
      tovar_group.roznich_st = 
         (SELECT SUM(KOL_SKLAD * roznich_cena) FROM tovar 
                  WHERE id_group = :OLD.id_group) 
  WHERE tovar_group.id_group = :OLD.id_group;                                                                                               
END;