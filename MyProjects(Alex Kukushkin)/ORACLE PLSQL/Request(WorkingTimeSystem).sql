SELECT ListWorkers.worker_id, ListWorkers.w_surname, ListWorkers.w_name, 
ListWorkers.w_fathername, WorkingTimeSystem.working_day, WorkingTimeSystem.working_hour_number, 
WorkingTimeSystem.simbol
FROM ListWorkers LEFT JOIN WorkingTimeSystem 
ON ListWorkers.worker_id = WorkingTimeSystem.worker_id;
