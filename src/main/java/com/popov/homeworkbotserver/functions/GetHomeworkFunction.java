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

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.firebase.database.*;
import com.popov.homeworkbotserver.IFunction;
import com.popov.homeworkbotserver.IMessageSender;
import com.popov.homeworkbotserver.model.Attachment;
import com.popov.homeworkbotserver.model.Homework;
import com.popov.homeworkbotserver.model.PhotoAttachment;
import com.sun.corba.se.spi.legacy.interceptor.UnknownType;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anton Popov
 */
public class GetHomeworkFunction implements IFunction {

    private final Pattern getHomeworkRegexp = Pattern.compile(".*что задали по ([а-яА-Яa-zA-Z0-9-. ]+).*|.*(задан\\S*|дз|д/з|д.з.|д.з|дз.|домаш\\S*|домаш\\S* раб\\S*|домаш\\S* задан\\S*) по ([а-яА-Яa-zA-Z0-9-. ]+).*");

    @Override
    public boolean isMatch(String text) {
        return getHomeworkRegexp.matcher(text).matches();
    }

    @Override
    public void execute(String inputText, List<Attachment> attachments, final Object userId, final DatabaseReference ref, final IMessageSender messageSender, final Map<String, String> locale) {
        Matcher matcher = getHomeworkRegexp.matcher(inputText);
        matcher.find();
        final String subjectName = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);

        ref.child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> subjectsMap = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {
                });
                for (String name : subjectsMap.keySet()) {
                    final String value = subjectsMap.get(name).toLowerCase();
                    if (subjectName.matches(value)) {
                        ref.child("homework").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Homework homework;
                                    try {
                                        homework = new Homework(dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {
                                        }));
                                    } catch (UnknownType unknownType) {
                                        messageSender.sendMessage(locale.get("errorString"), userId);
                                        return;
                                    }
                                    List<String> photoAttachmentIds = Stream.of(homework.attachments)
                                            .filter(value -> value instanceof PhotoAttachment)
                                            .map(attachment -> (PhotoAttachment) attachment)
                                            .limit(5)
                                            .map(attachment -> attachment.type + attachment.owner_id + "_" + attachment.id + (attachment.access_key == null ? "" : "_" + attachment.access_key))
                                            .collect(Collectors.toList());
                                    messageSender.sendMessage(homework.text, photoAttachmentIds, userId);
                                } else messageSender.sendMessage(locale.get("noHomework"), userId);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                messageSender.sendMessage(locale.get("errorString") + databaseError.getMessage(), userId);
                            }
                        });
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