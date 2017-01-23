<?php
class NewsController extends AbstractController
{
    protected $articles_file = './data/news.txt'; // в этом файле хранятся новости
    
    
    public function get($request)
    {
        $articles = $this->readArticles(); // получаем все новости
        switch (count($request->url_elements)) {
            case 1:
                return $articles; // возвращаем все новости, если не передан id новости (/news)
            break;
            case 2:
                $article_id = $request->url_elements[1];
                return $articles[$article_id]; // вовзращаем 1 новость по id (/news/id)
            break;
        }
    }
    
    public function post($request)
    {
       	// добавляем новость в текстовый файл
        $id = (count($articles) + 1);
        $articles = $this->readArticles();
        $article = array(
            'id' => $id,
            'title' => $request->parameters['title'],
            'content' => $request->parameters['content'],
            'published' => date('c')
        );
        $articles[] = $article;
        $this->writeArticles($articles);
        header('HTTP/1.1 201 Created');
        header('Location: api/news/'.$id);
        return 'News created.';
          
    }
    
    
    protected function readArticles()
    {
    	// получаем все новости из текстового файла
        $articles = unserialize(file_get_contents($this->articles_file));
        
        $this->writeArticles($articles);
        
        return $articles;
    }
    
    
    protected function writeArticles($articles)
    {
    	// запись новостей в текстовый файл
        file_put_contents($this->articles_file, serialize($articles));
        return true;
    }
}