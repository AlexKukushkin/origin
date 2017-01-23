<?
class Model_Portfolio extends Model
{
public function get_data()
{
return array(
array(
'Year' => '2014',
'Site' => 'http://donstu.ru',
'Description' => 'Сайт университета'
),
array(
'Year' => '2004',
'Site' => 'http://facebook.com',
'Description' => 'Социальная сеть.'
),
// добавьте еще несколько сайтов
);
}
}
?>