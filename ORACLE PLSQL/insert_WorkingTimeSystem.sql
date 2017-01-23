insert into WorkingTimeSystem
(worker_id, working_day, working_hour_number, simbol)
values
(15, '31-JAN-2013', 8, null);

delete from WorkingTimeSystem 
where worker_id = 8 and working_day = '13-JAN-2013';

select * from WorkingTimeSystem;