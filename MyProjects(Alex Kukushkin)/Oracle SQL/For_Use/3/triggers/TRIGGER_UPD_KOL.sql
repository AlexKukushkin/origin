create or replace
TRIGGER TRIGGER_UPD_KOL 
BEFORE UPDATE OF KOL_SKLAD ON TOVAR 
FOR EACH ROW 

DECLARE
V_SUM_1 NUMBER;
V_SUM_2 NUMBER;
BEGIN
  TRIGUPDKOL.V_num := TRIGUPDKOL.V_num + 1;
  TRIGUPDKOL.V_tovar_ID(TRIGUPDKOL.V_num):= :new.id_tovar;
  TRIGUPDKOL.V_kol_TOV(TRIGUPDKOL.V_num):= :new.kol_sklad;
  TRIGUPDKOL.V_group_ID(TRIGUPDKOL.V_num):= :new.id_group;
  
  SELECT SUM(TRIGUPDKOL.V_kol_TOV(TRIGUPDKOL.V_num)) INTO V_SUM_1 FROM tovar
         WHERE id_group = :old.id_group;
                 
  SELECT SUM(TRIGUPDKOL.V_kol_TOV(TRIGUPDKOL.V_num) * roznich_cena) INTO V_SUM_2  FROM tovar 
         WHERE id_group = :old.id_group; 
  
  UPDATE tovar_group
  SET count_on_sklad = v_sum_1,
      roznich_st = v_sum_2
  WHERE tovar_group.id_group = :old.id_group;
END
