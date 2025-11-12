# OpenJDK 17을 사용
FROM eclipse-temurin:17-jdk AS build

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트의 모든 파일을 컨테이너 내부로 복사
COPY . .

# Gradle 실행 권한 부여 (리눅스 환경에서 필수)
RUN chmod +x gradlew

# Gradle을 사용하여 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew build -x test

# 실행 환경 설정
FROM debian:bookworm

WORKDIR /app

# JRE 및 기본 유틸 설치 (캐시 최소화)
RUN apt-get -o Acquire::Retries=5 update && \
    apt-get install -y --no-install-recommends \
        ca-certificates gnupg \
        curl wget unzip \
        openjdk-17-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

# Chromium만 먼저 설치 (용량 피크 분산)
RUN apt-get -o Acquire::Retries=5 update && \
    apt-get install -y --no-install-recommends \
        chromium && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

# Chromium-Driver 별도 설치 (용량 피크 분산)
RUN apt-get -o Acquire::Retries=5 update && \
    apt-get install -y --no-install-recommends \
        chromium-driver && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

# 빌드된 JAR 파일을 실행할 환경에 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 Spring Boot 실행
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]