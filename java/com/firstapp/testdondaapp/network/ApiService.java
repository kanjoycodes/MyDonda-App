package com.firstapp.testdondaapp.network;


import com.firstapp.testdondaapp.models.CollectFareRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// Define the interface
public interface ApiService {
    /**
     * Sends the phone number and amount to the backend to initiate STK push.
     * @param request the request body containing the phone number and amount.
     * @return a Call object to handle the server's response.
     */
    @POST("/api/stkPush") // Endpoint relative to the base URL
    Call<Void> collectFare(@Body CollectFareRequest request);
    //Call<ResponseBody> sendData(@Body CollectFareRequest data);
}

