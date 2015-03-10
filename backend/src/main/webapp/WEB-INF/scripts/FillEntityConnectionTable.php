<?php
/**
This Script Should Move all Connections in the Arachne Database to the New Connection Table
It Thereby Defines the Connections of things
**/
ini_set("memory_limit","800M");
$GLOBALS["DBUser"] ="root";
$GLOBALS["DBPassword"] ="tosso";
$GLOBALS["TargetTable"] = "SemanticConnection_new";
$GLOBALS["VerknuepfungTable"] = "`Sem_Verknuepfungen`";
$GLOBALS["MysqlServer"] = "localhost";
$GLOBALS["DataBase"] = "arachne";
//used for the Large Query thing
$GLOBALS["Insertstatement"] =array();
$GLOBALS["InserCSVFilename"] = "temp.csv";
$GLOBALS["InserCSVhandle"] ;

//Testing the Script

//Inserts into SQL
$GLOBALS["fillTable"]= true;

//Every Entry will be retrived , elsewise only the Abstract Tables will be interpreted
$GLOBALS["processEveryEntry"]= true;
//Put out every generated SQL Command 
//-------------ACHTUNG! produceces exreme output with "$GLOBALS["processEveryEntry"] = true;" !
$GLOBALS["ShowSQL"]= false;
//Put out every generated SQL Command 
//Put out generated SQL Commands for Compex Querys(No entity Lookups and Inserts) 
$GLOBALS["ShowStructualSQL"]=false;

//Show Errors
// 


$GLOBALS["EntityLookup"] = array();

echo  "Start : ".date("D M j G:i:s T Y"). "  \n";

mysqlConnect();
$GLOBALS["TempTargetTable"] = $GLOBALS["TargetTable"]."_new";

PrepareDatabase();



//Retrive all Possible Connections in the Database
$PossibleConnections = DatabaseRetriveAssoc(  "SELECT * FROM ".$GLOBALS["VerknuepfungTable"]." ;");
//Iterate over all Possible Connections
//var_dump($PossibleConnections);


//!!!!!!!!!TEST !!
/*$counterBreakTest =0;
$MaxTestITTER = 5;
*/
//!!!!!!!!!TEST !!
$GLOBALS["InserCSVhandle"] = fopen ($GLOBALS["InserCSVFilename"],"w");

foreach($PossibleConnections as $poscon){
	//Retrive actual existing connections

	$tab1 = strtolower($poscon["Teil1"]);
	$tab2 = strtolower($poscon["Teil2"]);
	
	//Precautions for Selfconnection
	$tab1 = str_replace("1","",$tab1);
	$tab2 = str_replace("2","",$tab2);
	//Precautions for Hierarchy Self Connections
	if($tab2 == "parent")
		$tab2 = $tab1;
	
	if($tab1 == "parent")
		$tab1 = $tab2;
	
	//DEBUG Datierungen
	/*
	if($tab1 == "datierung" ||$tab2 == "datierung"){
		$GLOBALS["ShowSQL"]= true;
		$GLOBALS["processEveryEntry"]= true;
	}else{
		$GLOBALS["processEveryEntry"]= false;
		$GLOBALS["ShowSQL"]= false;
	}*/
	
	$connectiontype = $poscon["StandardCIDOCConnectionType"];
	$sql= BuildSQLfromVerknuepfungstableentry($poscon);
	//Debug
	if($GLOBALS["ShowSQL"]||$GLOBALS["ShowStructualSQL"])
		var_dump($sql);
	
	//if Not its just Debug
	if($GLOBALS["processEveryEntry"]){
	
		$connetions = DatabaseRetriveList($sql);
		//var_dump(count($connetions));
		$dubbles =0;
		foreach($connetions as $con){
			
			//LookupEntityIDs
			$one = LookupEntityID($con["Part1"],$tab1);
			$two = LookupEntityID($con["Part2"],$tab2);
			
			if($one ===false ){
				echo "Key In Table Not Valid : IN Table ".$poscon["Tabelle"]. "  ". $tab1 ." Key ". $con["Part1"] ." \n";
				continue;
			}
			
			
			if($two ===false){
				echo "Key In Table Not Valid : IN Table ".$poscon["Tabelle"]. "  ". $tab2 ." Key ". $con["Part2"] ."\n";
				continue;
			}
			
			// DEBUG
			echo "Con: ", " ", $con;

			//Insert into new Table
			
			$additionalInfos1 = AdditionalInfos( $con,$tab2 );
			$additionalInfos2 = AdditionalInfos( $con,$tab1 );
			
			/*
			if($additionalInfdos =="")
				continue;
			*/
			//print_r($additionalInfdos);
			if($GLOBALS["fillTable"]){
				//INSERT 
				$result = InsertNewInfoIgnoreDublicates($one ,$two, $tab1,$tab2,$con["Part1"],$con["Part2"],$connectiontype, $additionalInfos1,$additionalInfos2);
				
				/*
				if($result !== true){
				
					if($result !="Duplicate")
						echo $result;
					else
						$dubbles++;
				}
					*/
					
			}
				
		}
		/*
		if($dubbles != 0){
			echo "Dublikate ". $poscon["Tabelle"].": ". $dubbles . "\n";
		}
		*/
		
	}
//!!!!!!!!!TEST!!!!
/*if($counterBreakTest > $MaxTestITTER)
	break;
else
	$counterBreakTest++;
*/
//!!!!!!!!!TEST!!!!
}
//ONE Last fill into the CSV
FillCSV();

fclose($GLOBALS["InserCSVhandle"]);
echo "successfully written csv file";

//The Fields That use it
$fields=		"( `"
	
		.$GLOBALS["TempTargetTable"]."`.`Source`, `"
		.$GLOBALS["TempTargetTable"]."`.`Target` , `"
		.$GLOBALS["TempTargetTable"]."`.`TypeSource`, `"
		.$GLOBALS["TempTargetTable"]."`.`TypeTarget`, `"
		.$GLOBALS["TempTargetTable"]."`.`ForeignKeySource` , `"
		.$GLOBALS["TempTargetTable"]."`.`ForeignKeyTarget` , `"
		.$GLOBALS["TempTargetTable"]."`.`CIDOCConnectionType` , `"
		.$GLOBALS["TempTargetTable"]."`.`AdditionalInfosJSON`) ";



//LOAD Everything from the CSV to The Database

$sql = "LOAD DATA INFILE '".getcwd()."/".$GLOBALS["InserCSVFilename"]."' IGNORE INTO TABLE `".$GLOBALS["TempTargetTable"]."`  FIELDS TERMINATED BY ';' ENCLOSED BY '\"' ESCAPED BY '\\\' LINES TERMINATED BY '\n' ".$fields." ;";

mysql_query($sql )or die ('Error: '.mysql_error ());;


FinishDatabase();
$GLOBALS["InserCSVhandle"] = fopen ($GLOBALS["InserCSVFilename"],"w");
fclose($GLOBALS["InserCSVhandle"]);

echo "successfully imported csv file";

// save number of connections per entity in arachneentitydegrees
$sql = "DROP TABLE IF EXISTS arachneentitydegrees";
mysql_query($sql) or die ('Error: '.mysql_error ());
$sql = "CREATE TABLE arachneentitydegrees (
  ArachneEntityID bigint(20) NOT NULL,
  Degree int(11) NOT NULL,
  PRIMARY KEY (ArachneEntityID),
  KEY Degree (Degree)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Stores the number of connections for each entity';";
mysql_query($sql) or die ('Error: '.mysql_error ());
$sql = "INSERT INTO arachneentitydegrees SELECT Source, COUNT(*) FROM SemanticConnection GROUP BY Source";
mysql_query($sql) or die ('Error: '.mysql_error ());

echo  "END : ".date("D M j G:i:s T Y"). "  \n";


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!END MAIN FUNCTION!!!!!!!!!!!!!!!!!!!!







//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Functions!!!!!!!!!!!!!!!!!!!!!!!!!!!




//This Extracts the Additional infos from the Request and Converts them to JSON
function AdditionalInfos($con, $prefix){
	$ret = ""; 
	foreach($con as $key => $value){
		if($key == "Part2"|| $key =="Part1")
			continue;
		
		if(!empty($value) && $value !== 0 && $value !== "" && $value !== " "){
			//var_dump($value);
			//TODO Json Encode noch scheisse!
			$ret[$prefix.".".$key] = $value;
		}
	}


	if($ret != ""){
		//Debug
		/*
		if(count($ret)>1){
			echo "--------------------\n";
			var_dump($ret);
			var_dump($tmp);
			var_dump(json_decode($tmp));
			echo "--------------------\n";
		}
		*/
		$out = addslashes(my_json_encode($ret));
		
		return $out;
	}else
		return $ret;
}




// Build Query that Gets all the Links described in a Verknuepfungstabellen entry
function BuildSQLfromVerknuepfungstableentry($verknentry){
	$Part1 = $verknentry["Teil1"];
	$Part2 = $verknentry["Teil2"];
	$Tabelle = $verknentry["Tabelle"];
	$Sconn = $verknentry["Selfconnection"];
	$sql = "";
	
	
	// Additional Fields - Reads the Field List and Converts it to SQL Parts  
	$attached ="";
	if(strpos($verknentry["Felder"],",") !== false  ){
		$fields = explode(",",$verknentry["Felder"]);	
		$requests = array();	
		foreach($fields as $field ){
		
			if($field != "Snr.|Seriennummer" && !empty($field)  ){
				$tmp = explode("|" ,$field);
				//print_r($tmp);
				$request[]= $tmp[0];
				//Use Alias
				//$request[]= $tmp[0]." as ".$tmp[1];

			}
			
		
		}
		
		$attached = ",". implode(",",$request);
		
	}
	
	
	//This Part Converts The Verknuepfungen Table to SQL requests
	//This handles Relation
	if( $verknentry["Befehl"] =="update" && $verknentry["Type"] == "UnDirected" ) {
		

		if(strtolower($Tabelle) == strtolower($Part1))
			$sql .="SELECT PS_".$Part1."ID as Part1,FS_".$Part2."ID as Part2 ".$attached." FROM `".$Tabelle."` WHERE `PS_".$Part1."ID` IS NOT NULL AND `FS_".$Part2."ID` IS NOT NULL ;";
		else
			$sql .="SELECT FS_".$Part1."ID as Part1, PS_".$Part2."ID as Part2 ". $attached." FROM `".$Tabelle."` WHERE `PS_".$Part2."ID` IS NOT NULL AND `FS_".$Part1."ID` IS NOT NULL ;";
		
		
		
	}else if($verknentry["Befehl"] =="insert" && $verknentry["Type"] == "UnDirected"){
		//This case handles Many to Many Realtion in a Reference Table

		$sql = "SELECT FS_".$Part1."ID as Part1, FS_".$Part2."ID as Part2 ".$attached."  FROM `".$Tabelle."` WHERE `FS_".$Part1."ID` IS NOT NULL AND `FS_".$Part2."ID` IS NOT NULL ;";
		//$sql = "";
	}else if($verknentry["Befehl"] =="update" && $verknentry["Type"] == "HierarchySelfconnection"){
			$sql = "SELECT PS_".$Part1."ID as Part1, ParentID  as Part2 ".$attached." FROM `".$Tabelle."` WHERE `PS_".$Part1."ID` IS NOT NULL AND `ParentID` IS NOT NULL ;";
		
	
	}else if($verknentry["Befehl"] =="insert" && $verknentry["Type"] == "UnDirectedSelfconnection"){
		$sql = "SELECT FS_".$Part1."ID as Part1, FS_".$Part2."ID as Part2 ".$attached." FROM `".$Tabelle."` WHERE `FS_".$Part1."ID` IS NOT NULL AND `FS_".$Part2."ID` IS NOT NULL ;";
	
	
	}else{
		// IF NON of the cases Above fit the Script threse Lines Below
	
		echo "THIS LINE COULDNT BE PROCESSED! \n\n";
		
		var_dump($verknentry);
		
		
	
	}

	//var_dump($sql);
	return $sql;

}


//Lookup Arachne Entity IDS in the DB
function LookupEntityID($ID,$Type){

	if($Type == "datierung" || $Type == "uri")
		return 0;

	if(!isset($GLOBALS["EntityLookup"][$Type]))
		FillEntityLookup($Type);
	
	
	if(isset($GLOBALS["EntityLookup"][$Type][$ID]))
		return $GLOBALS["EntityLookup"][$Type][$ID];
	else 
		return false;

}

function FillEntityLookup($Type){
	
 	
	$sql = "SELECT `ForeignKey`, `ArachneEntityID` FROM `arachneentityidentification` WHERE `arachneentityidentification`.`TableName` LIKE '".$Type."' ;";
	
	
	$result = mysql_query($sql);
	
	$GLOBALS["EntityLookup"][$Type] = array();
	
	
	while ($row = mysql_fetch_array($result, MYSQL_NUM)) {
		$GLOBALS["EntityLookup"][$Type][$row[0]] = $row[1];
		
	}
	


}




//Genererate Entry for CSV
/**
**@param $entity1 Arachne Entity ID 1
**@param $entity2 Arachne Entity ID 2
**@param $type1 Database Table Name 1
**@param $type2 Database Table Name 2
**@param $fkey1 Database Table Key 1
**@param $fkey2 Database Table Key 2
**@param $connectiontype Typing for the Connections If the The Typ of Realtion is not Bidirectional it Contains a / to Seperate 1 >2 from 2>1 Connection Type
**@param $AdditionalInfos This ist an JSON Element containing an Associative Array for Additional Information of a Connection
**@return True if everything is right, The String "Duplicate" id this key is a Doublicate Entry, else the Mysql Error Description  
**/

function InsertNewInfoIgnoreDublicates($entity1 ,$entity2, $type1,$type2,$fkey1,$fkey2,$connectiontype, $AdditionalInfos1, $AdditionalInfos2){
	$tempes = explode("/",$connectiontype);
	//First Direction Links
/*
//Quatsch da Datierung keine Entity ID hat Nachbessern
	if(empty($entity1) || empty($entity2) || empty ($type1) || empty($type2)|| empty($fkey1) || empty($fkey2) ){
		
		echo "Isuficient Infos to Create Table Entry\n";
		return;
	}*/
	
	//Start Inserting the Shit
	if(!empty($GLOBALS["Insertstatement"]) && is_array($GLOBALS["Insertstatement"]) && count($GLOBALS["Insertstatement"])> 5000){
		$temp = FillCSV();
		if($temp !== true)
			echo $temp;
	}
	
	
	$GLOBALS["Insertstatement"][] = '"'. $entity1.'";"'.$entity2.'";"'.$type1.'";"'.$type2.'";"'.$fkey1.'";"'.$fkey2.'";"'.$tempes[0].'";"'.$AdditionalInfos1.'"'."\n";
		

	//Second direction Links
	if(count($tempes) == 1)
		$tempes[1] = $tempes[0];
	$GLOBALS["Insertstatement"][] ='"'. $entity2.'";"'.$entity1.'";"'.$type2.'";"'.$type1.'";"'.$fkey2.'";"'.$fkey1.'";"'.$tempes[1].'";"'.$AdditionalInfos2.'"'."\n";
	

	return true;
}
// This is the Execution Function for InsertNewInfoIgnoreDublicates
function FillCSV(){

	foreach($GLOBALS["Insertstatement"] as $insert){
	
		fwrite($GLOBALS["InserCSVhandle"],$insert);
	
	}
	unset($GLOBALS["Insertstatement"]);
	$GLOBALS["Insertstatement"] = array();
	return true;
	
}


//////////////DATABASE Functions //////////////////


//Connect to mysql Server
function mysqlConnect(){
	//Mysql connection
	$db = mysql_connect($GLOBALS["MysqlServer"], $GLOBALS["DBUser"], $GLOBALS["DBPassword"]) or die ("Error: ".mysql_error());
	mysql_query("SET NAMES 'utf8'", $db);
	mysql_select_db($GLOBALS["DataBase"],$db);

}



//retrive Function 
function DatabaseRetriveAssoc(  $sql ){


	//Send Query
	$result = mysql_query($sql);
	//if result is empty
	if($result === false )
			return array();
	//Sum up Results
	$out = array();
	while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
		$out[] = $row;
		
	}

	//resut of the found Items
	mysql_free_result($result);
	return $out;	
}


function DatabaseRetriveList(  $sql ){


	//Send Query
	$result = mysql_query($sql);
	//if result is empty
	if($result === false )
		{
			echo "MAIN ERROR WHILE EXECUTING!!!!!\n\n";

			echo "MAIN ERROR IN: ".$sql. "\n";
			echo " ". mysql_error()."\n";
			echo "MAIN Table Entry missiterpreted!\n\n";
			return array();
		}
	//Sum up Results
	$out = array();
	while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
		$out[] = $row;
		
	}

	//resut of the found Items
	mysql_free_result($result);
	return $out;	
}
//Prepare temp_Table
function PrepareDatabase(){
	$sql = "DROP TABLE IF EXISTS `".$GLOBALS["TempTargetTable"]."`;";
	$allRight=mysql_query($sql);
	if($allRight === false)
	print_r("An Error Occured by removing the old temoprary Database Table: \n");
	if($GLOBALS["ShowSQL"] || $allRight === false)
		var_dump($sql);
		

	$sql = "CREATE TABLE IF NOT EXISTS `".$GLOBALS["TempTargetTable"]."` (
  `PS_SemanticConnectionID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Source` bigint(20) NOT NULL,
  `TypeSource` varchar(30) NOT NULL,
  `ForeignKeySource` bigint(20) NOT NULL,
  `Target` bigint(20) NOT NULL,
  `TypeTarget` varchar(30) NOT NULL,
  `ForeignKeyTarget` bigint(20) NOT NULL,
  `CIDOCConnectionType` varchar(255) NOT NULL,
  `AdditionalInfosJSON` text NOT NULL COMMENT 'This Is Additional infos in JSON',
  PRIMARY KEY (`PS_SemanticConnectionID`),
  UNIQUE KEY `UnifyThings` (  `TypeSource`,`ForeignKeySource`,`Source`,`ForeignKeyTarget`,`TypeTarget`,  `Target`,`CIDOCConnectionType`),
  KEY `EntityConnections` (`Source`,`Target`),
  KEY `OldConnections` (`TypeSource`,`ForeignKeySource`, `TypeTarget`,`ForeignKeyTarget`),
  KEY `ConnectionSemanticsIndex` (`CIDOCConnectionType`),
  KEY `SourceEntityID` (`Source`),
  KEY `TargetEntityID` (`Target`),
  KEY `InternalSource` (`TypeSource`,`ForeignKeySource`),
  KEY `InternalTarget` (`TypeTarget`,`ForeignKeyTarget`),
  FULLTEXT KEY `AdditionalInfosJSON` (`AdditionalInfosJSON`)
	) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COMMENT='This is a semantics enabled central CrossTable' ;";

	$allRight=mysql_query($sql);


	if($allRight === false)
	print_r("An Error Occured by Creating th temoprary Database Table: \n");
	if($GLOBALS["ShowSQL"] || $allRight === false)
		var_dump($sql);

}



function FinishDatabase(){
	$sql = "DROP TABLE IF EXISTS `".$GLOBALS["TargetTable"]."`;";
	$allRight=mysql_query($sql);
	
	
	if($allRight === false)
		print_r("An Error Occured by replacing the old Database Table: \n");
	if($GLOBALS["ShowSQL"] || $allRight === false)
		var_dump($sql);
	
	$sql ="ALTER TABLE ". $GLOBALS["TempTargetTable"]. " RENAME TO ".$GLOBALS["TargetTable"].";";
	$allRight=mysql_query($sql);

	if($allRight === false)
		print_r("An Error Occured by replacing the old Database Table: \n");
	if($GLOBALS["ShowSQL"] || $allRight === false)
		var_dump($sql);
	
		
	
	
	mysql_close();
}


//copied vom php.net

function my_json_encode($arr)
{
        //convmap since 0x80 char codes so it takes all multibyte codes (above ASCII 127). So such characters are being "hidden" from normal json_encoding
        array_walk_recursive($arr, function (&$item, $key) { if (is_string($item)) $item = mb_encode_numericentity($item, array (0x80, 0xffff, 0, 0xffff), 'UTF-8'); });
        return mb_decode_numericentity(json_encode($arr), array (0x80, 0xffff, 0, 0xffff), 'UTF-8');

}

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Deprecated but Useful !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

// Insert the Links into the New Table
//This Function is very slow bit it shows when there are dublicates in the Connection Tables 
/**
**@param $entity1 Arachne Entity ID 1
**@param $entity2 Arachne Entity ID 2
**@param $type1 Database Table Name 1
**@param $type2 Database Table Name 2
**@param $fkey1 Database Table Key 1
**@param $fkey2 Database Table Key 2
**@param $connectiontype Typing for the Connections If the The Typ of Realtion is not Bidirectional it Contains a / to Seperate 1 >2 from 2>1 Connection Type
**@param $AdditionalInfos This ist an JSON Element containing an Associative Array for Additional Information of a Connection
**@return True if everything is right, The String "Duplicate" id this key is a Doublicate Entry, else the Mysql Error Description  
**/

function InsertNewInfo($entity1 ,$entity2, $type1,$type2,$fkey1,$fkey2,$connectiontype, $AdditionalInfos1, $AdditionalInfos2){
	$tempes = explode("/",$connectiontype);
	//First Direction Links
/*
//Quatsch da Datierung keine Entity ID hat Nachbessern
	if(empty($entity1) || empty($entity2) || empty ($type1) || empty($type2)|| empty($fkey1) || empty($fkey2) ){
		
		echo "Isuficient Infos to Create Table Entry\n";
		return;
	}*/
	
	
	$sql = "INSERT INTO `".$GLOBALS["TempTargetTable"].
		"` SET `"
		.$GLOBALS["TempTargetTable"]."`.`Source`=".$entity1.", `"
		.$GLOBALS["TempTargetTable"]."`.`Target`=".$entity2." , `"
		.$GLOBALS["TempTargetTable"]."`.`TypeSource` = '".$type1."', `"
		.$GLOBALS["TempTargetTable"]."`.`TypeTarget` = '".$type2."', `"
		.$GLOBALS["TempTargetTable"]."`.`ForeignKeySource` = ".$fkey1." , `"
		.$GLOBALS["TempTargetTable"]."`.`ForeignKeyTarget` = ".$fkey2." , `"
		.$GLOBALS["TempTargetTable"]."`.`CIDOCConnectionType` = '".$tempes[0]."' , `"
		.$GLOBALS["TempTargetTable"]."`.`AdditionalInfosJSON` = '".$AdditionalInfos1.
		"' ;";
		
	if($GLOBALS["ShowSQL"])
		var_dump($sql);
	
	$temp = mysql_query($sql);

	if($temp === false){
	
		$error = mysql_error() ;
		
		if(strpos($error, "Duplicate")!== false)
			return "Duplicate";
		return "An Error Occured \nSQL: ".$sql."\nError: \n ". $error;
		
	
	}
	if($GLOBALS["ShowSQL"])
		var_dump($sql);
	//Second direction Links
	if(count($tempes) == 1)
		$tempes[1] = $tempes[0];
	$sql = "INSERT INTO `".$GLOBALS["TempTargetTable"].
		"` SET `"
		.$GLOBALS["TempTargetTable"]."`.`Source`=".$entity2.", `"
		.$GLOBALS["TempTargetTable"]."`.`Target`=".$entity1." , `"
		.$GLOBALS["TempTargetTable"]."`.`TypeSource` = '".$type2."', `"
		.$GLOBALS["TempTargetTable"]."`.`TypeTarget` = '".$type1."', `"
		.$GLOBALS["TempTargetTable"]."`.`ForeignKeySource` = ".$fkey2." , `"
		.$GLOBALS["TempTargetTable"]."`.`ForeignKeyTarget` = ".$fkey1." , `"
		.$GLOBALS["TempTargetTable"]."`.`CIDOCConnectionType` = '".$tempes[1]."' , `"
		.$GLOBALS["TempTargetTable"]."`.`AdditionalInfosJSON` = '".$AdditionalInfos2.
		"' ;";
	

	
	$temp = mysql_query($sql);
	if($temp === false){
	
		$error = mysql_error() ;
		
		if(strpos($error, "Duplicate")!== false)
			return "Duplicate";
		return "An Error Occured \nSQL: ".$sql."\nError: \n ". $error;
		
	
	}
	
	if($GLOBALS["ShowSQL"])
		var_dump($sql);
		
	return true;
}



?>
