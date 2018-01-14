<?php
	if (isset($_GET['id'])) {
		include('./config.inc.php');	
		$db = new mysqli($mysql_host, $mysql_user, $mysql_pass, $mysql_dbname);	
		$db->query('SET NAMES utf8');
			
		$id = $_GET['id'];
		$id = $db->real_escape_string($id);
		
		$query = 'SELECT answer FROM answers WHERE poll_id="'.$id.'";';
		$result = $db->query($query);

		if (empty($result)) {
			die();
		}
		$i = 0;
		while($res = $result->fetch_array())
		{
			$output[$i] = $res['answer'];
			$i++;
		}
		echo json_encode($output);
		$db->close();
	}
?>