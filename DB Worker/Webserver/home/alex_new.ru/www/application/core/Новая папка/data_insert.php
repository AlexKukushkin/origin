<?php
 echo $_GET['id']."<br>";
 echo $_GET['title']."<br>";
 echo $_GET['text']."<br>";
 echo $_GET['date']."<br>";
 $data = array( 
			  'id' => $_GET['id'],	
              'title' => $_GET['title'], 
              'text' => $_GET['text'],
              'data' => $_GET['date']
        ); 
$db=new PDO('mysql:host=localhost;dbname=triada','triada','191919');
$STH = $db->prepare('INSERT INTO alex_team ( id, title,text,data  ) values ( :id, :title, :text, :data )');
$STH->execute( $data);
?>