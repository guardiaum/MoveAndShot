package com.johnymoreira.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUploadUtility {

    private DataOutputStream request;
    private final String boundary =  "*****";
    private final String crlf = "\r\n";
    private final String twoHyphens = "--";

    public void uploadSingleImage(String[] param) {
        try {

            File file = new File(param[2]);

            doUploadInBackground(file.getAbsolutePath(), file.getName(), param[1], param[0]);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doUploadInBackground(String path,
            String filename, String poiId, String service)
            throws IOException {

        HttpURLConnection conn = startRequest(service);

        addFormField("poi_id", poiId);

        addFilePart("file", new File(path));

        finish(conn);
    }

    private HttpURLConnection startRequest(String service) throws IOException {

        // inicia comunicacao com ws
        URL url = new URL(service);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // escreve cabecalho
        conn.setUseCaches(false);
        conn.setDoOutput(true); // indicates POST method
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + boundary);

        // realiza request
        request =  new DataOutputStream(conn.getOutputStream());
        return conn;
    }

    // adiciona o cimpo de formulario a ser passado junto da imagem
    public void addFormField(String name, String value)throws IOException  {
        request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" + name + "\""+ this.crlf);
        request.writeBytes("Content-Type: text/plain; charset=UTF-8" + this.crlf);
        request.writeBytes(this.crlf);
        request.writeBytes(value + this.crlf);
        request.flush();
    }

    // adiciona imagem como multipart
    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                fieldName + "\";filename=\"" +
                fileName + "\"" + this.crlf);
        request.writeBytes(this.crlf);
        byte[] bytes = getBytesFromFile(uploadFile);
        request.write(bytes);
    }

    public String finish(HttpURLConnection conn) throws IOException {

        String response ="";

        request.writeBytes(this.crlf);
        request.writeBytes(this.twoHyphens + this.boundary +
                this.twoHyphens + this.crlf);
        request.flush();
        request.close();

        // checks server's status code first
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            InputStream responseStream = new
                    BufferedInputStream(conn.getInputStream());
            BufferedReader responseStreamReader =
                    new BufferedReader(new InputStreamReader(responseStream));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();
            response = stringBuilder.toString();
            conn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return response;
    }

    public static byte[] getBytesFromFile(File file) throws FileNotFoundException, IOException{
        FileInputStream inputStream = new FileInputStream(file);
        long length = file.length();
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;

        while(offset < bytes.length &&
                (numRead = inputStream.read(bytes, offset,bytes.length-offset))>=0){
           offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        inputStream.close();
        return bytes;
    }

}
