<?
class Controller_Data extends Controller
{
	
function construct()
	{
	$data = array( 
              'title' => $_POST['title'], 
              'text' => $_POST['text'],
              'data' => $_POST['data']
			);
			}
function action_index()
{
$this->view->generate('data_view.php',
'template_view.php');
}
function action_add()
{
$db = $GLOBALS['db'];

	
		if (isset($_POST['title'])) {
			$data = array( 
				  'title' => $_POST['title'], 
				  'text' => $_POST['text'],
				  'data' => $_POST['data']
				); 
				$db = $GLOBALS['db'];

			$STH = $db->prepare('INSERT INTO news ( title,text,data  ) values ( :title, :text, :data )');
			$STH->execute( $data);
			echo "Данные успешно добавлены в БД.";
		}
		else 
		{
			$this->view->generate('data_insert_view.php', 'template_view.php');
		}
}
function action_update()
{
		$db = $GLOBALS['db'];
		if (isset($_POST['title'])) 
		{
			//$db=new PDO('mysql:host=localhost;dbname=site','root','');
			 $data = array( 
			  'id' => $_POST['id'],
              'title' => $_POST['title'], 
              'text' => $_POST['text'],
              'data' => $_POST['data']
			); 
				$STH = $db->prepare('UPDATE news SET title= :title, text= :text, data= :data WHERE id= :id');
				$STH->execute($data);

			echo "Данные успешно обновлены.";
		}
		else 
		{
			
			$STH = $db->query('SELECT * from news  WHERE id = '.$_GET['id']);
			$data = $STH->fetch();
			$this->view->generate('data_update_view.php', 'template_view.php',$data);
			
		}
}
function action_delete()
{
			if (isset($_POST['title'])) {

				$db = $GLOBALS['db'];
				$STH = $db->prepare('DELETE FROM news WHERE id = :id');
				$STH->execute(array('id' => $_POST['title']));
				echo "Данные удалены из БД.";
			}
			else {
				$this->view->generate('data_delete_view.php', 'template_view.php');
			}
}
}
?>