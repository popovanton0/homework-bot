FROM openjdk:8u151-jre
WORKDIR /usr/src/app
ADD target/homework-bot-server-jar-with-dependencies.jar /usr/src/app/server.jar
ADD serverSecret.json /usr/src/app/serverSecret.json
CMD java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar server.jar $DB_BRANCH $VK_API_BASE_URL