<?php
  $host = 'localhost';
  $user = 'root';
  $pw = 'rmk1324354657';
  $dbName = 'creativeplatform';

  $conn = new mysqli($host, $user, $pw, $dbName);
  if($conn) {
    $sql = "SELECT U.nickname, SUM(D.number) AS value FROM appDB U, userData D WHERE U.id=D.userId GROUP BY U.nickname ORDER BY value DESC;";
    $result = mysqli_query($conn, $sql);
    if(mysqli_num_rows($result) > 0) {
      echo "Select Success";
      while($row = mysqli_fetch_assoc($result)) {
        echo $row['nickname'];
        echo ",".$row['value'];
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
