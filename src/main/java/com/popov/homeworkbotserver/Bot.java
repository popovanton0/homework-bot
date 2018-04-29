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

import com.google.firebase.database.*;
import com.popov.homeworkbotserver.model.Attachment;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Антон on 27.01.2018.
 */

public class Bot implements IBot {

    private final IMessageSender messageSender;
    private final DatabaseReference ref;
    private List<IFunction> functions = new ArrayList<>();
    private Map<String, String> locale = new HashMap<>();
    private boolean maintenance = false;
    private List<Long> maintenanceUserIds = new ArrayList<>();

    public Bot(final DatabaseReference reference, List<IFunction> functions, final IMessageSender messageSender) throws InterruptedException {
        this.functions.addAll(functions);
        this.messageSender = messageSender;
        this.ref = reference;

        ref.child("broadcastMsg").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot broadcastMsgSnapshot) {
                if (broadcastMsgSnapshot.getValue(String.class).equals(""))
                    return;
                // get all users
                ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot usersSnapshot) {
                        Map<String, Boolean> users = usersSnapshot.getValue(new GenericTypeIndicator<Map<String, Boolean>>() {
                        });

                        String msg = broadcastMsgSnapshot.getValue(String.class);
                        // send message to all users
                        for (String id : users.keySet())
                            messageSender.sendMessage(msg, id);
                        ref.child("broadcastMsg").setValue("");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        ref.child("broadcastMsg").setValue("");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ref.child("broadcastMsg").setValue("");
            }
        });

        ref.child("maintenance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                maintenance = dataSnapshot.child("state").getValue(Boolean.class);
                List<Long> cloudMaintenanceUserIds = dataSnapshot.child("userIds").getValue(new GenericTypeIndicator<List<Long>>() {
                });
                maintenanceUserIds.clear();
                maintenanceUserIds.addAll(cloudMaintenanceUserIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final CountDownLatch getLocaleLatch = new CountDownLatch(1);
        ref.child("locale").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> cloudLocale = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {
                });
                locale.clear();
                locale.putAll(cloudLocale);
                if (getLocaleLatch.getCount() == 1) getLocaleLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        getLocaleLatch.await();
    }

    @Override
    public void call(String inputText, String userId, String requestKey) {
        call(inputText, Collections.emptyList(), userId, requestKey);
    }

    @Override
    public void call(String inputText, List<Attachment> attachments, String userId, String requestKey) {
        if (maintenance && !maintenanceUserIds.contains(Long.valueOf(userId))) {
            messageSender.sendMessage(locale.get("maintenanceAnswer"), userId);
            ref.child("requests").child(requestKey).removeValue();
            return;
        }
        for (IFunction function : functions) {
            if (function.isMatch(inputText)) {
                ref.child("requests").child(requestKey).removeValue();
                try {
                    function.execute(inputText, attachments, userId, ref, messageSender, locale);
                } catch (Throwable e) {
                    messageSender.sendMessage(locale.get("errorString"), userId);
                }
                break;
            }
        }
    }
}