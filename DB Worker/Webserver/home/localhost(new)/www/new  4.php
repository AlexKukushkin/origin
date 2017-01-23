<?php
	$result = mysql_query("SELECT * FROM users_data");
<center>
	<table border="0" bgcolor="#E0E4E0">
		<tr>
			<td>
				Общее количество зарегистрированных пользователей<br><br>
			</td>
			<td width='100' align='center'>
				$kol
			</td>
		</tr>
?>