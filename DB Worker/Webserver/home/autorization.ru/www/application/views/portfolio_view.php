<h1>Портфолио</h1>
<p>
<table>
СУПЕР ВЫМЫШЛЕННЫЕ ПРОЕКТЫ!!!
<tr><td>Год</td><td>Проект</td><td>Описание</td></tr>
<?php

	while($row = $data->fetch())
	{
		echo '<tr><td>'.$row['date'].'</td><td>'.$row['title'].'</td><td>'.$row['text'].'</td></tr>';
	}
	
?>
</table>
</p>
