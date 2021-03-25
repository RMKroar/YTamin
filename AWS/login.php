<?php
  $host = 'localhost';
  $user = 'root';
  $pw = 'rmk1324354657';
  $dbName = 'creativeplatform';

  $conn = new mysqli($host, $user, $pw, $dbName);
  if($conn) {
    $sql = "SELECT nickname, password FROM appDB WHERE nickname = '".$_POST['Nickname']."' AND password = '".$_POST['Password']."';";
    $result = mysqli_query($conn, $sql);
    if(mysqli_num_rows($result) > 0) {
      echo "Login Success";
    }
    else echo "Login Failure";
    mysqli_close($conn);
  }
  else {
    echo "MySQL Failed";
  }
?>
