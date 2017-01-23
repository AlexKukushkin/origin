SELECT ListWorkers.worker_id, ListWorkers.w_surname, ListWorkers.w_name, 
ListWorkers.w_fathername, ListWorkers.birthday, ListWorkers.union_member, 
ListWorkers.home_tel, ListWorkers.job_tel, 
ListWorkers.characteristics, ListWorkers.date_appointment, 
ListWorkers.date_dismissal, WorkerPosition.position_name, WorkerPosition.position_appointment_date, 
WorkerPosition.position_translation
FROM ListWorkers LEFT JOIN WorkerPosition ON ListWorkers.worker_id = WorkerPosition.worker_id
WHERE (WorkerPosition.position_name = 'Введите название должности')
ORDER BY ListWorkers.worker_id;
