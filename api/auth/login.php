<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

require_once "../config/db.php";

// Leer json
$data = json_decode(file_get_contents("php://input"), true);
$email      = $data["email"] ?? "";
$contrasena = $data["contrasena"] ?? "";

// Validar que no vengan vacíos
if (empty($email) || empty($contrasena)) {
    echo json_encode(["success" => false, "message" => "Campos vacíos"]);
    exit;
}

// Consulta segura con prepared statement
$stmt = $conn->prepare("SELECT id_usuario, nombre, email, rol, contrasena FROM usuario WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $row = $result->fetch_assoc();

    if ($contrasena === $row["contrasena"]) {
        unset($row["contrasena"]); 
        echo json_encode(["success" => true, "usuario" => $row]);
    } else {
        echo json_encode(["success" => false, "message" => "Contraseña incorrecta"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Usuario no encontrado"]);
}

$stmt->close();
$conn->close();
?>