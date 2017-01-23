<?php

class Controller_Portfolio extends Controller
{
	function __construct()
	{
		
		$this->model = new Model_Portfolio();
		$this->view = new View();
		
		$data = array( 
              'title' => $_POST['title'], 
              'text' => $_POST['text'],
              'date' => $_POST['date']
			); 
	}
	
	function action_index()
	{
		$data = $this->model->get_data();		
		$this->view->generate('portfolio_view.php', 'template_view.php', $data);
	}
	function action_create()
	{
		if (isset($_POST['title'])) {
			$STH = $db->prepare('INSERT INTO alex_team ( title,text,date  ) values ( :title, :text, :date )');
			$STH->execute( $data);
			echo "Данные успешно добавлены в БД.";
		}
		else {
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
              'date' => $_POST['date']
			); 
				$STH = $db->prepare('UPDATE alex_team SET title= :title, text= :text, date= :date WHERE id= :id');
				$STH->execute($data);

			echo "Данные успешно обновлены.";
		}
		else {
			
			$STH = $db->query('SELECT * from alex_team  WHERE id = '.$_GET['id']);
			$data = $STH->fetch();
			$this->view->generate('data_update_view.php', 'template_view.php',$data);
			
		}
	}
	
	function action_delete()
	{
		if (isset($_POST['title'])) {
				 $data = array( 
				  'id' => $_POST['id'],
				  'title' => $_POST['title'], 
				  'text' => $_POST['text'],
				  'date' => $_POST['date']
				); 
				$STH = $db->prepare('DELETE FROM alex_team WHERE id = :id');
				$STH->execute( $data);
				echo "Данные удалены из БД.";
			}
			else {
				$this->view->generate('data_delete_view.php', 'template_view.php');
			}
		}
}
