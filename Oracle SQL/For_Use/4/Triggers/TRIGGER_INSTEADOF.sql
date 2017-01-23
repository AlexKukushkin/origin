CREATE OR REPLACE TRIGGER TRIGGER_INSTEADOF 
INSTEAD OF UPDATE ON TOVAR_VIEW 
BEGIN
  UPDATE tovar SET tovar_name = :NEW.name_t, prihod_cena = :NEW.price, kol_sklad = :NEW.count_sklad WHERE tovar_name = :OLD.name_t;

--  EXCEPTION WHEN except_error THEN RAISE_APPLICATION_ERROR(-20100, 'Имя группы введено неправильно!!!');--
END;