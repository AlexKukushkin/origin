<?
function autoload_class($class_name) {
    $directories = array(
        'controllers/',
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

require_once 'core/Request.php';
require_once 'core/Response.php';
require_once 'core/ResponseJson.php';
require_once 'core/db.php';
require_once 'core/model.php';
require_once 'core/view.php';
require_once 'core/controller.php';
require_once 'core/route.php';
Route::start(); 
?>