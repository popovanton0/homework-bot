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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Anton Popov
 */
public class ScheduleFunctionTest {

    private static List<IFunction> functions = new ArrayList<>();
    private static IBot bot;
    private static TestMessageSender messageSender;
    
    @BeforeClass
    public static void setUp() throws Exception {
        functions.add(new ScheduleFunction());
        messageSender = new TestMessageSender();
        bot = new Bot(Main.initDB().child("test"), functions, messageSender);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FirebaseApp.getApps().get(0).delete();
    }

    @Test
    public void isMatch() throws Exception {
        IFunction f = functions.get(0);
        assertTrue(f.isMatch("покажи расписание"));
        assertTrue(f.isMatch("показать расписание"));
        assertTrue(f.isMatch("продемонстрируй расписание"));
        assertTrue(f.isMatch("продемонстрируй расписание на завтра"));
        assertTrue(f.isMatch("продемонстрируй расписание на неделю"));
        assertTrue(f.isMatch("продемонстрируй расписание на всю неделю"));
        assertTrue(f.isMatch("продемонстрируй список звонков"));
        assertTrue(f.isMatch("продемонстрируй список уроков"));
    }

    @Test
    public void execute() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] isSuccess = {""};
        messageSender.callback = obj -> {
            isSuccess[0] = String.valueOf(obj);
            latch.countDown();
        };
        bot.call("расписание", "123456", "requestKeyExample");
        latch.await(10, TimeUnit.SECONDS);
        assertNotNull(isSuccess[0]);
        assertTrue(isSuccess[0].length() > 10);
    }

    @Test
    public void executeTomorrow() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] isSuccess = {""};
        messageSender.callback = obj -> {
            isSuccess[0] = String.valueOf(obj);
            latch.countDown();
        };
        bot.call("расписание на завтра", "123456", "requestKeyExample");
        latch.await(10, TimeUnit.SECONDS);
        assertNotNull(isSuccess[0]);
        assertTrue(isSuccess[0].length() > 10);
    }

    @Test
    public void executeWeek() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] isSuccess = {""};
        messageSender.callback = obj -> {
            isSuccess[0] = String.valueOf(obj);
            latch.countDown();
        };
        bot.call("расписание на неделю", "123456", "requestKeyExample");
        latch.await(10, TimeUnit.SECONDS);
        assertNotNull(isSuccess[0]);
        assertTrue(isSuccess[0].length() > 300);
    }
}