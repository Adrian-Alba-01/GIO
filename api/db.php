<?php
$conn = new mysqli("192.168.1.1", "root", "", "GIO");

if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}
?>