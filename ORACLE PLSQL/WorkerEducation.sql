create table WorkerEducation
(
    worker_id number(38),
    education varchar2(70),
    university varchar2(100),
    speciality varchar2(100),
    cvalification varchar2(100),
    graduation_date date,
    serial_id varchar2(10),
    primary key(worker_id)    
);

desc WorkerEducation

drop table WorkerEducation;