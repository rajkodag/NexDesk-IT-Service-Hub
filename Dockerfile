# ==========================================
# Stage 1: Build Application with Maven
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy POM files and source code
COPY pom.xml .
COPY backend/pom.xml backend/
COPY backend/src backend/src
COPY frontend frontend

# Build package without running unit tests during Docker build
RUN mvn clean package -f backend/pom.xml -DskipTests

# ==========================================
# Stage 2: Minimal Production Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built Spring Boot executable jar from builder stage
COPY --from=build /app/backend/target/nexdesk-backend-*.jar app.jar
COPY frontend /app/frontend

# Expose Spring Boot port
EXPOSE 8080

# Environment variables with sensible enterprise defaults
ENV PORT=8080
ENV DB_URL=jdbc:oracle:thin:@host.docker.internal:1521/XEPDB1
ENV DB_USER=ITHELPDESK
ENV DB_PASSWORD=ithelpdesk123
ENV JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
ENV JWT_EXPIRATION=86400000

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/api/health || exit 1

# Execute Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
