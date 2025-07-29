# =================================================================
# === ETAPA 1: CONSTRUCCIÓN (BUILD STAGE) - "El Taller" ===
# =================================================================
# Usamos una imagen oficial de Maven con Java 17. Esta imagen es grande
# porque tiene todas las herramientas para compilar tu código.
FROM maven:3.8.5-openjdk-17 AS build

# Establecemos el directorio de trabajo donde ocurrirá toda la compilación.
WORKDIR /app

# Argumento para recibir el token de autenticación de CodeArtifact desde GitHub Actions.
ARG CODEARTIFACT_AUTH_TOKEN

# Crea el archivo settings.xml DENTRO del contenedor en tiempo de construcción.
# Esta es la forma más segura y limpia de manejar la autenticación con repositorios privados.
# REEMPLAZA la URL con la URL de TU NUEVO repositorio de CodeArtifact.
RUN echo '<settings><servers><server><id>codeartifact</id><username>aws</username><password>${env.CODEARTIFACT_AUTH_TOKEN}</password></server></servers><profiles><profile><id>codeartifact</id><activation><activeByDefault>true</activeByDefault></activation><repositories><repository><id>codeartifact</id><url>https://pillihuamanlib-614520203803.d.codeartifact.us-east-1.amazonaws.com/maven/pillihuaman-com-pe-lib/</url></repository></repositories></profile></profiles><activeProfiles><activeProfile>codeartifact</activeProfile></activeProfiles><mirrors><mirror><id>codeartifact</id><name>CodeArtifact</name><url>https://pillihuamanlib-614520203803.d.codeartifact.us-east-1.amazonaws.com/maven/pillihuaman-com-pe-lib/</url><mirrorOf>external:*</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml

# Copia el archivo pom.xml primero. Docker es inteligente: si este archivo no cambia,
# reutilizará la capa de dependencias descargadas, haciendo las construcciones futuras más rápidas.
COPY pom.xml .

# Descarga todas las dependencias usando el settings.xml que acabamos de crear.
# El --no-transfer-progress hace que los logs de GitHub Actions sean más limpios.
RUN mvn dependency:go-offline --no-transfer-progress

# Ahora copia el resto del código fuente de tu aplicación.
COPY src ./src

# Finalmente, compila la aplicación y crea el archivo .jar.
# Saltamos los tests para acelerar el pipeline de CI/CD.
RUN mvn clean install -DskipTests --no-transfer-progress


# =================================================================
# === ETAPA 2: FINAL (FINAL STAGE) - "El Contenedor de Producción" ===
# =================================================================
# Empezamos desde una imagen base limpia y muy ligera de Java 17.
# Esta imagen NO contiene Maven ni el código fuente, solo lo necesario para ejecutar.
FROM amazoncorretto:17-alpine-jdk

# Establecemos el directorio de trabajo.
WORKDIR /app

# Copiamos ÚNICAMENTE el archivo .jar compilado desde la etapa de construcción anterior.
# ESTA LÍNEA DEBE SER MODIFICADA PARA CADA MICROSERVICIO.
COPY --from=build /app/target/pillihuaman-com-pe-neuroIA-0.0.1-SNAPSHOT.jar app.jar

# Informa a Docker que el contenedor escuchará en este puerto.
# ESTE PUERTO DEBE SER MODIFICADO PARA CADA MICROSERVICIO.
EXPOSE 8099

# El comando que se ejecutará cuando el contenedor se inicie.
ENTRYPOINT ["java", "-jar", "app.jar"]