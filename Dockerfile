# JDK 21 사용
FROM eclipse-temurin:21-jdk-jammy

# 이미지 메타데이터 정의 (유지관리자, 설명, 버전)
LABEL maintainer="kim hyun bin" \
    description="Backend for Wedit" \
    version="0.0.1"

# 컨테이너의 타임 존 = 아시아/서울
ENV TZ=Asia/Seoul

# 작업 디렉토리 설정
WORKDIR /app

# 업로드 디렉토리 및 로그 디렉토리 생성
RUN mkdir -p /app/uploads /app/logs

# JAR 파일 경로
ARG JAR_FILE=build/libs/backend-0.0.1-SNAPSHOT.jar
# 컨테이너 내부로 JAR 복사
COPY ${JAR_FILE} wedit_backend.jar

# uploads 디렉토리 권한 설정
RUN chmod -R 755 /app/uploads

# 포트 노출
EXPOSE 8080

# 컨테이너 초기화 스크립트
ENTRYPOINT [ \
    "java", \
    "-jar", \
    "-Duser.timezone=${TZ}", \
    "wedit_backend.jar" \
]