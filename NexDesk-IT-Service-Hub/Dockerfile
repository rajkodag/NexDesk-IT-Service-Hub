FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY backend/src /app/src
COPY backend/lib /app/lib
COPY frontend /app/frontend
RUN mkdir -p /app/out && find /app/src -name "*.java" > /tmp/sources.txt && javac -cp "/app/lib/ojdbc11.jar" -d /app/out @/tmp/sources.txt
EXPOSE 8080
ENV DB_URL=jdbc:oracle:thin:@host.docker.internal:1521/XEPDB1
ENV DB_USER=ITHELPDESK
CMD ["java","-cp","/app/out:/app/lib/ojdbc11.jar","com.nexdesk.NexDeskApplication"]
