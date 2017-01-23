create table WorkerPosition
(
    worker_id number(38),
    position_name varchar2(75),
    position_appointment_date date,   
    position_translation varchar2(75)
);

desc WorkerPosition
select * from WorkerPosition
drop table WorkerPosition;