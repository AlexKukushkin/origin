
<form action="/portfolio/update" method="POST"> <br>
Название проекта <input type="text" name="title" size="20" maxlength="30" value="<?=$data['title']?>"><br><br>
Описание <br>
<input  name="text" cols="15" rows="10" value="<?=$data['text']?>"><br><br>
Дата (день-месяц-год)<input type="text" name="date" size="20" maxlength="30" value="<?=$data['data']?>"><br><br>
<input type="submit" name="Кнопка1" value="Редактировать проект">
</form>

