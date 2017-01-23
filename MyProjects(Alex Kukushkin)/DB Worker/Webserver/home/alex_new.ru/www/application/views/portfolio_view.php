<h1>Наши проекты</h1>
<p>
<table>
<img src="/images/1.jpg" width="295" height="256" >
<br/>
<br/>
<tr><td>Номер</td><td>Название</td><td>Описание</td><td>Дата</td></tr>
<?php

	foreach($data as $row)
	{
		echo '</td><td>'.$row['0'].'</td><td>'.$row['1'].'</td><td>'.$row['2'].'</td><td>'.$row['3'].'</td></tr>';
		
	}
	
?>
</table>
</p>
