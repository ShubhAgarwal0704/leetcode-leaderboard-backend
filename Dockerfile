FROM maven:3.8.5-openjdk-17 AS BUILD
COPY . .
RUN mvn clean package -DskipTests


FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/leetcode-dashboard-0.0.1-SNAPSHOT.jar leetcode-dashboard.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar","leetcodedashboard.jar"]
