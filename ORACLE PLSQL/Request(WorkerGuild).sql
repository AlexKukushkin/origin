SELECT ListWorkers.worker_id, ListWorkers.w_surname, ListWorkers.w_name, 
ListWorkers.w_fathername, Guild.guild_name
FROM ListWorkers LEFT JOIN Guild ON ListWorkers.worker_id = Guild.worker_id;
