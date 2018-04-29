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
import com.annimon.stream.function.Function;
import com.google.common.base.Functions;
import com.google.firebase.database.*;
import com.popov.homeworkbotserver.IFunction;
import com.popov.homeworkbotserver.IMessageSender;
import com.popov.homeworkbotserver.model.Attachment;
import com.popov.homeworkbotserver.model.Homework;
import com.popov.homeworkbotserver.model.PhotoAttachment;
import com.sun.corba.se.spi.legacy.interceptor.UnknownType;

import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Anton Popov
 */
public class GetAllHomeworkFunction implements IFunction {

    private final Pattern getAllHomeworkRegexp = Pattern.compile(".*((покаж|показ|расска)\\S*( о)? (вс\\S)?\\S* (задан\\S*|дз|д/з|д.з.|д.з|дз.|домаш\\S*|домаш\\S* раб\\S*|домаш\\S* задан\\S*)|что задал\\S*).*");

    @Override
    public boolean isMatch(String text) {
        return getAllHomeworkRegexp.matcher(text).matches();
    }

    @Override
    public void execute(String inputText, List<Attachment> attachments, final Object userId, final DatabaseReference ref, final IMessageSender messageSender, final Map<String, String> locale) {
        ref.child("homework").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get homework
                final Map<String, Map<String, Object>> homeworkMapCloud = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Map<String, Object>>>() {
                });
                final Map<String, Homework> homeworkMap = Stream.of(homeworkMapCloud)
                        .map(Function.Util.safe(stringMapEntry -> new SimpleEntry<>(stringMapEntry.getKey(), new Homework(stringMapEntry.getValue()))))
                        .withoutNulls()
                        .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
                ref.child("schedule").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot scheduleSnapshot) {
                        Calendar calendar = Calendar.getInstance();
                        int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                        if (today + 1 == Calendar.SATURDAY) today = 0;
                        // get schedule
                        List<String> schedule = scheduleSnapshot.child(String.valueOf(today)).getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        StringBuilder currentHomework = new StringBuilder(locale.get("currentHomeworkTitle") + "%0A");
                        StringBuilder otherHomework = new StringBuilder(locale.get("otherHomeworkTitle") + "%0A");
                        for (String name : homeworkMap.keySet()) {
                            final Homework homework = homeworkMap.get(name);
                            (schedule.contains(name) ? currentHomework : otherHomework)
                                    .append(locale.get("homeworkListPrefix"))
                                    .append(name).append(": %0A")
                                    .append(homework.text)
                                    .append("%0A");
                            if (!homework.attachments.isEmpty()) {
                                final String photoUrls = Stream.of(homework.attachments)
                                        .filter(attachment -> attachment instanceof PhotoAttachment)
                                        .limit(5)
                                        .map(attachment -> (PhotoAttachment) attachment)
                                        .map(PhotoAttachment::getLargestImageUrl)
                                        .collect(Collectors.joining(locale.get("photosAttachmentDelimiter"), locale.get("photosAttachmentText"), "%0A"));
                                (schedule.contains(name) ? currentHomework : otherHomework).append(photoUrls);
                            }
                        }
                        messageSender.sendMessage(currentHomework.append(otherHomework).toString(), userId);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        messageSender.sendMessage(locale.get("errorString") + databaseError.getMessage(), userId);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                messageSender.sendMessage(locale.get("errorString") + databaseError.getMessage(), userId);
            }
        });
    }
}