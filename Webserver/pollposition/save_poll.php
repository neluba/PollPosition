<?php
	if (isset($_GET['name']) && isset($_GET['date']) && isset($_GET['beacon']) && isset($_GET['answers'])) {
		include('./config.inc.php');	
		$db = new mysqli($mysql_host, $mysql_user, $mysql_pass, $mysql_dbname);	
		$db->query('SET NAMES utf8');
			
		$error = false;
		
		$name = $_GET['name'];
		$name = $db->real_escape_string($name);
		$date = $_GET['date'];
		$date = $db->real_escape_string($date);
		$beacon = $_GET['beacon'];
		$beacon = $db->real_escape_string($beacon);
		
		$answers = $_GET['answers'];
		$answersArray = json_decode($answers);
		
		$query = 'INSERT INTO polls (name, beacon_name, date) VALUES("'.$name.'", "'.$beacon.'", "'.$date.'");';
		if( $db->query($query) == true ) 
			$error = false;
		else 
			$error = true;
		
		$query = 'SELECT _id FROM polls WHERE name="'.$name.'" AND date="'.$date.'" AND beacon_name="'.$beacon.'";';
		$result = $db->query($query);

		if (empty($result)) {
			die();
		}

		while($res = $result->fetch_array()) {
			$id = $res['_id'];
		}
		
		foreach($answersArray as $answer) {
			$query = 'INSERT INTO answers (poll_id, answer, votes) VALUES("'.$id.'", "'.$answer.'", "0");';
			if( $db->query($query) == true ) 
				$error = false;
			else 
				$error = true;
		}
		if(!$error)
			echo "true";
		
		$db->close();
	}

?>