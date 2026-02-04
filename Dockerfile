# syntax=docker/dockerfile:1

ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre

# ------------------------------
# deps: pre-fetch Maven deps (fast rebuilds)
# ------------------------------
FROM ${MAVEN_IMAGE} AS deps
WORKDIR /workspace

# The official Maven Docker image sets MAVEN_CONFIG=/root/.m2.
# This project's Maven Wrapper (mvnw) uses MAVEN_CONFIG as *CLI args* and will
# accidentally pass "/root/.m2" to Maven, causing: "Unknown lifecycle phase".
ENV MAVEN_CONFIG=""

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies/plugins once to maximize Docker layer caching
RUN ./mvnw -q -DskipTests dependency:go-offline


# ------------------------------
# build: compile/package the Quarkus app (fast-jar)
# ------------------------------
FROM ${MAVEN_IMAGE} AS build
WORKDIR /workspace

ENV MAVEN_CONFIG=""

COPY --from=deps /root/.m2 /root/.m2
COPY . .

RUN ./mvnw -q -DskipTests package


# ------------------------------
# dev: Quarkus dev mode (hot reload)
# ------------------------------
FROM ${MAVEN_IMAGE} AS dev
WORKDIR /workspace

ENV MAVEN_CONFIG=""

# Keep dependencies cached; source is copied for convenience,
# but you will typically run with a bind mount on /workspace.
COPY --from=deps /root/.m2 /root/.m2
COPY . .

EXPOSE 8080 5005
CMD ["./mvnw", "quarkus:dev", "-Dquarkus.http.host=0.0.0.0"]


# ------------------------------
# runtime: minimal JRE image
# ------------------------------
FROM ${RUNTIME_IMAGE} AS runtime
WORKDIR /app

# Create a non-root user (works on Ubuntu/Debian based Temurin images)
RUN (addgroup --system --gid 1001 quarkus 2>/dev/null || true) \
 && (adduser  --system --uid 1001 --ingroup quarkus --home /app --disabled-password quarkus 2>/dev/null || true)

COPY --from=build /workspace/target/quarkus-app/lib/ ./lib/
COPY --from=build /workspace/target/quarkus-app/app/ ./app/
COPY --from=build /workspace/target/quarkus-app/quarkus/ ./quarkus/
COPY --from=build /workspace/target/quarkus-app/quarkus-run.jar ./quarkus-run.jar

EXPOSE 8080
USER 1001

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0"
ENTRYPOINT ["java"]
CMD ["-jar", "/app/quarkus-run.jar"]


# ------------------------------
# bot: Discord Bot (ThrasherBot Reactive) - Standalone Java app
# ------------------------------
FROM ${MAVEN_IMAGE} AS bot-build
WORKDIR /workspace

ENV MAVEN_CONFIG=""

COPY --from=deps /root/.m2 /root/.m2
COPY . .

# Just compile, don't package
RUN ./mvnw -q compile

# Bot runtime - Maven image to run with exec:java
FROM ${MAVEN_IMAGE} AS bot
WORKDIR /app

ENV MAVEN_CONFIG=""

# Copy everything needed
COPY --from=bot-build /root/.m2 /root/.m2
COPY --from=bot-build /workspace /app

RUN chmod +x /app/mvnw

USER 1000

# Run the bot with maven exec
ENTRYPOINT ["/app/mvnw"]
CMD ["exec:java", "-Dexec.mainClass=fr.univtln.yhaouas846.discord4j.ThrasherBotReactive"]
