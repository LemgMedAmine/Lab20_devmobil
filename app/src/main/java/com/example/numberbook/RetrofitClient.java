package com.example.numberbook;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Commentaire LEMGHILI Mohammed Amine: configuration centrale de Retrofit pour communiquer avec XAMPP.
public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.100.233/numberbook-api/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
