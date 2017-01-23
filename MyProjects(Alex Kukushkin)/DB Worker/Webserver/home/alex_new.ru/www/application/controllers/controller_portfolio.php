<?php

class Controller_Portfolio extends Controller
{
	function __construct()
	{
		
		$this->model = new Model_Portfolio();
		$this->view = new View();
		
		$lastID = 1;
	}
	
	function action_index()
	{
		$data = $this->model->get_data();		
		$this->view->generate('portfolio_view.php', 'template_view.php', $data);
	}
	function action_create()
	{
		$db = $GLOBALS['db'];
		if (isset($_POST['title'])) {
			$data = array( 
				  'title' => $_POST['title'], 
				  'text' => $_POST['text'],
				  'date' => $_POST['date']
				); 
			$STH = $db->prepare('INSERT INTO alex_team ( title, text, date  ) values ( :title, :text, :date )');
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
			//$db=new PDO('mysql:host=localhost;dbname=triada','triada','191919');
			 $data = array( 
			  'id' => $_POST['id'],
              'title' => $_POST['title'], 
              'text' => $_POST['text'],
              'date' => $_POST['date']
			); 
			print_r($_POST);
			$STH = $db->prepare('UPDATE alex_team SET title = :title, text = :text, date = :date WHERE id = :id');
			$STH->execute($data);
			echo "Данные успешно обновлены.";
		}
		else 
		{
			$id = $_GET['id'];
			$lastID = $id;
			
			$STH = $db->query('SELECT * from alex_team  WHERE id = '. $id);
			$data = $STH->fetch();
			print_r($GLOBALS['last_id']);
			$this->view->generate('data_update_view.php', 'template_view.php', $data);
		}
	}
	
	function action_delete()
	{
		if (isset($_POST['title'])) {

				$db = $GLOBALS['db'];
				$STH = $db->prepare('DELETE FROM alex_team WHERE id = :id');
				$STH->execute(array('id' => $_POST['title']));
				echo "Данные удалены из БД.";
			}
			else {
				$this->view->generate('data_delete_view.php', 'template_view.php');
			}
		}
    protected $articles_file = './data/news.txt'; // в этом файле хранятся новости
    
    
    public function get($request)
    {
        $articles = $this->readArticles(); // получаем все новости
		//print_r(count($request->url_elements));
        switch (count($request->url_elements)) {
            case 1:
				//print_r($articles);
				/*$data = array( 
				  'id' => $res[0],
				  'Name' => $res[1], 
				  'About' => $res[2],
				  'Date' => $res[3]
				); */
				//print_r(count($articles));
                return $articles; // возвращаем все новости, если не передан id новости (/news)
            break;
            case 2:
                $article_id = $request->url_elements[1];
				$res = $articles[$article_id];
				$data = array( 
				  'id' => $res[0],
				  'title' => $res[1], 
				  'text' => $res[2],
				  'date' => $res[3]
				); 
				//print_r($articles[$article_id]);
                return $articles[$article_id]; // вовзращаем 1 новость по id (/news/id)
            break;
        }
    }
    
    public function post($request)
    {
       	// добавляем новость в текстовый файл
        $article = array(
            'title' => $request->parameters['title'],
            'text' => $request->parameters['text'],
            'date' => date('c')
        );
        //$articles[] = $article;
        $this->writeArticles($article);
        //header('HTTP/1.1 201 Created');
        //header('Location: api/news/'.$id);
        return 'News created.';
          
    }
    
    
    protected function readArticles()
    {
    	// получаем все новости из текстового файла
		$db = $GLOBALS['db'];
		$arr = array();	
		$STH = $db->query('SELECT * from alex_team;');
        $id = 0;
		while ($row = $STH->fetch()) 
		{	
			$data = array( 
				  'id' => $row[0],
				  'title' => $row[1], 
				  'text' => $row[2],
				  'date' => $row[3]
				); 
			$arr[$id] = $data;
			$id++;
		}
		//print_r($arr);
        return $arr;
    }
    
    
    protected function writeArticles($articles)
    {
		$db = $GLOBALS['db'];
		$STH = $db->prepare('INSERT INTO alex_team ( title, text, date  ) values ( :title, :text, :date )');
		$STH->execute($articles);
        return true;
    }
}
