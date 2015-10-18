package gdg.hackatonapp.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import gdg.hackatonapp.Application;
import gdg.hackatonapp.entity.User;

@SuppressWarnings("deprecation")
public abstract class Web {

    private boolean hasConnection = false;
    private Context context;

    protected Gson gson;

    public Web(Context context) {
        this.context = context;
        this.hasConnection = (checkMobile() || checkWifi());
        this.initGson();
    }

    public boolean checkWifi() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (info != null) {
                    if (info.isConnected()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkMobile() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (info != null) {
                    if (info.isConnected()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isConnected() {
        return this.hasConnection;
    }

    private void initGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        builder.excludeFieldsWithoutExposeAnnotation();
        gson = builder.create();
    }

    private HttpURLConnection prepareConnection(HttpURLConnection connection) {
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-type", "application/json");
        return connection;
    }

    private Response response(HttpURLConnection connection) throws Exception {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        Response obj = new Response();
        obj.setCode(connection.getResponseCode());
        obj.setUrl(connection.getURL().toString());
        obj.setMethod(connection.getRequestMethod());
        obj.setMessage(buffer.toString());
        obj.setLocation(connection.getHeaderField("Location"));
        if (obj.getLocation() != null) {
            try {
                String[] parts = obj.getLocation().split("/");
                String id = parts[parts.length - 1];
                obj.setLocationId(Long.parseLong(id));
            } catch (Exception e) {

            }
        }
        Log.i(Application.tag, obj.toString());
        return obj;
    }

    public User get(String method, Type type) throws Exception {
        URL url = new URL(Application.webServices + method);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            prepareConnection(connection);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.disconnect();
            Response response = response(connection);
            if (response.getCode() == HttpURLConnection.HTTP_OK) {
                User entity = gson.fromJson(response.getMessage(), type);
                return entity;
            }
            throw new Exception("Error GET : " + response.getCode());
        } catch (Exception e) {
            Log.e(Application.tag, e.getMessage());
            throw e;
        }
    }

    public List<User> getList(String method, Type type) throws Exception {
        URL url = new URL(Application.webServices + method);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            prepareConnection(connection);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.disconnect();
            Response response = response(connection);
            if (response.getCode() == HttpURLConnection.HTTP_OK) {
                List<User> entities = gson.fromJson(response.getMessage(), type);
                return entities;
            }
            throw new Exception("Error GET : " + response.getCode());
        } catch (Exception e) {
            Log.e(Application.tag, e.getMessage());
            throw e;
        }
    }

    public long post(String method, Type type, User obj) throws Exception {
        URL url = new URL(Application.webServices + method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            String postData = gson.toJson(obj);
            prepareConnection(connection);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            DataOutputStream osw = new DataOutputStream(connection.getOutputStream());
            osw.write(postData.getBytes());
            osw.flush();
            Response response = response(connection);
            if (response.getCode() == HttpURLConnection.HTTP_CREATED) {
                return response.getLocationId();
            }
            throw new Exception("Error POST : " + response.getCode());
        } catch (Exception e) {
            Log.e(Application.tag, e.getMessage());
            throw e;
        } finally {
            connection.disconnect();
        }
    }

    public long put(String method, Type type, User obj) throws Exception {
        URL url = new URL(Application.webServices + method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            String postData = gson.toJson(obj);
            prepareConnection(connection);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("PUT");
            DataOutputStream osw = new DataOutputStream(connection.getOutputStream());
            osw.write(postData.getBytes());
            osw.flush();
            Response response = response(connection);
            if (response.getCode() == HttpURLConnection.HTTP_ACCEPTED) {
                return response.getLocationId();
            }
            throw new Exception("Error POST : " + response.getCode());
        } catch (Exception e) {
            Log.e(Application.tag, e.getMessage());
            throw e;
        } finally {
            connection.disconnect();
        }
    }

    public boolean delete(String method, Type type, User obj) throws Exception {
        URL url = new URL(Application.webServices + method + "/" + obj.getId());
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod("DELETE");
            connection.disconnect();
            Response response = response(connection);
            if (response.getCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
            throw new Exception("Error GET : " + response.getCode());
        } catch (Exception e) {
            Log.e(Application.tag, e.getMessage());
            throw e;
        }
    }


    public String post(File file) throws Exception {
        URL url = new URL(Application.webServices + "/api/files");
        Response response = this.upload(file, url, "POST");
        String location = response.getLocation().replace(response.getUrl(), "").replace("/", "");
        return location;
    }

    public String put(File file, String guid) throws Exception {
        URL url = new URL(Application.webServices + "/api/files/" + guid);
        Response response = this.upload(file, url, "PUT");
        String location = response.getLocation().replace(response.getUrl(), "").replace("/", "");
        return location;
    }

    public boolean delete(String guid) throws Exception {
        URL url = new URL(Application.webServices + "/api/files/" + guid);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod("DELETE");
            connection.disconnect();
            Response response = response(connection);
            if (response.getCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
            throw new Exception("Error DELETE : " + response.getCode());
        } catch (Exception e) {
            Log.e(Application.tag, e.getMessage());
            throw e;
        }
    }

    private Response upload(File file, URL url, String method) throws Exception {
        String boundary = "*****";
        String fileName = file.getName();
        String crlf = "\r\n";
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            writer.append("--" + boundary).append(crlf);
            writer.append("Content-Disposition: form-data; name=\"data\"; filename=\"" + fileName + "\"").append(crlf);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(crlf);
            writer.append("Content-Transfer-Encoding: binary").append(crlf);
            writer.append(crlf);
            writer.flush();
            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(crlf);
            writer.flush();
            writer.append(crlf).flush();
            writer.append("--" + boundary + "--").append(crlf);
            writer.close();
            connection.disconnect();
            Response response = response(connection);
            if ((response.getCode() == HttpURLConnection.HTTP_ACCEPTED) || (response.getCode() == HttpURLConnection.HTTP_CREATED)) {
                return response;
            }
            throw new Exception("Error POST/PUT : " + response.getCode());
        } catch (IOException e) {
            throw e;
        }
    }
}
