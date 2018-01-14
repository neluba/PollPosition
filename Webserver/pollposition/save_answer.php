<?php
	if (isset($_GET['id']) && isset($_GET['name'])) {
		include('./config.inc.php');	
		$db = new mysqli($mysql_host, $mysql_user, $mysql_pass, $mysql_dbname);	
		$db->query('SET NAMES utf8');
			
		$id = $_GET['id'];
		$id = $db->real_escape_string($id);
		$name = $_GET['name'];
		$name = $db->real_escape_string($name);
		
		$query = 'UPDATE answers set votes = votes + 1 WHERE poll_id="'.$id.'" AND answer="'.$name.'";';
		if( $db->query($query) == true ) {
			echo "true";
		}
		$db->close();
	}

?>