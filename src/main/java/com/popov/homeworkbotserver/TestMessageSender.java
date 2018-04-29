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


import java.util.List;

/**
 * @author Anton Popov
 */
public class TestMessageSender implements IMessageSender {

    public Callback callback;

    @Override
    public void sendMessage(String text, Object userId) {
        System.out.println(text);
        if (callback != null) callback.callback(text);
    }

    @Override
    public void sendMessage(String text, List<String> attachmentIds, Object userId) {
        System.out.println(text);
        if (!attachmentIds.isEmpty()) {
            System.out.println("Attachments:");
            for (String attachment : attachmentIds) System.out.println(attachment);
        }
        if (callback != null) callback.callback(text);
    }
}
