<?php

$id=$_GET['id'];
$title=trim($_GET['title']);
$text=trim($_GET['text']);
$data=trim($_GET['data']);

$delete_sql = "DELETE FROM alex_team WHERE id=$id";
mysql_query($delete_sql)
echo "<p>Запись была успешно удалена!</p>";
?>