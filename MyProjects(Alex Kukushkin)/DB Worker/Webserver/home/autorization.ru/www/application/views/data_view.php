<!--<html>
<head>
</head>
<body>
<form action="core/data.php" method="GET"> <br>
Заголовок новости <input type="text" name="title" size="20" maxlength="30" value=""><br><br>
Текст <br>
<textarea name="text" cols="15" rows="10"></textarea><br><br>
Дата (год-месяц-день)<input type="text" name="date" size="20" maxlength="30" value=""><br><br>
<input type="submit" name="Кнопка" value="Добавить новость">
</form>
<form action="view.php" method=""> <br>
<input type="submit" name="Кнопка3" value="Вывести все новости из базы"><br>
<form action="view1.php" method=""> <br>
<input type="submit" name="Кнопка2" value="Редактировать новость"><br>
</form>
</form>
</body>
</html>-->
<?
$db=new PDO('mysql:host=localhost;dbname=triada','triada','191919');
$STH = $db->query("SELECT id,title,text,data from alex_team"); 
$STH->setFetchMode(PDO::FETCH_ASSOC); 
while($row = $STH->fetch()) {
   echo $row['id'] . ": ";
   echo $row['title'] . ": "; 
   echo $row['text'] . ", "; 
   echo $row['data'] . "<br>"; }

?>