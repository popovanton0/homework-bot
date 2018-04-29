/*
 * Copyright 2018 Anton Popov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.popov.homeworkbotserver;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import okhttp3.*;
import okhttp3.Callback;

import java.io.IOException;
import java.util.List;

/**
 * Created by Антон on 27.01.2018.
 */

public class VKMessageSender implements IMessageSender {

    private final OkHttpClient client = new OkHttpClient();
    private final String baseUrl;
    private final String accessToken;

    public VKMessageSender(String accessToken, String baseUrl) {
        this.accessToken = accessToken;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendMessage(String text, Object userId) {
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"),
                    "user_id=" + userId +
                            "&access_token=" + accessToken +
                            "&v=5.71" +
                            "&message=" + text);
            Request request = new Request.Builder()
                    .post(body)
                    .url(baseUrl + "/method/messages.send")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    public void sendMessage(String text, List<String> attachmentIds, Object userId) {
        try {
            if (attachmentIds.isEmpty()) {
                sendMessage(text, userId);
                return;
            }
            RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"),
                    "user_id=" + userId +
                            "&access_token=" + accessToken +
                            "&v=5.71" +
                            "&attachment=" + Stream.of(attachmentIds).collect(Collectors.joining(",")) +
                            "&message=" + text);
            Request request = new Request.Builder()
                    .post(body)
                    .url(baseUrl + "/method/messages.send")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                }
            });
        } catch (Exception ignored) {
        }
    }
}
