<?class Controller_user extends Controller
{
	function show_login_form()
	{
		$this->view->generate('login_view.php', 'template_view.php');
	}
	function show_reg_form()
	{
	}		$this->view->generate('registration_view.php', 'template_view.php');

	function action_login()
	{
		//print_r($_POST);
		
		if(!$_SESSION['auth'])
		{
			if(count($_POST))
			{
				$db = $GLOBALS['db'];
				$data = array( 
				  'User_name' => $_POST['user_name'], 
				  'User_password' => $_POST['user_password']
				); 
				//print_r($data);
				$STH = $db->prepare('SELECT * from user  WHERE user_name = :User_name AND user_password = :User_password');
				$STH->execute($data);
				$data = $STH->fetch();
				if($data)
				{
					$_SESSION['auth'] = true;
					$_SESSION['user_id'] = $_POST['user_name'];
					$this->view->generate('main_view.php', 'template_view.php');
				}
				else
				{
					$this->show_reg_form();
				}
			}
			else
			{
				$this->show_login_form();
			}
		}
	}
	function action_logout()
	{
		if($_SESSION['auth'])
		{
			$_SESSION['auth'] = false;
			session_destroy();
			$data['type'] = "user_out";
		
			$this->show_login_form();
		}
		else
		{
		}
		//print_r("action_logout");
	}
	function action_registration()
	{
		$data = array( 
		  'User_name' => $_POST['user_name'], 
		  'User_password' => $_POST['user_password']
		); 

		if($data['User_password'] == $_POST['user_password_conf'])
		{
			$db = $GLOBALS['db'];
			$STH = $db->prepare('SELECT * from user  WHERE user_name = :User_name');
			$STH->execute(array('User_name' => $_POST['user_name']));
			$result = $STH->fetch();

			if($result)
			{
				$data['type'] = "created";
				$this->view->generate('registration_view.php', 'template_view.php', $data);
			}
			else
			{
				
				$STH = $db->prepare('INSERT INTO user (user_name,user_password) values (:User_name, :User_password)');
				$STH->execute($data);
				$data['type'] = "added";
				$this->view->generate('registration_view.php', 'template_view.php', $data);
			}
		}
		else
		{
			$data['type'] = "error";
			$this->view->generate('registration_view.php', 'template_view.php', $data);
		}
	}
	$url = 'http://localhost/';
	$curl = curl_init($url);
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	$curl_response = curl_exec($curl);
	if ($curl_response === false) {
	    $info = curl_getinfo($curl);
	    curl_close($curl);
	    die('������: ' . var_export($info));
	}
	curl_close($curl);
	$json = $curl_response;
	$decoded = json_decode($curl_response);
	var_export($decoded);
}


























?>