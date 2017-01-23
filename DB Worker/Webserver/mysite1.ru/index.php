<?
	$a = -1;
	$b = -3;
	$c = 2;
?>

<html>
<head>
	<title> Уравнение!!! </title>
</head>
<body>

<?php 
	$D = $b * $b - 4 * $a * $c;
?>
<?php if($D < 0) :?>
	
	    <center><p><h1>Решения не существует!!!</h1></p></center>
		<?php else :?>
		<?php
		if($D == 0)
		{
			$x1 = $x2 = (-1*$b)/(2*$a);
		}
		if($D > 0)
		{
			$x1 = (-$b - sqrt($D))/(2*$a);
			$x2 = (-$b + sqrt($D))/(2*$a);
		}	   
		?>	
		<center><b><h1> Решение квадратного уравнения </h1></b></center>
		<br><br>
		
		<center><table border = "1" width = "500" height = "300" bgcolor = "red"  bordercolor = "black" cellpadding = "10" cellspacing = "0"> 
		<tr> 
			 <td><b><h2> Исходные данные </h2></b></td> 
			 <td><b><h2> Результаты </h2></b></td>
		</tr>
	 
		<tr>
			 <td><h2> A = <?php echo $a ?></h2></td> 
			 <td><h2> D = <?php echo $D ?></h2></td>
		</tr>

		<tr>
			 <td><h2> B = <?php echo $b ?></h2></td> 
			 <td><h2> X1 = <?php echo $x1 ?></h2></td>
		</tr>

		<tr>
			 <td><h2> C = <?php echo $c ?></h2></td> 
			 <td><h2> X2 = <?php echo $x2 ?></h2></td>
		</tr>

		</table></center>
		<br><br><br>		
<?php endif;?>
</body>
</html>
