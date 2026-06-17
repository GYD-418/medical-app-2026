package com.example.yiliaoapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    // 毕设演示默认地址，后续替换成你的 Spring Boot 服务地址。
    private static final String BASE_URL = "http://10.35.213.188:8081/";

    private static volatile SyncApiService syncApiService;

    private ApiClient() {
    }

    public static SyncApiService syncApi() {
        if (syncApiService == null) {
            synchronized (ApiClient.class) {
                if (syncApiService == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    syncApiService = retrofit.create(SyncApiService.class);
                }
            }
        }
        return syncApiService;
    }
}
