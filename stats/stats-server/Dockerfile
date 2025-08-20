FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]