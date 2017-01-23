SELECT ListWorkers.worker_id, ListWorkers.w_surname, 
ListWorkers.w_name, ListWorkers.w_fathername, WorkerEducation.education, 
WorkerEducation.university, WorkerEducation.speciality, 
WorkerEducation.cvalification, WorkerEducation.graduation_date, 
WorkerEducation.serial_id
FROM WorkerEducation LEFT JOIN ListWorkers 
ON WorkerEducation.worker_id = ListWorkers.worker_id;
