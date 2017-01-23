<?

class Controller_api extends Controller
{
	function action_index()
	{
		$request = new Request();
		if (isset($_SERVER['REQUEST_URI'])) 
		{	
			$request->url_elements = explode('/', trim($_SERVER['REQUEST_URI'], '/api/'));
			print_r($request->url_elements);
		}
	}
	
	function action_portfolio()
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
		print_r($request->url_elements);
		print(" *");
		/*if (!empty($request->url_elements)) {
			$controller_name = ucfirst("Controller_P" . $request->url_elements[0]);
			print_r($controller_name);
			print(" ");
			if (class_exists($controller_name)) {
				$controller = new $controller_name;
				$action_name = strtolower($request->method);
				$response_str = call_user_func_array(array($controller, $action_name), array($request));
				print_r($response_str);
				print("* ");
			}
			else
			{
				print(" NO ");
			}
		}*/
	}
}
?>