<?
 echo $_GET['title']."<br>";
 echo $_GET['text']."<br>";
 echo $_GET['date']."<br>";
 $data = array( 
              'title' => $_GET['title'], 
              'text' => $_GET['text'],
              'data' => $_GET['date']
        ); 

$db=new PDO('mysql:host=localhost;dbname=triada','triada','191919');
$STH = $db->prepare("INSERT INTO alex_team ( title,text,data  ) values ( :title, :text, :data )");
$STH->execute( $data);
?>