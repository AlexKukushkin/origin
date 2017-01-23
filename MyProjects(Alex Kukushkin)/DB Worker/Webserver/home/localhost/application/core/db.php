<?php
$db=new PDO('mysql:host=localhost;dbname=site','root','');
	function check_user()
	{
		if($_SESSION['auth'])
			return true;
		else
			return false;
		

	}
?>
 
