<?php
	if (isset($_GET['beacons']) && isset($_GET['time'])) {
		include('./config.inc.php');	
		$db = new mysqli($mysql_host, $mysql_user, $mysql_pass, $mysql_dbname);	
		$db->query('SET NAMES utf8');
			
		$beacons = $_GET['beacons'];
		$beaconArray = json_decode($beacons);
		
		$timeWindow = $_GET['time'];
		$timeWindow = $db->real_escape_string($timeWindow);

		// build the query to get all polls in a certain time window
		$query = 'SELECT * FROM polls WHERE date >= "'.$timeWindow.'" AND';
		for($i = 0; $i < count($beaconArray); $i++) {
			if($i == 0) 
				$query = $query.' beacon_name="'.$db->real_escape_string($beaconArray[$i]).'" ';
			else
				$query = $query.' OR beacon_name="'.$db->real_escape_string($beaconArray[$i]).'" ';
		}
		$query = $query.';';
		
		$result = $db->query($query);
		

		if (empty($result)) {
			die();
		}
		$i = 0;
		while($res = $result->fetch_array())
		{
			$output[$i] = $res['_id'];
			$i++;
			$output[$i] = $res['name'];
			$i++;
			$output[$i] = $res['beacon_name'];
			$i++;
			$output[$i] = $res['date'];
			$i++;
		}
		if(isset($output))
			echo json_encode($output);
		$db->close();
	}

?>