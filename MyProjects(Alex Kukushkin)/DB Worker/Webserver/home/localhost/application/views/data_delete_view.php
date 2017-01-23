<!--
$STH = $db->query('SELECT * from alex_team  WHERE id = '.$_GET['id']);
$STH->setFetchMode(PDO::FETCH_ASSOC); 
while($row = $STH->fetch()) {
   $id = $row['id'];
   $title = $row['title']; 
   $text = $row['text']; 
   $data = $row['data']; }
-->
<form action="/data/delete" method="POST"> <br>
Название новости <input type="text" name="title" size="20" maxlength="30" value="<?=$data['data']?>"><br><br>
<input type="submit" name="Кнопка1" value="Удалить новость">
</form>