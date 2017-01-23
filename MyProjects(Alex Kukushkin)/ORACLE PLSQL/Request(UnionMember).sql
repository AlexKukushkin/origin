SELECT ListWorkers.worker_id, ListWorkers.w_surname, 
ListWorkers.w_name, ListWorkers.w_fathername, ListWorkers.union_member
FROM ListWorkers
WHERE (ListWorkers.union_member = 1);
