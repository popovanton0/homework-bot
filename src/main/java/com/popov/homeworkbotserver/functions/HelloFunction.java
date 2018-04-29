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

import com.google.firebase.database.DatabaseReference;
import com.popov.homeworkbotserver.IFunction;
import com.popov.homeworkbotserver.IMessageSender;
import com.popov.homeworkbotserver.model.Attachment;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Антон on 27.01.2018.
 */

public class HelloFunction implements IFunction {

    private final Pattern regexp = Pattern.compile(".*(привет|ghbdtn|помощ|инструкци|справка|помог|что дела|как (\\S* )?работае).*");

    @Override
    public boolean isMatch(String text) {
        return regexp.matcher(text).matches();
    }

    @Override
    public void execute(String inputText, List<Attachment> attachments, Object userId, DatabaseReference ref, IMessageSender messageSender, Map<String, String> locale) {
        messageSender.sendMessage(locale.get("instruction"), userId);
    }
}
