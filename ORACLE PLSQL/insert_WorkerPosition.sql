insert into WorkerPosition
(worker_id, position_name, position_appointment_date, position_translation)
values
(15, 'техник', '23-JUN-1996', null);

select * from WorkerPosition;

drop table WorkerPosition;

create table WorkerPosition
(
    worker_id number(38),
    position_name varchar2(75),
    position_appointment_date date,   
    position_translation varchar2(75)
);
