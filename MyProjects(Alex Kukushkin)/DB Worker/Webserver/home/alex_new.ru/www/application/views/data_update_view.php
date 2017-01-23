
<form action="/portfolio/update" method="POST"> <br>
Номер проекта<br>
<input type="text" name="id" size="20" maxlength="30" readonly value="<?=$data['id']?>"><br><br>
Название проекта <br>
<input type="text" name="Name" size="20" maxlength="30" value="<?=$data['title']?>"><br><br>
Описание <br>
<input  name="About" cols="15" rows="10" value="<?=$data['text']?>"><br><br>
Дата (день-месяц-год)<br>
<input type="date" name="Date" size="20" maxlength="30" value="<?=$data['date']?>"><br><br>
<input type="submit" name="adx_button" value="Редактировать">
</form>

