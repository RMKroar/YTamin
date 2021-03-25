<?php
  $host = 'localhost';
  $user = 'root';
  $pw = 'rmk1324354657';
  $dbName = 'creativeplatform';

  $conn = mysqli_connect($host, $user, $pw, $dbName);
  if($conn) {
    $sql0 = "SELECT id FROM appDB WHERE nickname = '".$_POST['Nickname']."';";
    $result0 = mysqli_query($conn, $sql0);
    if(mysqli_num_rows($result0) > 0) echo "Signup Failure";
    else {
      $data_stream = "'".$_POST['Nickname']."','".$_POST['Password']."'";
      $query = "INSERT INTO appDB(nickname, password) VALUES (".$data_stream.")";
      $result = mysqli_query($conn, $query);
  
      if($result) echo "Signup Success";
      else echo "Signup Failure";
    }

    mysqli_close($conn);
  }
  else echo "MySQL Failure";
?>
