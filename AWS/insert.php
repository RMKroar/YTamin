<?php
  $host = 'localhost';
  $user = 'root';
  $pw = 'rmk1324354657';
  $dbName = 'creativeplatform';

  $conn = mysqli_connect($host, $user, $pw, $dbName);
  if($conn) {
    $sql = "SELECT id FROM appDB WHERE nickname = '".$_POST['Nickname']."';";
    $result = mysqli_query($conn, $sql);
    if(mysqli_num_rows($result) > 0) {
      $row = mysqli_fetch_assoc($result);
      $userid = (int)$row['id'];

      $data_stream = "'".$row['id']."','".$_POST['Number']."','".$_POST['Place']."',NOW()";
      $query = "INSERT INTO userData(userId, number, place, date) VALUES (".$data_stream.");";
      echo $query;
      $result2 = mysqli_query($conn, $query);

      if($result2) echo "Insert Success";
      else echo "Insert Failure";
    }
    else echo "0 results";

    mysqli_close($conn);
  }
  else {
    echo "MySQL Failed";
  }
?>
