<?php

class Controller_User extends Controller
{
	function action_login()
	{
		session_start();
		if(isset($_SESSION['login']))
		{
			$login='������������, '.$_SESSION['login'].'!';}
			// ���������, ����� �� ���������� ������ � id ������������
			if(empty($_SESSION['login']) or empty($_SESSION['id']))
			{
				echo "<p style='margin-left:60px;'>�� ����� �� ����, ��� �����</p>
					  <br> HTML-����� �����<br />������ �� ����� �����������"; 
			}
			else
			// ���� �� �����, ��
			{
				echo "<br /><br />�� ����� �� ����, ��� ".$_SESSION['login']."<br><br />";
				echo ('������ �����'); 
			}
		}
	}
	
	function action_logout()
	{
		session_start();//�������� ������
		unset($_SESSION['login']);//�������� ������ �� ������
		session_destroy();//�������� ������
		header("Location: http://lora.in.ua/php-uroki/avtorizaciya/vhod.php");//��������������� �� ��� �������� ����� ������� ������ �����
		/*$this->view->generate('logout_view.php', 'template_view.php');*/
	}
	
	function CheckLoginInDB($username,$password)
	{
		
	}
}
