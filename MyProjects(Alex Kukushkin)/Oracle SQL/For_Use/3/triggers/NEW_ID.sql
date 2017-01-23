CREATE OR REPLACE TRIGGER NEW_ID 
  BEFORE INSERT ON TOVAR 
  for each row
DECLARE
BEGIN
  select sc.nextval into :new.id_tovar
  from dual;
END NEW_ID;