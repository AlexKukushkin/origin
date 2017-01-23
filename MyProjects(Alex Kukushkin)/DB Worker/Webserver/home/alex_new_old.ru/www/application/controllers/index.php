<?php

// подключаем все php файлы (контролеры, модели)
function autoload_class($class_name) {
    $directories = array(
        'classes/',
        'classes/controllers/',
        'classes/models/'
    );
    foreach ($directories as $directory) {
        $filename = $directory . $class_name . '.php';
        if (is_file($filename)) {
            require($filename);
            break;
        }
    }
}
spl_autoload_register('autoload_class');

// Парсим исходный URL (выолняем роль роутера)
$request = new Request();
if (isset($_SERVER['REQUEST_URI'])) {
	
	    $request->url_elements = explode('/', trim($_SERVER['REQUEST_URI'], '/api/'));
}

// определяем метод, с помощью которого отправлен запрос
$request->method = strtoupper($_SERVER['REQUEST_METHOD']);
switch ($request->method) {
    case 'GET':
        $request->parameters = $_GET;
    break;
    case 'POST':
        $request->parameters = $_POST;
    break;
}

// роутер
if (!empty($request->url_elements)) {
    $controller_name = ucfirst($request->url_elements[0]) . 'Controller';
    if (class_exists($controller_name)) {
        $controller = new $controller_name;
        $action_name = strtolower($request->method);
        $response_str = call_user_func_array(array($controller, $action_name), array($request));
    }
    else {
        header('HTTP/1.1 404 Not Found');
        $response_str = 'Unknown request: ' . $request->url_elements[0];
    }
}
else {
    $response_str = 'Unknown request';
}

// вовзращаем ответ на запрос клиенту
$response_obj = Response::create($response_str, $_SERVER['HTTP_ACCEPT']);
echo $response_obj->render();