<?
	
	// получение всех новостей
	$url = 'http://localhost/api/news';
	$curl = curl_init($url);
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	$curl_response = curl_exec($curl);
	if ($curl_response === false) {
	    $info = curl_getinfo($curl);
	    curl_close($curl);
	    die('Ошибка: ' . var_export($info));
	}
	curl_close($curl);
	$json = $curl_response;
	$decoded = json_decode($curl_response);
	var_export($decoded);
	
	
	// добавление новости
	/*
	
	echo '<hr>';
	
	$url = 'http://localhost/api/news';
	$curl = curl_init($url);
	$curl_post_data = array(
	        'title' => 'News #4',
	        'content' => 'Content'
	);
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($curl, CURLOPT_POST, true);
	curl_setopt($curl, CURLOPT_POSTFIELDS, $curl_post_data);
	$curl_response = curl_exec($curl);
	if ($curl_response === false) {
	    $info = curl_getinfo($curl);
	    curl_close($curl);
	    die('Ошибка: ' . var_export($info));
	}
	curl_close($curl);
	$json = $curl_response;
	$decoded = json_decode($curl_response);
	var_export($decoded);
	*/
	
?>