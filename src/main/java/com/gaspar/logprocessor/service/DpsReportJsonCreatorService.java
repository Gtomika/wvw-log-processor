package com.gaspar.logprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaspar.logprocessor.model.UploadContentResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
@Slf4j
public class DpsReportJsonCreatorService extends AbstractJsonService {

    private final OkHttpClient client;

    @Value("${dps.report.api}")
    private String dpsReportApi;

    //stateful service
    @Getter
    private final List<String> permalinks = new ArrayList<>();

    public DpsReportJsonCreatorService(ObjectMapper mapper, SettingsService settingsService, OkHttpClient client) {
        super(mapper, settingsService);
        this.client = client;
    }

    //async
    public void generateJsonAsync(Path logFile, BiConsumer<String, Path> onSuccess, Consumer<Path> onFail) {
        log.debug("Getting JSON from dps.report API: {}", logFile);
        uploadLogToDpsReport(logFile, onSuccess, onFail);
    }

    //must upload log first, then JSON will be generated
    //async
    public void uploadLogToDpsReport(Path logFile, BiConsumer<String, Path> onSuccess, Consumer<Path> onFail) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(logFile);
            log.debug("Bytes of log {} have been read into memory", logFile);
        } catch (IOException e) {
            log.error("Failed to read bytes of log file: {}", logFile, e);
            onFail.accept(logFile);
        }

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", logFile.toFile().getName(), RequestBody.create(bytes))
                .build();

        Request request = new Request.Builder()
                .url(dpsReportApi + "/uploadContent?json=1&generator=ei&detailedwvw=true")
                .post(requestBody)
                .header("content-type", "multipart/form-data")
                .build();

        log.debug("Sending request to API: {}", request);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("Failed to upload file to dps.report API: {}", logFile, e);
                onFail.accept(logFile);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    UploadContentResponse uploadContentResponse = mapper.readValue(response.body().string(), UploadContentResponse.class);
                    log.debug("Successfully uploaded log '{}' to dps.report, permalink is: {}", logFile, uploadContentResponse.getPermalink());
                    savePermalink(uploadContentResponse.getPermalink());
                    getJsonFromDpsReport(logFile, uploadContentResponse, onSuccess, onFail);
                } else {
                    log.error("dps.report API gave unsuccessful response {} for log file: {}", response, logFile);
                    onFail.accept(logFile);
                }
            }
        });
    }

    private synchronized void savePermalink(String permalink) {
        permalinks.add(permalink);
    }

    //async
    private void getJsonFromDpsReport(
            Path logFile,
            UploadContentResponse uploadContentResponse,
            BiConsumer<String, Path> onSuccess,
            Consumer<Path> onFail
    ) {
        String getJsonUrl = "/getJson?id=" + uploadContentResponse.getId();
        Request request = new Request.Builder()
                .url(dpsReportApi + getJsonUrl)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("Failed to download JSON generated from log {}", logFile, e);
                onFail.accept(logFile);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    log.debug("JSON for log file {} acquired from dps.report API", logFile);
                    onSuccess.accept(response.body().string(), logFile);
                } else {
                    log.error("dps.report API gave unsuccessful response {} for log file: {}", response, logFile);
                    onFail.accept(logFile);
                }
            }
        });
    }

}
