FROM eclipse-temurin:11-jre
LABEL authors="mervedoker"

WORKDIR /app

# Copy the fat jar
COPY build/libs/*.jar /app/app.jar

# (Optional) run as non-root for better practice
RUN useradd -r -u 10001 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

# You can pass JVM options via JAVA_OPTS env
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
