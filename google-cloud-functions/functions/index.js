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

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const vkSecret = "*************************************************";
const confirmationCode = "";
admin.initializeApp(functions.config().firebase);

exports.helloWorld = functions.https.onRequest((req, res) => {
    if (req.method === "GET") {
        res.status(200).send();
        return;
    }

    // secret check
    if (req.body.secret !== vkSecret) {
        res.status(401);
        return;
    }

    if (req.body.type === "confirmation") {
        res.status(200).send(confirmationCode);
    } else if (req.body.type === "message_new") {
        res.status(200).send("ok");
        return admin.database().ref("production").child("requests").push({
            "userId": req.body.object.user_id,
            "text": req.body.object.body,
            "attachments" : req.body.object.attachments !== undefined ? req.body.object.attachments : null
        });
    }
});