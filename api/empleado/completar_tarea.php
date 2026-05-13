<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

require_once "../config/db.php";

$data = json_decode(file_get_contents("php://input"), true);

$id_tarea   = $data["id_tarea"]   ?? 0;
$id_usuario = $data["id_usuario"] ?? 0;

if ($id_tarea == 0 || $id_usuario == 0) {
    echo json_encode(["success" => false, "message" => "Datos inválidos"]);
    exit;
}

try {
    // Verificar que la tarea está asignada al usuario
    $check = $pdo->prepare("
        SELECT at.id FROM asignacion_tarea at
        WHERE at.id_tarea = :id_tarea AND at.id_usuario = :id_usuario
    ");
    $check->execute([":id_tarea" => $id_tarea, ":id_usuario" => $id_usuario]);

    if (!$check->fetch()) {
        echo json_encode(["success" => false, "message" => "Tarea no asignada a este usuario"]);
        exit;
    }

    $stmt = $pdo->prepare("
        UPDATE tarea SET estado = 'completada' WHERE id_tarea = :id_tarea
    ");
    $stmt->execute([":id_tarea" => $id_tarea]);

    echo json_encode(["success" => true]);

} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}