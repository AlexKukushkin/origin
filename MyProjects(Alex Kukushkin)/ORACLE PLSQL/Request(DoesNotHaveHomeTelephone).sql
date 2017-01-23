SELECT ListWorkers.worker_id, ListWorkers.w_surname, ListWorkers.w_name, ListWorkers.w_fathername, 
ListWorkers.home_tel
FROM ListWorkers
WHERE (ListWorkers.home_tel Is Null);
