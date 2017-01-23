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