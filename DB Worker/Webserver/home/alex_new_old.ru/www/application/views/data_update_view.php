
<form action="/portfolio/update/?id=<?=$_GET['id']?>" method="POST"> <br>
Название проекта <input type="text" name="title" size="20" maxlength="30" value="<?=$data['title']?>"><br><br>
Описание <br>
<input  name="text" cols="15" rows="10" value="<?=$data['text']?>"><br><br>
Дата (день-месяц-год)<input type="date" name="date" size="20" maxlength="30" value="<?=$data['date']?>"><br><br>
<input type="submit" name="Кнопка1" value="Редактировать проект">
</form>

