<?php
mysql_connect("localhost", "triada", "191919")
or die("<p>������ ����������� � ���� ������! " . mysql_error() . "</p>");


mysql_select_db("triada")
 or die("<p>������ ������ ���� ������! ". mysql_error() . "</p>");
 
$id=$_GET['id'];
$title=trim($_GET['title']);
$text=trim($_GET['text']);
$data=trim($_GET['data']);

$delete_sql = "DELETE FROM alex_team WHERE id=$id";
mysql_query($delete_sql) or 
die("<p>��� �������� ��������� ������</p>". mysql_error());
echo "<p>������ ���� ������� �������!</p>";
?>