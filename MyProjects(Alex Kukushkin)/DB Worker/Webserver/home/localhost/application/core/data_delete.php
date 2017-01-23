<?php
mysql_connect("localhost", "root", "")
or die("<p>Ошибка подключения к базе данных! " . mysql_error() . "</p>");


mysql_select_db("site")
 or die("<p>Ошибка выбора базы данных! ". mysql_error() . "</p>");
 
$id=$_GET['id'];
$title=trim($_GET['title']);
$text=trim($_GET['text']);
$data=trim($_GET['data']);

$delete_sql = "DELETE FROM news WHERE id=$id";
mysql_query($delete_sql) or 
die("<p>При удалении произошла ошибка</p>". mysql_error());
echo "<p>Запись была успешно удалена!</p>";
?>