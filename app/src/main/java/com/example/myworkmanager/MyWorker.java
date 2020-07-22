package com.example.myworkmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class MyWorker extends Worker {

    private static final String TAG = MyWorker.class.getSimpleName();
    private static final String API_KEY = "YOUR_API_KEY";
    public static final String CITY = "YOUR_CITY";
    private Result resultStatus;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters){
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        String dataCity = getInputData().getString(CITY);
        Result status = getCurrentWeather(dataCity);
        return status;
    }

    private Result getCurrentWeather(final String city){
        Log.d(TAG, "getCurrentWeather : Mulai ...");
        SyncHttpClient syncHttpClient = new SyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY;
        Log.d(TAG, "getCurrentWeather: " + url);

        syncHttpClient.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    String currentWeather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempKelvin = jsonObject.getJSONObject("main").getDouble("temp");

                    double tempCelcius = tempKelvin - 273;
                    String temperature = new DecimalFormat("##.##").format(tempCelcius);
                    String title = "Current Weather in " + city;
                    String message = currentWeather + ", " + description + "with " + temperature + "celcius";
                    showNotification(title, message);

                    Log.d(TAG, "onSuccess: Selesai.....");
                    resultStatus = Result.success();
                } catch (Exception e){
                    showNotification("Get Current Weather Not Success",e.getMessage());
                    Log.d(TAG, "onSuccess: Gagal.....");
                    resultStatus = Result.failure();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showNotification("Get Current Weather Not Success",error.getMessage());
                Log.d(TAG, "onSuccess: Gagal.....");
                resultStatus = Result.failure();
            }
        });
        return resultStatus;
    }

    private static final int NOTIF_ID = 1;
    private static final String CHANNEL_ID = "channel_01";
    private static final String CHANNEL_NAME = "xalam_channel";

    private void showNotification(String title, String description){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notification.setChannelId(CHANNEL_ID);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        if (notificationManager != null){
            notificationManager.notify(NOTIF_ID, notification.build());
        }
    }
}
