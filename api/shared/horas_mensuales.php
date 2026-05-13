<?php
header("Content-Type: application/json");
require_once "../config/db.php";

$data = json_decode(file_get_contents("php://input"), true);
$id_usuario = $data["id_usuario"] ?? 0;
$mes        = $data["mes"] ?? date("m");
$anio       = $data["anio"] ?? date("Y");

if (!$id_usuario) {
    echo json_encode(["success" => false, "message" => "ID inválido"]);
    exit;
}

try {
    $stmt = $pdo->prepare("
        SELECT
            dia,
            hora_entrada,
            hora_salida,
            horas_trabajadas
        FROM vista_horas_diarias
        WHERE id_usuario = :id
          AND MONTH(dia) = :mes
          AND YEAR(dia) = :anio
        ORDER BY dia DESC
    ");
    $stmt->execute([":id" => $id_usuario, ":mes" => $mes, ":anio" => $anio]);
    $filas = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $total = array_sum(array_column($filas, "horas_trabajadas"));
    $dias  = count($filas);
    $media = $dias > 0 ? round($total / $dias, 2) : 0;

    echo json_encode([
        "success"         => true,
        "total_horas"     => round($total, 2),
        "dias_trabajados" => $dias,
        "media_diaria"    => $media,
        "fichajes"        => $filas
    ]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}