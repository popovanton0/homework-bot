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

import com.google.firebase.database.DatabaseReference;
import com.popov.homeworkbotserver.model.Attachment;

import java.util.List;
import java.util.Map;

/**
 * Created by Антон on 27.01.2018.
 */

public interface IFunction {
    boolean isMatch(String text);
    void execute(String inputText, List<Attachment> attachments, Object userId, DatabaseReference ref, IMessageSender messageSender, Map<String, String> locale);
}
