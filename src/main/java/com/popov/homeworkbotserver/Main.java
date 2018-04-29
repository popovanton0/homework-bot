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
import com.annimon.stream.function.Function;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.popov.homeworkbotserver.functions.*;
import com.popov.homeworkbotserver.model.Attachment;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Антон on 27.01.2018.
 */

public class Main {

    private static List<IFunction> functions = new ArrayList<>();
    private static String vkApiBaseUrl = "https://api.vk.com";
    private static IMessageSender messageSender;

    public static void main(String[] args) throws Exception {
        sendGet();
        // Add all bot functions
        // Order matters
        functions.add(new HelloFunction());
        functions.add(new GetHomeworkFunction());
        functions.add(new GetAllHomeworkFunction());
        functions.add(new AddHomeworkFunction());
        functions.add(new ScheduleFunction());
        functions.add(new EasterEgg1Function());
        functions.add(new ThankYouFunction());
        functions.add(new NotUnderstandFunction());

        String dbBranch;
        try {
            dbBranch = args[0];
        } catch (Exception e) {
            throw new IllegalArgumentException("No dbBranch param provided");
        }

        try {
            if (args[1] != null) vkApiBaseUrl = args[1];
        } catch (Exception ignored) {
        }

        final DatabaseReference ref = initDB().child(dbBranch);

        System.out.println(ref.getDatabase().getApp().getName());
        // get vk access token
        final CountDownLatch vkTokenLatch = new CountDownLatch(1);
        ref.child("accessToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String accessToken = dataSnapshot.getValue(String.class);
                messageSender = new VKMessageSender(accessToken, vkApiBaseUrl);
                vkTokenLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        vkTokenLatch.await();

        final IBot bot = new Bot(ref, functions, messageSender);
        ref.child("requests").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                // on new message arrived to bot
                String text = snapshot.child("text").getValue(String.class).toLowerCase();
                String userId = String.valueOf(snapshot.child("userId").getValue(Long.class));
                String requestKey = snapshot.getKey();
                List<Map<String, Object>> attachmentsCloud = snapshot.child("attachments").getValue(new GenericTypeIndicator<List<Map<String, Object>>>() {
                });
                if (attachmentsCloud == null) attachmentsCloud = new ArrayList<>(0);
                List<Attachment> attachments = Stream.of(attachmentsCloud)
                        .map(Function.Util.safe(Attachment::fromMap))
                        .withoutNulls()
                        .collect(Collectors.toList());
                // add user to usersList
                ref.child("users").child(userId).setValue(true);
                bot.call(text, attachments, userId, requestKey);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        while (true) new CountDownLatch(1).await();
    }

    public static DatabaseReference initDB() throws IOException {
        // Fetch the service account key JSON file contents
        final InputStream serviceAccount = Main.class.getResourceAsStream("/serverSecret.json");
        // Initialize the app with a service account, granting admin privileges
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream("/usr/src/app/serverSecret.json")))
                .setDatabaseUrl("https://<PROJECT-ID>.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);
        return FirebaseDatabase.getInstance().getReference();
    }

    private static void sendGet() throws Exception {

        System.out.println("Start");
        String url = "http://detor.ambar.cloud/";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println("END");
        //print result
        System.out.println(response.toString());
    }
}
