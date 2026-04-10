<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

require_once "../config/db.php";

$data  = json_decode(file_get_contents("php://input"), true);
$email  = $data["email"] ?? "";
$nombre = $data["nombre"] ?? "";

if (empty($email)) {
    echo json_encode(["success" => false, "message" => "Email vacío"]);
    exit;
}

// Buscar si ya existe el usuario
$stmt = $conn->prepare("SELECT id_usuario, nombre, email, rol FROM usuario WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    // Usuario ya existe → devolver sus datos
    $row = $result->fetch_assoc();
    echo json_encode(["success" => true, "usuario" => $row]);
} else {
    // Usuario nuevo → crearlo con rol empleado por defecto
    $contrasena = password_hash(uniqid(), PASSWORD_DEFAULT); // contraseña aleatoria
    $stmt2 = $conn->prepare("INSERT INTO usuario (nombre, email, contrasena, rol) VALUES (?, ?, ?, 'empleado')");
    $stmt2->bind_param("sss", $nombre, $email, $contrasena);

    if ($stmt2->execute()) {
        $id = $conn->insert_id;
        $usuario = [
            "id_usuario" => $id,
            "nombre"     => $nombre,
            "email"      => $email,
            "rol"        => "empleado"
        ];
        echo json_encode(["success" => true, "usuario" => $usuario]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al crear usuario"]);
    }
}

$conn->close();
?>