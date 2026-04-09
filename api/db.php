<?php
$conn = new mysqli("localhost", "root", "", "GIO");

if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}
?>