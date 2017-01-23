<?php

/*
Класс-маршрутизатор для определения запрашиваемой страницы.
> цепляет классы контроллеров и моделей;
> создает экземпляры контролеров страниц и вызывает действия этих контроллеров.
*/
class Route
{

	static function start()
	{

		$controller_name = 'Main';
		$action_name = 'index';
		
		$routes = explode('/', $_SERVER['REQUEST_URI']);
		// получаем имя контроллера
		if ( !empty($routes[1]) )
		{	
			$controller_name = $routes[1];
		}
		
		// получаем имя экшена
		if ( !empty($routes[2]) )
		{
			$action_name = $routes[2];
		}
		
		// добавляем префиксы
		$model_name = 'Model_'.$controller_name;
		$controller_name = 'Controller_'.$controller_name;
		$action_name = 'action_'.$action_name;
		/*
		print_r($model_name);
		print_r($controller_name);
		print_r($action_name);
		*/
		
		switch ($controller_name) {
			case 'Controller_portfolio':
				if($_SESSION['auth'])
				{
					Route::Execute($model_name, $action_name, $controller_name);
				}
				else
				{
					Route::Execute('Model_user', 'action_login', 'Controller_user');
				}
				break;
			case 'Controller_api':
				Route::ExecuteAPI();
				break;
			default:
				Route::Execute($model_name, $action_name, $controller_name);
				break;
		}
	}
	static function ExecuteAPI()
	{
		$request = new Request();
		if (isset($_SERVER['REQUEST_URI'])) 
		{	
			$request->url_elements = explode('/', trim($_SERVER['REQUEST_URI'], '/api/'));
		}
		$request->method = strtoupper($_SERVER['REQUEST_METHOD']);
		switch ($request->method) {
			case 'GET':
				$request->parameters = $_GET;
			break;
			case 'POST':
				$request->parameters = $_POST;
			break;
		}
		if (!empty($request->url_elements)) {
		
			$model_file = 'Model_Portfolio.php';
			$model_path = "application/models/".$model_file;
			if(file_exists($model_path))
			{
				include "application/models/".$model_file;
			}
			$cont = "Controller_Portfolio";
			$action = strtolower($request->method);
			//print_r($action);
			$controller_file = strtolower($cont).'.php';
			$controller_path = "application/controllers/".$controller_file;
			if(file_exists($controller_path))
			{
				include "application/controllers/".$controller_file;
			}
			else
			{
				Route::ErrorPage404();
			}
			$controller = new $cont;
			$response_str = call_user_func_array(array($controller, $action), array($request));
			/*if(method_exists($cont, $action))
			{
				$res = $controller->$action($request);	
				print_r($res);
			}*/
			$response_obj = Response::create($response_str, $_SERVER['HTTP_ACCEPT']);
			echo $response_obj->render();
		}
	}
	static function Execute($model_name, $action_name, $controller_name)
	{
		$model_file = strtolower($model_name).'.php';
		$model_path = "application/models/".$model_file;
		if(file_exists($model_path))
		{
			include "application/models/".$model_file;
		}
		// подцепляем файл с классом контроллера
		$controller_file = strtolower($controller_name).'.php';
		$controller_path = "application/controllers/".$controller_file;
		if(file_exists($controller_path))
		{
			include "application/controllers/".$controller_file;
		}
		else
		{
			Route::ErrorPage404();
		}
		$controller = new $controller_name;
		$action = $action_name;
		
		if(method_exists($controller, $action))
		{
			$controller->$action();
		}
		else
		{
			Route::ErrorPage404();
		}
	}
	function ErrorPage404()
	{
        $host = 'http://'.$_SERVER['HTTP_HOST'].'/';
        header('HTTP/1.1 404 Not Found');
		header("Status: 404 Not Found");
		header('Location:'.$host.'404');
    }
    
}
