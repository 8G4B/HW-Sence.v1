# 베이스 이미지 설정
FROM gradle:7.6-jdk17 AS build
# 작업 디렉토리 설정
WORKDIR /app
# 소스 코드 복사
COPY . /app
# 빌드 실행
RUN gradle clean build -x test
# JRE 실행 단계
FROM openjdk:17-jdk-slim
# 작업 디렉토리 설정
WORKDIR /app
# 빌드 단계에서 생성된 jar 파일을 복사
COPY --from=build /app/build/libs/*.jar hw-sence.jar
# 포트 설정
EXPOSE 18029
# 명령어 실행
ENTRYPOINT ["java", "-jar", "hw-sence.jar"]