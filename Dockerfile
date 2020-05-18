FROM openjdk:11
COPY target/transitlog-hfp-api.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]