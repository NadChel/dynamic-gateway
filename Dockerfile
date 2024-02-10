FROM amazoncorretto:17-alpine-jdk AS builder

WORKDIR /app

COPY . .

RUN apk add --no-cache maven && \
    mvn package -Dmaven.test.skip=true

FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY --from=builder /app/target/dynamic-gateway-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

CMD ["java", "-jar", "dynamic-gateway-0.0.1-SNAPSHOT.jar"]