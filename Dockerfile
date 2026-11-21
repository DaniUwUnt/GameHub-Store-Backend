# Una compilación genera todos los módulos. Compose reutiliza la misma imagen.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -q -B package -DskipTests \
    && mkdir -p /jars && find . -path '*/target/*-service.jar' -exec cp {} /jars/ \; \
    && cp api-gateway/target/api-gateway.jar eureka-server/target/eureka-server.jar /jars/

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /jars/ .
ENV JAVA_TOOL_OPTIONS="-XX:ActiveProcessorCount=2 -Xms48m -Xmx256m"
USER 10001
ENTRYPOINT ["sh", "-c", "exec java -jar /app/${MODULO:?Define MODULO}.jar"]
