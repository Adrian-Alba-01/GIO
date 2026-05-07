package com.examp.gio.activities;

public class LocationUtils {

    public static float distanciaEnMetros(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        float[] resultados = new float[1];

        android.location.Location.distanceBetween(
                lat1,
                lon1,
                lat2,
                lon2,
                resultados
        );

        return resultados[0];
    }
}