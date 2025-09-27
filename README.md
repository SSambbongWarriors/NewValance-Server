# NewValance-Server
이화여자대학교 캡스톤디자인과창업프로젝트 3조 쌈뽕워리어즈 BE server

주요 기술 스택:

- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- Lombok
- JWT 인증
- OAuth2 (Google/Kakao/Naver) 로그인
- MySQL RDS
- Gradle

---

## 📋 사전 준비

- JDK 17 이상  
- Gradle 7 이상  
- Git  
- MySQL 8.x (RDS)  
- HTTPS 로컬 개발용 `keystore.p12`

---

## 🍴 Fork & Clone

1. GitHub에서 이 리포지토리를 **Fork** 합니다.  
2. 로컬에 **Clone** 합니다:  
    
        git clone https://github.com/<your-username>/capston.new_valance.git
        cd capston.new_valance

3. **upstream** 원격 저장소를 추가해 동기화할 수 있도록 합니다:  

        git remote add upstream https://github.com/original-owner/capston.new_valance.git

4. 변경사항을 가져와 병합하려면:  

        git fetch upstream
        git merge upstream/main

---

## 🔧 설정

### 1. application.yml

    spring:
      config:
        import: "classpath:application-API-key.properties"
      application:
        name: new-valance

      mvc:
        throw-exception-if-no-handler-found: true

      web:
        resources:
          add-mappings: false

      datasource:
        url: 
        username: 
        password: 
        driver-class-name: com.mysql.cj.jdbc.Driver

      security:
        oauth2:
          client:
            registration:
              kakao:
                client-id: ${Kakao_Client_ID}
                client-secret: ${Kakao_Client_SECRET}
                client-authentication-method: client_secret_post
                authorization-grant-type: authorization_code
                provider: kakao
                redirect-uri: https://localhost:8443/login/oauth2/code/kakao
                scope:
                  - profile_nickname
                  - account_email
                  - profile_image
              naver:
                client-id: ${Naver_Client_ID}
                client-secret: ${Naver_Client_Secret}
                redirect-uri: https://localhost:8443/login/oauth2/code/naver
                authorization-grant-type: authorization_code
                provider: naver
                scope:
                  - email
                  - profile_image
                  - nickname
              google:
                client-id: ${Google_Client_ID}
                client-secret: ${Google_Client_Secret}
                redirect-uri: https://localhost:8443/login/oauth2/code/google
                scope:
                  - email
                  - profile
          provider:
            kakao:
              authorization-uri: https://kauth.kakao.com/oauth/authorize
              token-uri: https://kauth.kakao.com/oauth/token
              user-info-uri: https://kapi.kakao.com/v2/user/me
              user-name-attribute: id
            naver:
              authorization_uri: https://nid.naver.com/oauth2.0/authorize
              token_uri: https://nid.naver.com/oauth2.0/token
              user-info-uri: https://openapi.naver.com/v1/nid/me
              user_name_attribute: response

      cloud:
        aws:
          region:
            static: ap-northeast-2
          s3:
            bucket: newvalance

    server:
      port: 8443
      ssl:
        key-store: classpath:keystore.p12
        key-store-password: 
        key-store-type: PKCS12
      forward-headers-strategy: native

    logging:
      level:
        root: INFO
        capston.new_valance.controller: DEBUG
        capston.new_valance.util.S3Uploader: DEBUG
        org.springframework: INFO

### 2. application-API-key.properties

    Kakao_Client_ID=your_kakao_client_id
    Kakao_Client_SECRET=your_kakao_client_secret
    Naver_Client_ID=your_naver_client_id
    Naver_Client_Secret=your_naver_client_secret
    Google_Client_ID=your_google_client_id
    Google_Client_Secret=your_google_client_secret

### 3. application-prod.yml

    # 내용은 application.yml과 동일하며, prod 프로파일 활성화 시 사용
    spring:
      config:
        import: "classpath:application-API-key.properties"
      application:
        name: new-valance
      # 이하 application.yml과 동일...
      # (생략)

---

## 🏗️ 빌드 & 실행

Gradle로 빌드하고 실행합니다:

    # 빌드
    ./gradlew clean build

    # 애플리케이션 실행
    ./gradlew bootRun

    # 또는 JAR 직접 실행
    java -jar build/libs/capston.new_valance-0.0.1-SNAPSHOT.jar

기본 포트: **8443** (HTTPS)

---

## 🔌 주요 API 엔드포인트

### 인증
- **GET** `/api/login/{provider}` — 회원 가입/로그인   
- **POST** `/api/auth/refresh` — 토큰 재발급   

### 사용자
- **GET**  `/api/profile` — 내 정보 조회  
- **GET**  `/api/profile/week` — 주별 시청 개수 조회  

### 영상 & 추천
- **GET**  `/api/news/home`       — 홈 뉴스 가판대 조회  
- **GET**  `/api/news/category/{categoryId}` — 카테고리별 뉴스 리스트 조회  
- **GET** `/api/video/recommend/{newsId}` — 추천 동영상 재생  

---


## 🤝 기여 가이드

1. 프로젝트를 Fork  
2. 새 브랜치 생성:  
        
        git checkout -b feature/your-feature-name

3. 코드 변경 및 커밋  
4. 브랜치 푸시 후 Pull Request 생성  

