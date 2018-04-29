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

import com.google.firebase.database.*;
import com.popov.homeworkbotserver.IFunction;
import com.popov.homeworkbotserver.IMessageSender;
import com.popov.homeworkbotserver.model.Attachment;
import com.popov.homeworkbotserver.model.Homework;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anton Popov
 */
public class AddHomeworkFunction implements IFunction {

    private final Pattern addHomeworkRegexp = Pattern.compile("^([а-яА-Яa-zA-Z0-9-. ]+):(.*)$", Pattern.DOTALL);

    @Override
    public boolean isMatch(String text) {
        return addHomeworkRegexp.matcher(text).matches();
    }

    @Override
    public void execute(String inputText, final List<Attachment> attachments, final Object userId, final DatabaseReference ref, final IMessageSender messageSender, final Map<String, String> locale) {
        Matcher matcher = addHomeworkRegexp.matcher(inputText);
        matcher.find();
        final String subjectName = matcher.group(1);
        String homework = matcher.group(2).replace("\n", "%0A").trim();
        if (homework.matches("(\\s)*")) homework = null;

        final String finalHomework = homework;
        ref.child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> subjectsMap = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {
                });
                for (String name : subjectsMap.keySet()) {
                    String value = subjectsMap.get(name);
                    if (subjectName.matches(value)) {
                        ref.child("homework").child(name).setValue(new Homework(finalHomework, attachments.size() > 5 ? attachments.subList(0, 5) : attachments, new Date()))
                                .addOnSuccessListener(aVoid -> messageSender.sendMessage(locale.get("homeworkAdded"), userId))
                                .addOnFailureListener(e -> messageSender.sendMessage(locale.get("errorString") + e.getMessage(), userId));
                        return;
                    }
                }
                messageSender.sendMessage(locale.get("subjectNotFound"), userId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                messageSender.sendMessage(locale.get("errorString") + databaseError.getMessage(), userId);
            }
        });
    }
}
