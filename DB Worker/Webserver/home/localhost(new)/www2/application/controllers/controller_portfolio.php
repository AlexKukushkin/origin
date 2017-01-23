<?php

class Controller_Portfolio extends Controller
{

	function __construct()
	{
		$this->model = new Model_Portfolio();
		$this->view = new View();
	}
	
	function action_index()
	{
		$data = $this->model->get_data();		
		$this->view->generate('portfolio_view.php', 'template_view.php', $data);
	}
	function action_create()
	{
		if (isset($_POST)) {
			//здесь код добавлени€ записи в Ѕƒ и вывести сообщение что данные добавлены
		}
		else {
			$this->view->generate('portfolio_create.php', 'template_view.php');
		}
	}
}
