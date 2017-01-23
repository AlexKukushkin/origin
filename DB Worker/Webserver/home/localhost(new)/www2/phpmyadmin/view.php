<?
$db=new PDO('mysql:host=localhost;dbname=site','kunfuman','root');
$STH = $db->query('SELECT title,text,date from news'); 
$STH->setFetchMode(PDO::FETCH_ASSOC); 
while($row = $STH->fetch()) {
   echo $row['title'] . ": "; 
   echo $row['text'] . ", "; 
   echo $row['date'] . "<br>"; }

?>