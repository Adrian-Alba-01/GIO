<?php
header("Content-Type: application/json");

$host = "192.168.1.10";
$user = "root";
$pass = "";
$db   = "GIO";

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    http_response_code(500);
    die(json_encode([
        "status" => "error",
        "message" => "Error de conexión a la base de datos"
    ]));
}

$conn->set_charset("utf8");
?>