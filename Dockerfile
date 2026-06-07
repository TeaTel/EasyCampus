# Campus Market v1.2.1 - Multi-stage Docker Build
# Stage 1: Node.js (Frontend Build)
# Stage 2: Java JDK (Backend Build)
# Stage 3: Java JRE (Runtime)

# ============================================
# STAGE 1: Build Frontend with Node.js
# ============================================
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package.json ./
COPY frontend/package-lock.json ./

RUN npm ci

COPY frontend/vite.config.js ./
COPY frontend/index.html ./
COPY frontend/public ./public/
COPY frontend/src ./src/
COPY frontend/scripts ./scripts/
COPY frontend/.env.production ./

RUN npm run build

# ============================================
# STAGE 2: Build Backend with Java JDK
# ============================================
FROM eclipse-temurin:21-jdk AS backend-build

ARG MAVEN_VERSION=3.9.15
ENV MAVEN_HOME=/opt/maven

RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -fSL -o /tmp/maven.tar.gz "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" || \
    curl -fSL -o /tmp/maven.tar.gz "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    tar xzf /tmp/maven.tar.gz -C /opt && rm /tmp/maven.tar.gz && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    ln -s ${MAVEN_HOME}/bin/mvn /usr/local/bin/mvn && \
    apt-get purge -y curl && apt-get autoremove -y && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app/backend

COPY backend/pom.xml .

RUN mvn dependency:go-offline -B --no-transfer-progress || true

COPY backend/src ./src

RUN mkdir -p src/main/resources/static

COPY --from=frontend-build /app/frontend/dist/ src/main/resources/static/

RUN ls -la src/main/resources/static/ && mvn clean package -DskipTests -B --no-transfer-progress

# ============================================
# STAGE 3: Runtime with Java JRE
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=backend-build /app/backend/target/backend-0.0.1-SNAPSHOT.jar ./backend.jar

COPY start.sh ./start.sh
RUN chmod +x ./start.sh

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

CMD ["./start.sh"]
