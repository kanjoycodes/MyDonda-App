package com.firstapp.testdondaapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://c36d-41-89-18-2.ngrok-free.app"; // Replace with your backend's base URL
    private static Retrofit retrofit;

    /**
     * Returns the singleton Retrofit instance.
     * If not initialized, it initializes Retrofit with the base URL and Gson converter.
     */
    public static Retrofit getRetrofitInstance() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Base URL for your backend API
                    .addConverterFactory(GsonConverterFactory.create()) // JSON to Java object converter
                    .build();
        }
        return retrofit;
    }
}

