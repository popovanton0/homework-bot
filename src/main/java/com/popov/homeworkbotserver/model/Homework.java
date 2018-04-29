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

package com.popov.homeworkbotserver.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.sun.corba.se.spi.legacy.interceptor.UnknownType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Popov
 */
public class Homework {
    public String text;
    public long addDate;
    public List<Attachment> attachments = new ArrayList<>(0);

    public Homework(Map<String, Object> map) throws UnknownType {
        this.text = (String) map.get("text");
        this.addDate = (long) map.get("addDate");
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) map.get("attachments");
        if (attachments == null) attachments = new ArrayList<>(0);
        this.attachments = Stream.of(attachments)
                .map(Function.Util.safe(Attachment::fromMap))
                .withoutNulls()
                .collect(Collectors.toList());
    }

    public Homework(String text, Date addDate) {
        this.text = text;
        this.addDate = addDate.getTime();
    }

    public Homework(String text, List<Attachment> attachments, Date addDate) {
        this.text = text;
        this.attachments = attachments == null ? new ArrayList<>(0) : attachments;
        this.addDate = addDate.getTime();
    }

    @Override
    public String toString() {
        return "Homework{" +
                "text='" + text + '\'' +
                ", addDate=" + new Date(addDate) +
                '}';
    }
}