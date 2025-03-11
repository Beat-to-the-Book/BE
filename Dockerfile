# OpenJDK 17을 사용
FROM openjdk:17-jdk-slim AS build

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트의 모든 파일을 컨테이너 내부로 복사
COPY . .

# Gradle 실행 권한 부여 (리눅스 환경에서 필수)
RUN chmod +x gradlew

# Gradle을 사용하여 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew build -x test

# 실행 환경 설정
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드된 JAR 파일을 실행할 환경에 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 Spring Boot 실행
CMD ["java", "-jar", "app.jar"]
