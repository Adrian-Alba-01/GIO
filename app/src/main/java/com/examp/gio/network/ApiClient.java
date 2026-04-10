package com.examp.gio.network;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    // Cambia esto a la IP de tu servidor si no usas el emulador
    private static final String BASE_URL = "http://10.0.2.2/GIO/api";

    public static String post(String endpoint, String data) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writer.write(data);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            InputStream is;
            if (responseCode >= 200 && responseCode < 300) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            if (is == null) return "{}";

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            reader.close();
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"Error de conexión\"}";
        }
    }
}