<?
 echo $_GET['title']."<br>";
 echo $_GET['text']."<br>";
 echo $_GET['date']."<br>";
 $data = array( 
              'title' => $_GET['title'], 
              'text' => $_GET['text'],
              'date' => $_GET['date']
        ); 

$db=new PDO('mysql:host=localhost;dbname=site','kunfuman','root');
$STH = $db->prepare("INSERT INTO news ( title,text,date  ) values ( :title, :text, :date )");
$STH->execute( $data);
?>