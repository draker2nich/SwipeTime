package com.draker.swipetime.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Утилита для проверки наличия интернет-соединения и мониторинга его состояния
 */
public class NetworkHelper {

    private static NetworkHelper instance;
    private final MutableLiveData<Boolean> isNetworkAvailable = new MutableLiveData<>();
    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;

    private NetworkHelper(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isNetworkAvailable.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                isNetworkAvailable.postValue(false);
            }
        };
        
        // Инициализация начального состояния
        isNetworkAvailable.postValue(isInternetAvailable());
        
        // Регистрация колбэка для мониторинга сети
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    /**
     * Получить экземпляр NetworkHelper
     * @param context контекст приложения
     * @return экземпляр NetworkHelper
     */
    public static synchronized NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Проверить доступность интернета в данный момент
     * @return true, если интернет доступен
     */
    public boolean isInternetAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Для Android 6.0 (API 23) и выше
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // Для более старых версий Android
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    /**
     * Получить LiveData для наблюдения за состоянием сети
     * @return LiveData с состоянием сети (true - доступна, false - недоступна)
     */
    public LiveData<Boolean> getNetworkAvailability() {
        return isNetworkAvailable;
    }

    /**
     * Освободить ресурсы сетевого монитора
     */
    public void cleanup() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception e) {
            // Игнорируем, если колбэк не был зарегистрирован
        }
    }
}
