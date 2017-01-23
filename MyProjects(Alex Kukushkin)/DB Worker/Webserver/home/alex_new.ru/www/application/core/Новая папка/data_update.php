<?php
		$a = array(
			array(
				'Year' => '2013',
				'Site' => 'http://DunkelBeer.ru',
				'Description' => 'Промо-сайт темного пива Dunkel от немецкого производителя Löwenbraü выпускаемого в России пивоваренной компанией "CАН ИнБев".'
			),

			array(
				'Year' => '2013',
				'Site' => 'http://ZopoMobile.ru',
				'Description' => 'Русскоязычный каталог китайских телефонов компании Zopo на базе Android OS и аксессуаров к ним.'
			),

			array(
				'Year' => '2013',
				'Site' => 'http://GeekWear.ru',
				'Description' => 'Интернет-магазин брендовой одежды для гиков.'
			),

			array(
				'Year' => '2013',
				'Site' => 'http://РоналВарвар.рф',
				'Description' => 'Промо-сайт мультфильма "Рональд-варвар" от норвежских режиссеров.'
			),

			array(
				'Year' => '2013',
				'Site' => 'http://TompsonTatoo.ru',
				'Description' => 'Персональный сайт-блог художника-татуировщика Алексея Кукушкина.'
			)
		);
//print_r ($a);
mysql_connect("localhost", "root", "")//параметры в скобках ("хост", "имя пользователя", "пароль")
or die("<p>Ошибка подключения к базе данных! " . mysql_error() . "</p>");


mysql_select_db("site")//параметр в скобках ("имя базы, с которой соединяемся")
 or die("<p>Ошибка выбора базы данных! ". mysql_error() . "</p>");
 
$id=$_GET['id'];
$title=trim($_GET['title']);
$text=trim($_GET['text']);
$data=trim($_GET['data']);

$update_sql = "UPDATE alex_team SET title='$title', text='$text', data='$data' WHERE id='$id'";
mysql_query($update_sql) or die("Ошибка вставки" . mysql_error());
echo '<p>Запись успешно обновлена!</p>';
?>

