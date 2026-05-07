<?php

require_once "../config/db.php";

$data = json_decode(file_get_contents("php://input"), true);

$id_usuario = $data["id_usuario"] ?? 0;

if ($id_usuario == 0) {
    echo json_encode([
        "success" => false,
        "message" => "ID de usuario inválido"
    ]);
    exit;
}

try {

    $sql = "
        SELECT
            a.id_asignacion,
            o.id_obra,
            o.nombre,
            o.direccion,
            o.latitud,
            o.longitud,
            o.radio_permitido
        FROM asignacion a
        INNER JOIN obra o ON a.id_obra = o.id_obra
        WHERE a.id_usuario = :id_usuario
        ORDER BY a.id_asignacion DESC
        LIMIT 1
    ";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ":id_usuario" => $id_usuario
    ]);

    $obra = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$obra) {
        echo json_encode([
            "success" => false,
            "message" => "No hay obra asignada"
        ]);
        exit;
    }

    echo json_encode([
        "success" => true,
        "obra" => [
            "id_obra"        => (int)$obra["id_obra"],
            "nombre"         => $obra["nombre"],
            "direccion"      => $obra["direccion"] ?: "Sin dirección",
            "latitud"        => (float)$obra["latitud"],
            "longitud"       => (float)$obra["longitud"],
            "radio_permitido"=> (int)$obra["radio_permitido"]
        ]
    ]);

} catch (Exception $e) {

    http_response_code(500);

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}