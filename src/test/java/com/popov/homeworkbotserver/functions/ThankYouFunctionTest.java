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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.popov.homeworkbotserver.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Anton Popov
 */
public class ThankYouFunctionTest {

    private static List<IFunction> functions = new ArrayList<>();
    private static IBot bot;
    private static TestMessageSender messageSender;
    private static String thankYouLocale = "";

    @BeforeClass
    public static void setUp() throws Exception {
        functions.add(new ThankYouFunction());
        messageSender = new TestMessageSender();

        final DatabaseReference ref = Main.initDB().child("test");
        final CountDownLatch localeLatch = new CountDownLatch(1);
        final boolean[] isFail = {false};
        ref.child("locale/thankYou").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                thankYouLocale = snapshot.getValue(String.class);
                localeLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                isFail[0] = true;
                localeLatch.countDown();
            }
        });
        localeLatch.await();
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
        assertTrue(f.isMatch("спасибо"));
        assertTrue(f.isMatch("спасибочки"));
        assertTrue(f.isMatch("спасибки"));
        assertTrue(f.isMatch("спс"));
    }

    @Test
    public void execute() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] isSuccess = {""};
        messageSender.callback = obj -> {
            isSuccess[0] = String.valueOf(obj);
            latch.countDown();
        };
        bot.call("спасибо", new ArrayList<>(0), "123456", "requestKeyExample");
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(thankYouLocale, isSuccess[0]);
    }
}