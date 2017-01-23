<?php
class Model_Portfolio extends Model
{
	public function get_data()
	{	
		$db = $GLOBALS['db'];
		$STH = $db->query('SELECT * from alex_team');
		$STH->setFetchMode(PDO::FETCH_ASSOC); 
		return $STH;
	}
	//----------------------------------------------------
	  public function get($request)
    {
		$db = $GLOBALS['db'];
        $articles = $db->readArticles(); // получаем все новости
        switch (count($request->url_elements)) {
            case 1:
                return $articles; // возвращаем все новости, если не передан id новости (/news)
            break;
            case 2:
                $article_id = $request->url_elements[1];
                return $articles[$article_id]; // возвращаем 1 новость по id (/news/id)
            break;
        }
    }
    
    public function post($request)
    {
        $article = array(
            'title' => $request->parameters['title'],
            'text' => $request->parameters['text'],
            'date' => date('c')
        );
        $articles[] = $article;
        $this->writeArticles($articles);
        header('HTTP/1.1 201 Created');
        //header('Location: api/news/'.$id);
        return 'News created.';
          
    }
    
    
    protected function writeArticles($articles)
    {
		$db = $GLOBALS['db'];
		$data = $articles;
    	$STH = $db->prepare('INSERT INTO alex_team ( title,text,date  ) values ( :title, :text, :date )');
		$STH->execute( $data);
    }
    
    
    protected function readArticles()
    {
    	$db = $GLOBALS['db'];
		$STH = $db->query('SELECT * from alex_team');
		$STH->setFetchMode(PDO::FETCH_ASSOC); 
		return $STH;
    }
}
?>
