<html>	<head>		<title>ALEX KUKUSHKIN TEAM</title>		<link rel="stylesheet" type="text/css" href="/css/style.css" />		<script src="/js/jquery-1.6.2.js" type="text/javascript"></script>	</head>	<body>		<div id="wrapper">			<div id="header">				<div id="logo">					<img src="/images/logo.jpg" width="768" height="256" >				</div>			</div>			<br/>			<br/>			<br/>			<br/>			<br/>			<br/>						<?php 				if(check_user())					echo "<div id='page'><a href='/user/logout'>" .$_SESSION['user_id'] ." Выход</a></div>";			?>			<div id="page">				<div id="sidebar">									<div class="side-box">											<h3>Главное меню</h3>						<ul class="list">							<li class="first "><a href="/">Главная</a></li>							<li><a href="/services">Услуги</a></li>							<li><a href="/portfolio">Проекты</a></li>							<li class="last"><a href="/contacts">Контакты</a></li>						</ul>					</div>														</div>														<div id="content">					<div class="box">						<?php 							include 'application/views/'.$content_view;						?>					</div>					<br class="clearfix" />				</div>				<br class="clearfix" />			</div>												<div id="page-bottom">				<div id="page-bottom-left">					<h3>О Компании</h3>					    Aдрес: г.Ростов-на-Дону ул.Мечникова 154А				</div>				<br class="clearfix" />			</div>		</div>		<div id="footer">			<a href="/">ALEX KUKUSHKIN TEAM</a> &copy; 2014</a>		</div>	</body></html>