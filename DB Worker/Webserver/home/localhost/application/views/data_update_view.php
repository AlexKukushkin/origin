<form action="/data/update" method="POST"> <br>
Номер <input type="text" name="id" size="20" maxlength="30" readonly value="<?=$data['id']?>"><br><br>
Название новости <input type="text" name="title" size="20" maxlength="30" value="<?=$data['title']?>"><br><br>
Описание <br>
<input  name="text" cols="15" rows="10" value="<?=$data['text']?>"><br><br>
Дата (день-месяц-год)<input type="date" name="data" size="20" maxlength="30" value="<?=$data['data']?>"><br><br>
<input type="submit" name="Кнопка1" value="Редактировать новость">
</form>

