# Homework Bot

Homework bot uses VK bot api and allows users to collaboratively manage homework of single class. For now it supports 
**only** russian language. Pupils can add and get homework. Added homework is visible to all pupils. One instance is 
intended for **one** class. 

## List of all commands:
1. Get homework of single subject:
    * что задали по алгебре
    * дз по геометрии
2. Get all homework:
    * что задали
    * показать все домашние задания
    * покажи все дз
    * покажи все задания
3. Add homework:
    * русский: упр. 259, 261
    
    You can attach from 1 to 5 photos to message
4. Adding new homework overrides old one. ***Warning***: Adding homework to **only** next lesson is allowed.
5. Schedule
    * расписание
    * расписание на завтра
    * расписание на неделю
6. Get help:
    * привет
    * помощь
    
## Deploy
To run this bot you will need **server** with 🐳docker installed. I use 
[Raspberry Pi 3B](https://www.raspberrypi.org/products/raspberry-pi-3-model-b/) with 
[Raspbian Stretch](https://www.raspberrypi.org/downloads/raspbian/) installed.

1. Clone repo
    ```bash
    git clone https://github.com/popovanton0/homework-bot.git
    ```
2. Create Firebase project: [tutorial](https://firebase.google.com/docs/admin/setup#add_firebase_to_your_app) 
3. Replace `serverSecret.json` with *JSON file containing your service account's credentials* and rename it to 
*serverSecret.json*
4. Create group in VK
    1. Go to <img src="https://image.freepik.com/free-icon/more-button-interface-symbol-of-three-horizontal-aligned-dots_318-69928.jpg" width="22">
    -> Group manage -> Settings -> API
    2. Create `access key` with access to messages of the group
    3. Add server in the *Callback API* section and copy `confirmation code`
    4. Add random `secret key`
5. In `google-cloud-functions/functions/index.js` file replace:
    1. vkSecret with `secret key`
    2. confirmationCode with `confirmation code`
6. [Install](https://firebase.google.com/docs/cli/) firebase-tools (if not already installed)
7. Run in `google-cloud-functions` folder
    ```bash
    firebase init
    ```
    1. Choose only *functions*
8. Run in `google-cloud-functions` folder
    ```bash
    firebase deploy
    ```
9. In VK group API settings set `server url` to firebase function url (shows up after *firebase deploy* command)
10. Open realtime-database.json file and:
    1. Replace `accessToken` value with VK access key from 4.2
    2. Add your schedule (example is there, you only need to change it)
    3. Add subjects (value of subject is regexp, witch bot looking for in user messages)
    4. Add your time schedule (special is schedule for Saturday)
    5. Duplicate this changes to test branch 
11. Go to https://console.firebase.google.com , choose your project, click Develop -> Database -> Realtime 
Database -> require auth -> press 
<img src="http://www.eternaljudgment.com/wp-content/uploads/2016/07/three-vertical-dots-1.png" width="22"> -> *Import
from JSON file* -> upload `realtime-database.json` file
12. In src/main/java/com/popov/homeworkbotserver/Main.java replace *`<PROJECT-ID>`* with firebase project id (get from 
serverSecret.json file, "project_id":<PROJECT-ID>)
13. 🐳Run in *root* of the project
    ```bash
    mvn clean package
    docker build -t popovanton0/homework-bot
    docker run -d -e DB_BRANCH='production' popovanton0/homework-bot
    ```
    
## RKN Blocks
Because of the recent 
[RKN blocks](https://www.bleepingcomputer.com/news/government/russia-bans-18-million-amazon-and-google-ips-in-attempt-to-block-telegram/), 
deploying Firebase Cloud Functions and using Realtime Database in Firebase Admin SDK for Java is unavailable. 
But access to running Firebase Cloud Functions itself is available right now (29.04.2018). So only solution is to place 
server outside Russia or use proxies (I tried but no success).