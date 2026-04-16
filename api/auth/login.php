<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: ");

require_once "../config/db.php";

$data = json_decode(file_get_contents("php://input"), true);

$email = isset($data["email"]) ? trim($data["email"]) : "";
$contrasena = isset($data["contrasena"]) ? trim($data["contrasena"]) : "";

if ($email === "" || $contrasena === "") {
    echo json_encode([
        "success" => false,
        "message" => "Campos requeridos"
    ]);
    exit;
}

$stmt = $pdo->prepare("SELECT FROM usuario WHERE email = ?");
$stmt->execute([$email]);
$usuario = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$usuario) {
    echo json_encode([
        "success" => false,
        "message" => "Usuario no encontrado"
    ]);
    exit;
}

if ($usuario["contrasena"] !== $contrasena) {
    echo json_encode([
        "success" => false,
        "message" => "Contraseña incorrecta"
    ]);
    exit;
}

echo json_encode([
    "success" => true,
    "usuario" => [
        "id_usuario" => $usuario["id_usuario"],
        "nombre" => $usuario["nombre"],
        "email" => $usuario["email"],
        "rol" => $usuario["rol"]
    ]
]);