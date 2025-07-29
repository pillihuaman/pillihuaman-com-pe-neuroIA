# syntax=docker/dockerfile:1.4

FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

ARG CODEARTIFACT_AUTH_TOKEN
ENV CODEARTIFACT_AUTH_TOKEN=${CODEARTIFACT_AUTH_TOKEN}

RUN mkdir -p /root/.m2 && \
    echo "<settings><servers><server><id>codeartifact</id><username>aws</username><password>${CODEARTIFACT_AUTH_TOKEN}</password></server></servers><profiles><profile><id>codeartifact</id><activation><activeByDefault>true</activeByDefault></activation><repositories><repository><id>codeartifact</id><url>https://pillihuamanlib-614520203803.d.codeartifact.us-east-1.amazonaws.com/maven/pillihuaman-com-pe-lib/</url></repository></repositories></profile></profiles><activeProfiles><activeProfile>codeartifact</activeProfile></activeProfiles><mirrors><mirror><id>codeartifact</id><name>CodeArtifact</name><url>https://pillihuamanlib-614520203803.d.codeartifact.us-east-1.amazonaws.com/maven/pillihuaman-com-pe-lib/</url><mirrorOf>external:*</mirrorOf></mirror></mirrors></settings>" > /root/.m2/settings.xml

COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn clean install -DskipTests --no-transfer-progress

# --- ETAPA 2: FINAL (FINAL STAGE) ---
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app

COPY --from=build /app/target/pillihuaman.com.pe.neuroIA-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
