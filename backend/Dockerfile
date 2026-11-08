# =============================================================================
# Imagen del backend para Render (D49). Dos etapas:
#   1. compilacion: compila el jar con Maven y JDK 17 (los tests no corren acá: necesitan una base).
#   2. ejecucion:   solo el JRE 17 y el jar. La imagen final no lleva Maven, el código fuente ni el JDK.
# =============================================================================

# ---------- Etapa 1: compilación ----------
FROM maven:3.9.11-eclipse-temurin-17 AS compilacion
WORKDIR /app

# Primero solo el pom: así Docker reutiliza las dependencias descargadas si solo cambió el código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# La aplicación no corre como root.
RUN useradd --system --no-create-home --uid 10001 fixture
COPY --from=compilacion /app/target/fixture-0.0.1-SNAPSHOT.jar app.jar
USER fixture

# El plan gratis de Render tiene 512 MB en total: se limita la memoria de la JVM para que entre con margen.
# -Xmx300m: heap máximo. MaxMetaspaceSize: clases cargadas. SerialGC: el recolector que menos memoria usa.
# TieredStopAtLevel=1: compila menos optimizado pero arranca más rápido y usa menos memoria.
ENV JAVA_TOOL_OPTIONS="-Xmx300m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"

# Render define PORT; la aplicación lo lee con server.port=${PORT:8080}.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
