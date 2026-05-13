<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

require_once "../config/db.php";

$data       = json_decode(file_get_contents("php://input"), true);
$id_usuario = $data["id_usuario"] ?? 0;

if ($id_usuario == 0) {
    echo json_encode(["success" => false, "message" => "Datos inválidos"]);
    exit;
}

try {
    $stmt = $pdo->prepare("
        SELECT t.id_tarea,
               t.nombre,
               t.descripcion,
               t.estado,
               t.fecha_inicio,
               t.fecha_fin
        FROM tarea t
        INNER JOIN asignacion_tarea at ON at.id_tarea = t.id_tarea
        WHERE at.id_usuario = :id_usuario
          AND t.estado IN ('pendiente', 'en_proceso')
        ORDER BY t.fecha_inicio ASC
    ");
    $stmt->execute([":id_usuario" => $id_usuario]);
    $tareas = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode(["success" => true, "tareas" => $tareas]);

} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}