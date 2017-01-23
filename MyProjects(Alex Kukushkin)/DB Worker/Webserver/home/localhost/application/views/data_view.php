<?
$db=new PDO('mysql:host=localhost;dbname=site','root','');
$STH = $db->query("SELECT id,title,text,data from news"); 
$STH->setFetchMode(PDO::FETCH_ASSOC); 
while($row = $STH->fetch()) {
   echo $row['id'] . ": "; 
   echo $row['title'] . ": "; 
   echo $row['text'] . ", "; 
   echo $row['data'] . "<br>"; }

?>