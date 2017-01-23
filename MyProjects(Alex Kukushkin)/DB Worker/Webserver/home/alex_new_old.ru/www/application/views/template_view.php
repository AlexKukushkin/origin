<!DOCTYPE html>
<html>
	<head>
		<title>ALEX TEAM THE BEST!!!</title>
		<link rel="stylesheet" type="text/css" href="/css/style.css" />
		<script src="/js/jquery-1.6.2.js" type="text/javascript"></script>
		
	</head>
	<body>
		<?php 
				if(check_user())
					echo "<div id='page'><a href='/user/logout'>" .$_SESSION['user_id'] ." Выход из кабинета</a></div>";
		?>
		<div id="wrapper">
			<div id="header">
				<div id="logo">
					<a href="/">ALEX</span> <span class="cms">TEAM</span></a>
				</div>
				<!--<div id="menu">
					<ul>
						<li class="first active"><a href="/">Главная</a></li>
						<li><a href="/services">Услуги</a></li>
						<li><a href="/portfolio">Портфолио</a></li>
						<li class="last"><a href="/contacts">Контакты</a></li>
					</ul>
					<br class="clearfix" />
				</div>
				-->
			</div>
			<div id="page">
				<div id="sidebar">
					<div class="side-box">
						<h3>Основное меню</h3>
						<ul class="list">
							<li class="first "><a href="/">Главная</a></li>
							<li><a href="/user/login">Вход на сайт</a></li>
							<li><a href="/user/logout">Выход</a></li>
							<li><a href="/services">Услуги</a></li>
							<li><a href="/portfolio">Портфолио</a></li>
							<li class="last"><a href="/contacts">Контакты</a></li>
						</ul>
					</div>
				</div>
				<div id="content">
					<div class="box">
						<?php include 'application/views/'.$content_view; ?>
					</div>
					<br class="clearfix" />
				</div>
				<br class="clearfix" />
			</div>
			<div id="page-bottom">
				<div id="page-bottom-content">
					<h3>О Компании</h3>
					<p>
						Траляляляляляляля
						Траляляляляляляля
						Траляляляляляляля
						Траляляляляляляля
						Траляляляляляляля
						Траляляляляляляля
						Траляляляляляляля  YAHOO
						Траляляляляляляля  YEH
						Траляляляляляляля  KAWABANGA!!!!
						Траляляляляляляля  HI, GIRLS!!!!
						Траляляляляляляля  OOOO
						Траляляляляляляля  HEY!!!!
						Траляляляляляляля
						Траляляляляляляля
						Траляляляляляляля
						Траляляляляляляля :))))))
						Траляляляляляляля :_____
						Траляляляляляляля :))))))
						Траляляляляляляля :))))))
						Траляляляляляляля
					</p>
				</div>
				<br class="clearfix" />
			</div>
		</div>
		<div id="footer">
			<a href="/">ALEX TEAM</a> &copy; 2014</a>
		</div>

	</body>
</html>