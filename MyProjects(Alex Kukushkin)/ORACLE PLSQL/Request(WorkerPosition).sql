select ListWorkers.worker_id, ListWorkers.w_surname, ListWorkers.w_name, 
ListWorkers.w_fathername, WorkerPosition.position_name, WorkerPosition.position_appointment_date, 
WorkerPosition.position_translation 
from
ListWorkers left join WorkerPosition on ListWorkers.worker_id = WorkerPosition.worker_id;