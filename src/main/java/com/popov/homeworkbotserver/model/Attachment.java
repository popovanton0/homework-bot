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

import com.sun.corba.se.spi.legacy.interceptor.UnknownType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Popov
 */
public class Attachment {
    public String type;

    Attachment() {
    }

    public static Attachment fromMap(Map<String, Object> attachmentMap) throws UnknownType {
        final String type = (String) attachmentMap.get("type");
        switch (type) {
            case "photo":
                Map<String, Object> params = (Map<String, Object>) attachmentMap.get(type);
                if (params == null) params = attachmentMap;
                params = new HashMap<>(params); // HashMap accepts nulls
                return PhotoAttachment.newBuilder()
                        .setWidth((Long) params.getOrDefault("width", 0L))
                        .setHeight((Long) params.getOrDefault("height", 0L))
                        .setDate((Long) params.getOrDefault("date", 0L))
                        .setId((Long) params.getOrDefault("id", 0L))
                        .setOwner_id((Long) params.getOrDefault("owner_id", 0L))
                        .setPost_id((Long) params.getOrDefault("post_id", 0L))
                        .setAlbum_id((Long) params.getOrDefault("album_id", 0L))
                        .setText((String) params.get("text"))
                        .setPhoto_75((String) params.get("photo_75"))
                        .setPhoto_130((String) params.get("photo_130"))
                        .setPhoto_604((String) params.get("photo_604"))
                        .setPhoto_807((String) params.get("photo_807"))
                        .setPhoto_1280((String) params.get("photo_1280"))
                        .setPhoto_2560((String) params.get("photo_2560"))
                        .setAccess_key((String) params.get("access_key"))
                        .build();
            default:
                throw new UnknownType();
        }
    }
}
