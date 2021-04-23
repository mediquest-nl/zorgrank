FROM openjdk:8-alpine
COPY target/uberjar/zorgrank.jar /zorgrank/app.jar
EXPOSE 3000
CMD ["java", "-jar", "/zorgrank/app.jar"]
