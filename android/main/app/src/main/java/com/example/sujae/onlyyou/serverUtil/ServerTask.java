package com.example.sujae.onlyyou.serverUtil;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerTask extends AsyncTask<String, String, String> {

    private Activity    currentActivity;

    @Override
    protected String doInBackground(String... urls) {
        try {
            HttpURLConnection   con     = null;
            BufferedReader      reader  = null;

            try {
                URL url = new URL(urls[0]);
                con     = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Cache-Control", "no-cache");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "text/html");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.connect();

                //서버로 PUSH
                OutputStream outStream  = con.getOutputStream();
                BufferedWriter writer   = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write("push server");
                writer.flush();
                writer.close();

                //서버로부터 PULL
                InputStream stream  = con.getInputStream();
                reader              = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line         = "";
                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString(); //서버로부터 PULL 한 값을 리턴
            }
            catch (MalformedURLException e) { e.printStackTrace(); }
            catch (IOException e)           { e.printStackTrace(); }
            finally {
                if (con != null)        con.disconnect();
                try                     { if(reader != null) reader.close(); }
                catch (IOException e)   { e.printStackTrace(); }
            }
        }
        catch (Exception e) {e.printStackTrace(); }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }


}
