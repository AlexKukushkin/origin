<?$db=new PDO('mysql:host=localhost;dbname=triada','triada','191919');
$sql = "SELECT title,text,data from alex_team  WHERE id = 50"; 
//$STM = $db->prepare($sql);

//$STM->bindParam(':id',$_GET['id'], PDO::PARAM_INT);
$STH = $db->query('SELECT * from alex_team  WHERE id = '.$_GET['id']);
$STH->setFetchMode(PDO::FETCH_ASSOC); 
while($row = $STH->fetch()) {
   $id = $row['id'];
   $title = $row['title']; 
   $text = $row['text']; 
   $data = $row['data']; }
?>
<form action="core/data_update.php" method="GET"> <br>
Номер проекта <input type="integer" name="id" size="10" maxlength="10" value="<? echo $id ?>"><br><br>
Название проекта <input type="text" name="title" size="20" maxlength="30" value="<? echo $title ?>"><br><br>
Описание <br>
<textarea name="text" cols="15" rows="10" value="<? echo $text ?>"></textarea><br><br>
Дата (день-месяц-год)<input type="text" name="date" size="20" maxlength="30" value="<?echo $data ?>"><br><br>
<input type="submit" name="Кнопка1" value="Редактировать проект">
</form>

