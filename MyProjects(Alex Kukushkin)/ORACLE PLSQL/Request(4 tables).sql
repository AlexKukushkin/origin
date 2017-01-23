SELECT ListWorkers.worker_id, ListWorkers.w_surname, ListWorkers.w_name, 
ListWorkers.w_fathername, Pasport.seria, Pasport.serial_number, 
Pasport.unit_name, Pasport.date_of_issue, 
Pasport.registration, Pasport.marital_status, 
WorkerEducation.education, WorkerEducation.university, 
WorkerEducation.speciality, WorkerEducation.cvalification, 
WorkerEducation.graduation_date, Guild.guild_name
FROM ((ListWorkers LEFT JOIN Pasport ON ListWorkers.worker_id = Pasport.worker_id) 
LEFT JOIN WorkerEducation ON ListWorkers.worker_id = WorkerEducation.worker_id) 
LEFT JOIN Guild ON ListWorkers.worker_id = Guild.worker_id;
