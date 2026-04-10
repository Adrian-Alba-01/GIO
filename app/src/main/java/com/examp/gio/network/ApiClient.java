package com.examp.gio.network;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    public static String post(String urlString, String data) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writer.write(data);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            InputStream is;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

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
            return "ERROR";
        }
    }
}