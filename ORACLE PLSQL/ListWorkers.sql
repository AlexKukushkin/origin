create table ListWorkers
(
    worker_id number(38),
    w_surname varchar2(25),
    w_name varchar2(25),
    w_fathername varchar2(35),
    birthday date,
    union_member  number(1),
    home_tel varchar2(15),
    job_tel varchar2(15),
    characteristics varchar2(100),
    date_appointment date,
    date_dismissal date,
    primary key(worker_id)  
);

