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

import com.google.firebase.database.Exclude;

/**
 * @author Anton Popov
 */
public class PhotoAttachment extends Attachment {
    public long width;
    public long height;
    public long date;
    public long id;
    public long owner_id;
    public long post_id;
    public long album_id;
    public String text;
    public String photo_75;
    public String photo_130;
    public String photo_604;
    public String photo_807;
    public String photo_1280;
    public String photo_2560;
    public String access_key;

    private PhotoAttachment() {
        type = "photo";
    }

    public static Builder newBuilder() {
        return new PhotoAttachment().new Builder();
    }

    @Exclude
    public String getLargestImageUrl() {
        if (photo_2560 != null) return photo_2560;
        else if (photo_1280 != null) return photo_1280;
        else if (photo_807 != null) return photo_807;
        else if (photo_604 != null) return photo_604;
        else if (photo_130 != null) return photo_130;
        else if (photo_75 != null) return photo_75;
        else return null;
    }

    public class Builder {

        private Builder() {
        }

        public Builder setWidth(long width) {
            PhotoAttachment.this.width = width;
            return this;
        }

        public Builder setHeight(long height) {
            PhotoAttachment.this.height = height;
            return this;
        }

        public Builder setDate(long date) {
            PhotoAttachment.this.date = date;
            return this;
        }

        public Builder setId(long id) {
            PhotoAttachment.this.id = id;
            return this;
        }

        public Builder setOwner_id(long owner_id) {
            PhotoAttachment.this.owner_id = owner_id;
            return this;
        }

        public Builder setPost_id(long post_id) {
            PhotoAttachment.this.post_id = post_id;
            return this;
        }

        public Builder setAlbum_id(long album_id) {
            PhotoAttachment.this.album_id = album_id;
            return this;
        }

        public Builder setText(String text) {
            PhotoAttachment.this.text = text;
            return this;
        }

        public Builder setPhoto_75(String photo_75) {
            PhotoAttachment.this.photo_75 = photo_75;
            return this;
        }

        public Builder setPhoto_130(String photo_130) {
            PhotoAttachment.this.photo_130 = photo_130;
            return this;
        }

        public Builder setPhoto_604(String photo_604) {
            PhotoAttachment.this.photo_604 = photo_604;
            return this;
        }

        public Builder setPhoto_807(String photo_807) {
            PhotoAttachment.this.photo_807 = photo_807;
            return this;
        }

        public Builder setPhoto_1280(String photo_1280) {
            PhotoAttachment.this.photo_1280 = photo_1280;
            return this;
        }

        public Builder setPhoto_2560(String photo_2560) {
            PhotoAttachment.this.photo_2560 = photo_2560;
            return this;
        }

        public Builder setAccess_key(String access_key) {
            PhotoAttachment.this.access_key = access_key;
            return this;
        }

        public PhotoAttachment build() {
            return PhotoAttachment.this;
        }
    }
}
