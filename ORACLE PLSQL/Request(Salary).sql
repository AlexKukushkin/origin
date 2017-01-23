SELECT ListWorkers.worker_id, ListWorkers.w_surname, 
ListWorkers.w_name, ListWorkers.w_fathername, 
Salary.year, Salary.month, Salary.salary
FROM ListWorkers LEFT JOIN Salary ON ListWorkers.worker_id = Salary.worker_id;
