<?php
mysql_connect("localhost", "triada", "191919")//параметры в скобках ("хост", "имя пользователя", "пароль")
or die("<p>Ошибка подключения к базе данных! " . mysql_error() . "</p>");


mysql_select_db("triada")//параметр в скобках ("имя базы, с которой соединяемся")
 or die("<p>Ошибка выбора базы данных! ". mysql_error() . "</p>");
 
$id=$_GET['id'];
$title=trim($_GET['title']);
$text=trim($_GET['text']);
$data=trim($_GET['data']);

$update_sql = "UPDATE alex_team SET title='$title', text='$text', data='$data' WHERE id='$id'";
mysql_query($update_sql) or die("Ошибка вставки" . mysql_error());
echo '<p>Запись успешно обновлена!</p>';
?>

