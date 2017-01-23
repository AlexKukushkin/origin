<?php

// подключаем файлы ядра
//$db=new PDO('mysql:host=localhost;dbname=site','root','');
require_once 'core/db.php';
require_once 'core/model.php';
require_once 'core/view.php';
require_once 'core/controller.php';
require_once 'core/route.php';

require_once 'core/Request.php';
require_once 'core/Response.php';
require_once 'core/ResponseJson.php';
$db=new PDO('mysql:host=localhost;dbname=triada','triada','191919');
/*
Здесь обычно подключаются дополнительные модули, реализующие различный функционал:
	> аутентификацию
	> кеширование
	> работу с формами
	> абстракции для доступа к данным
	> ORM
	> Unit тестирование
	> Benchmarking
	> Работу с изображениями
	> Backup
	> и др.
*/

require_once 'core/route.php';
Route::start(); // запускаем маршрутизатор
?>