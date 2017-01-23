create table Pasport
(
    worker_id number(38),
    seria varchar2(15),
    serial_number varchar2(15),
    unit_name varchar2(50),
    date_of_issue date,
    registration varchar2(15),
    marital_status varchar2(25),
    primary key(worker_id)     
);

desc Pasport;
drop table Pasport;