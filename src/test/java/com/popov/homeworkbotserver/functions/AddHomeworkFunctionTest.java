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

package com.popov.homeworkbotserver.functions;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import com.popov.homeworkbotserver.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.*;

/**
 * @author Anton Popov
 */
public class AddHomeworkFunctionTest {

    private static List<IFunction> functions = new ArrayList<>();
    private static IBot bot;
    private static TestMessageSender messageSender;
    private static String subjectNotFound = "";
    private static DatabaseReference ref;

    @BeforeClass
    public static void setUp() throws Exception {
        functions.add(new AddHomeworkFunction());
        messageSender = new TestMessageSender();

        ref = Main.initDB().child("test");
        final CountDownLatch localeLatch = new CountDownLatch(1);
        final boolean[] isFail = {false};
        ref.child("locale/subjectNotFound").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                subjectNotFound = snapshot.getValue(String.class);
                localeLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                isFail[0] = true;
                localeLatch.countDown();
            }
        });
        localeLatch.await(10, TimeUnit.SECONDS);
        assertFalse(isFail[0]);

        bot = new Bot(ref, functions, messageSender);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FirebaseApp.getApps().get(0).delete();
    }

    @Test
    public void isMatch() throws Exception {
        IFunction f = functions.get(0);
        assertTrue(f.isMatch("русский: упр. 159, 161"));
        assertTrue(f.isMatch("лит-ра: упр. 159, 161"));
        assertTrue(f.isMatch("Англ. яз.: упр. 159, 161"));
    }

    @Test
    public void execute() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        messageSender.callback = obj -> latch.countDown();
        final UUID uuid = UUID.randomUUID();
        bot.call("русский: " + uuid, "123456", "requestKeyExample");
        latch.await(10, TimeUnit.SECONDS);

        final String[] realValue = {""};
        final CountDownLatch resultLatch = new CountDownLatch(1);
        ref.child("homework/Русский").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> value = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {
                });
                realValue[0] = (String) value.get("text");
                resultLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                resultLatch.countDown();
                System.err.println("Fail to get real value: " + error.getDetails());
            }
        });
        resultLatch.await(10, TimeUnit.SECONDS);
        assertEquals(realValue[0], uuid.toString());
    }

    @Test
    public void executeSubjectNotFound() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] isSuccess = {""};
        messageSender.callback = obj -> {
            isSuccess[0] = String.valueOf(obj);
            latch.countDown();
        };
        bot.call("ыоал: выаыва", "123456", "requestKeyExample");
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(subjectNotFound, isSuccess[0]);
    }

}