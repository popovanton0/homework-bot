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
import com.popov.homeworkbotserver.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Anton Popov
 */
public class GetAllHomeworkFunctionTest {

    private static List<IFunction> functions = new ArrayList<>();
    private static IBot bot;
    private static TestMessageSender messageSender;

    @Before
    public void setUp() throws Exception {
        functions.add(new GetAllHomeworkFunction());
        messageSender = new TestMessageSender();
        bot = new Bot(Main.initDB().child("test"), functions, messageSender);
    }

    @After
    public void tearDown() throws Exception {
        FirebaseApp.getApps().get(0).delete();
    }

    @Test
    public void isMatch() throws Exception {
        IFunction f = functions.get(0);
        assertTrue(f.isMatch("что задали "));
        assertTrue(f.isMatch("пожалуйста покажи всё дз"));
        assertTrue(f.isMatch("пожалуйста покажи всё домашнее задание"));
        assertTrue(f.isMatch("пожалуйста покажи все домашнее задание"));
        assertTrue(f.isMatch("пожалуйста покажи всю домашнюю работу"));
        assertTrue(f.isMatch("пожалуйста расскажи о домашней работе"));
    }

    @Test
    public void execute() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] isSuccess = {""};
        messageSender.callback = obj -> {
            isSuccess[0] = String.valueOf(obj);
            latch.countDown();
        };
        bot.call("что задали", "123456", "requestKeyExample");
        latch.await(10, TimeUnit.SECONDS);
        assertFalse(isSuccess[0].isEmpty());
    }

}