<?php
	$db = new PDO('mysql:host=localhost;dbname=triada','triada','191919');
	
	function check_user()
	{
		if($_SESSION['auth'])
			return true;
		else
			return false;
		

	}
?>