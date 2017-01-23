<?php

class Controller_User extends Controller
{
	function action_login()
	{
		session_start();
		if(isset($_SESSION['login']))
		{
			$login='Здравствуйте, '.$_SESSION['login'].'!';}
			// Проверяем, пусты ли переменные логина и id пользователя
			if(empty($_SESSION['login']) or empty($_SESSION['id']))
			{
				echo "<p style='margin-left:60px;'>Вы вошли на сайт, как гость</p>
					  <br> HTML-форма входа<br />Ссылка на форму регистрации"; 
			}
			else
			// Если не пусты, то
			{
				echo "<br /><br />Вы вошли на сайт, как ".$_SESSION['login']."<br><br />";
				echo ('Кнопка ВЫЙТИ'); 
			}
		}
	}
	
	function action_logout()
	{
		session_start();//открытие сессии
		unset($_SESSION['login']);//закрытие сессии по логину
		session_destroy();//удаление сессии
		header("Location: http://lora.in.ua/php-uroki/avtorizaciya/vhod.php");//Перенаправление на эту страницу после нажатия кнопки ВЫЙТИ
		/*$this->view->generate('logout_view.php', 'template_view.php');*/
	}
	
	function CheckLoginInDB($username,$password)
	{
		
	}
}
