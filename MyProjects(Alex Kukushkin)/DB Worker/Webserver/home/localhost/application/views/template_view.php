<!DOCTYPE html>
<html>
     <head>
	 	<meta http-equiv="content-type" content="text/html;charset=utf-8"/>
		<title>Albatros</title>
		 <link href="/css/menu.css" rel="stylesheet">
		<style>
		ul.hr li {
                 display: inline; 
                 margin-right: 5px;
                 padding: 3px;
				 font-size: 18pt;
				 }
				 </style>
	</head>
	<body>
	<?php 
				if(check_user())
					echo "<div id='page'><a href='/user/logout'>" .$_SESSION['user_id'] ."Выход</a></div>";
			?>
						<ul id="mymenu">
        <li><a href="/">Главная</a></li>
        <li><a href="/services">Услуги</a></li>
		<li><a href="/portfolio">Портфолио</a></li>
        <li><a href="/data">Новости</a></li>
		<li><a href="/contacts">Контакты</a></li>	
            </ul>
        <br></br>
		<br></br>
		<br></br>
		<div class="row-fluid">
		   <div style="margin-left: 45px;" class="span12">
				<?php include 'application/views/'.$content_view; ?>
			       </div>   
                </div>   				
					
	</body>
</html>

