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

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anton Popov
 */
public class ScheduleFunction implements IFunction {

    private final Pattern scheduleRegexp = Pattern.compile("((покаж|показ|\\S*демонстр)\\S* )?(расписан\\S*|спис\\S*)( звонк\\S*|урок\\S*)?( на завтр\\S*| на (всю )?неделю)?.*"); // do not edit without checking the code

    @Override
    public boolean isMatch(String text) {
        return scheduleRegexp.matcher(text).matches();
    }

    @Override
    public void execute(final String inputText, List<Attachment> attachments, final Object userId, final DatabaseReference ref, final IMessageSender messageSender, final Map<String, String> locale) {
        ref.child("schedule").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot scheduleSnapshot) {
                ref.child("timeSchedule").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot timeScheduleSnapshot) {
                        Matcher matcher = scheduleRegexp.matcher(inputText);
                        matcher.find();
                        Calendar calendar = Calendar.getInstance();
                        boolean isTomorrow = false;
                        boolean isWeek = false;
                        for (int i = 1; i <= matcher.groupCount(); i++)
                            if (matcher.group(i) != null && matcher.group(i).contains("на завтр")) {
                                isTomorrow = true;
                                break;
                            } else if (matcher.group(i) != null && matcher.group(i).contains("неделю")) {
                                isWeek = true;
                                break;
                            }
                        if (isWeek) {
                            List<List<String>> weekSchedule = scheduleSnapshot.getValue(new GenericTypeIndicator<List<List<String>>>() {
                            });

                            String answer = "";
                            for (int i1 = 0; i1 < weekSchedule.size(); i1++) {
                                List<String> schedule = weekSchedule.get(i1);
                                StringBuilder day = new StringBuilder();
                                for (int i = 0; i < schedule.size(); i++) {
                                    day.append(String.valueOf(i + 1)).append(". ").append(schedule.get(i)).append("%0A");
                                }
                                String dayName = "";
                                switch (i1) {
                                    case 0:
                                        dayName = "ПОНЕДЕЛЬНИК";
                                        break;
                                    case 1:
                                        dayName = "ВТОРНИК";
                                        break;
                                    case 2:
                                        dayName = "СРЕДА";
                                        break;
                                    case 3:
                                        dayName = "ЧЕТВЕРГ";
                                        break;
                                    case 4:
                                        dayName = "ПЯТНИЦА";
                                        break;
                                    case 5:
                                        dayName = "СУББОТА";
                                }
                                answer += dayName + "%0A" + day.toString() + "%0A";
                            }
                            messageSender.sendMessage(answer, userId);
                            return;
                        }

                        int day = calendar.get(Calendar.DAY_OF_WEEK) - (isTomorrow ? 1 : 2);
                        if (day < 0 || day > (Calendar.SATURDAY - 2)) {
                            messageSender.sendMessage(locale.get("noSubjectsToday"), userId);
                            return;
                        }
                        List<String> schedule = scheduleSnapshot.child(String.valueOf(day)).getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        List<String> timeSchedule = timeScheduleSnapshot.child(day == Calendar.SATURDAY - 2 ? "special" : "usual").getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        String subject;
                        String timeScheduleLine = "";
                        StringBuilder answer = new StringBuilder();
                        for (int i = 0; i < schedule.size(); i++) {
                            subject = schedule.get(i);
                            try {
                                timeScheduleLine = " (" + timeSchedule.get(i) + ")";
                            } catch (Exception e) {
                                timeScheduleLine = "";
                            }
                            answer.append(String.valueOf(i + 1)).append(". ").append(subject).append(timeScheduleLine).append("%0A");
                        }
                        messageSender.sendMessage(answer.toString(), userId);
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
