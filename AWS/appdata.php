<?php
  $host = 'localhost';
  $user = 'root';
  $pw = 'rmk1324354657';
  $dbName = 'creativeplatform';

  $conn = new mysqli($host, $user, $pw, $dbName);
  if($conn) {
    $sql = "SELECT D.number, D.place, D.date FROM appDB U, userData D WHERE U.id = D.userId && nickname = '".$_POST['Nickname']."';";
    $result = mysqli_query($conn, $sql);
    if(mysqli_num_rows($result) > 0) {
      echo "Select Success";
      while($row = mysqli_fetch_assoc($result)) {
        echo $row['number'];
        echo ",".$row['place'];
        echo ",".$row['date'];
        echo '#';
      }
    }
    else echo "0 results";
    mysqli_close($conn);
  }
  else {
    echo "MySQL Failed";
  }
?>
