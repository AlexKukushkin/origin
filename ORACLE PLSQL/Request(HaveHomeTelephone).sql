select ListWorkers.worker_id, ListWorkers.w_surname, ListWorkers.w_name, 
ListWorkers.w_fathername, ListWorkers.home_tel 
from 
ListWorkers
where ListWorkers.home_tel is not null;