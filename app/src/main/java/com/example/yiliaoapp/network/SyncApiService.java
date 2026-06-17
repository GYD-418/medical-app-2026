package com.example.yiliaoapp.network;

import com.example.yiliaoapp.network.dto.ImageUploadResponse;
import com.example.yiliaoapp.network.dto.SyncRequest;
import com.example.yiliaoapp.network.dto.SyncResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SyncApiService {
    @POST("api/sync/task")
    Call<SyncResponse> uploadTask(@Body SyncRequest request);

    @Multipart
    @POST("api/image/upload")
    Call<ImageUploadResponse> uploadImage(
            @Part MultipartBody.Part file,
            @Part("inspectionId") RequestBody inspectionId,
            @Part("createdAt") RequestBody createdAt
    );
}
