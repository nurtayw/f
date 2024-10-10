FROM openjdk:17-jdk

WORKDIR /app

COPY target/teststore-0.0.1-SNAPSHOT.jar /app/teststore.jar

EXPOSE 8080

CMD [ "java", "-jar", "teststore.jar" ]