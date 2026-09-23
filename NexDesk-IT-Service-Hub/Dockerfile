FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy Java source
COPY backend/src /app/src

# Copy Oracle JDBC driver
COPY backend/lib /app/lib

# Copy frontend files
COPY frontend /app/frontend

# Compile Java application
RUN mkdir -p /app/out \
    && find /app/src -name "*.java" > /tmp/sources.txt \
    && javac -cp "/app/lib/ojdbc11.jar" \
       -d /app/out \
       @/tmp/sources.txt

# Render will provide PORT at runtime.
EXPOSE 8080

# Database configuration must be supplied by environment variables.
# Do NOT put your Oracle password in the Dockerfile.
ENV DB_URL=""
ENV DB_USER=""
ENV DB_PASSWORD=""

# Start application
CMD ["java", "-cp", "/app/out:/app/lib/ojdbc11.jar", "com.nexdesk.NexDeskApplication"]
