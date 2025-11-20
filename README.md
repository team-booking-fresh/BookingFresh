# BookingFresh
-  🥬 Spring Boot 기반 식재료 추천 · 장보기 · 배송시간 예약 서비스 
## 🚀 배포 주소 
- 프로덕션: https://bookingfresh.duckdns.org/
- API 명세: http://43.200.101.128:8080/swagger-ui/index.html#/

## 프로젝트 문서 (Notion)
- https://www.notion.so/oreumi/BookingFresh-2afebaa8982b802e8d7dda5104361c45
## 기능 일람 

## 기술 스택 
#### Backend
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/springmail-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">  
#### Frontend
<img src="https://img.shields.io/badge/html-E34F26?style=for-the-badge&logo=html5&logoColor=white"> <img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"> <img src="https://img.shields.io/badge/bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white"> <img src="https://img.shields.io/badge/thymeleaf-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> 
#### DB

#### External API

#### Deployment/DevOps
  
## 시스템 아키텍처

## ERD 
- https://www.erdcloud.com/d/MepTrpZJoc49NjZFy
<img width="2000" height="1049" alt="image (7)" src="https://github.com/user-attachments/assets/243a7d9e-7e18-44f5-a492-69aedeabe20a" />

## 디렉토리 구조
```
src
 └── main
     ├── java/est/oremi/backend12/bookingfresh
     │   ├── config
     │   │   ├── jwt
     │   │   ├── sessionConfig
     │   │   └── WebSecurityConfig.java
     │   │
     │   ├── domain
     │   │   ├── cart
     │   │   ├── consumer
     │   │   ├── coupon
     │   │   ├── mail
     │   │   ├── order
     │   │   ├── product
     │   │   └── session
     │   │
     │   ├── exception
     │   │
     │   └── BookingFreshApplication.java
     │
     └── resources
         ├── static
         │   ├── images
         │   └── js
         │
         ├── templates
         │   ├── ai
         │   ├── authentication
         │   ├── home.html
         │   └── navbar.html
         │
         ├── application.properties
         ├── application.yml
         ├── application-dev.yml
         └── application-prod.yml
```

## 개발 컨벤션

#### 폴더 구조
- 도메인형 패키지 구조(기능별)

#### 커밋 컨벤션
| 타입 | 의미 | 예시 |
| --- | --- | --- |
| **feat** | 새로운 기능 추가 | `feat(user): 회원가입 기능 구현` |
| **fix** | 버그 수정 | `fix(menu): 메뉴 상세조회 시 NPE 발생 수정` |
| **refactor** | 코드 리팩토링 (기능 변화 없음) | `refactor(service): 로직 구조 개선` |
| **style** | 코드 스타일, 포맷 변경 (로직 영향 X) | `style(html): 인덴트 정리 및 class명 수정` |
| **docs** | 문서 수정 (README, 주석 등) | `docs: API 명세서 수정` |
| **test** | 테스트 코드 추가/수정 | `test(menu): 메뉴 등록 단위 테스트 추가` |
| **chore** | 빌드, 환경설정, 의존성 관련 | `chore(gradle): spring-boot-starter-validation 추가` |
| **ci** | CI/CD 관련 설정 | `ci(github): EC2 자동 배포 스크립트 추가` |
| **perf** | 성능 개선 | `perf(query): MenuRepository 쿼리 최적화` |
| **revert** | 이전 커밋 되돌림 | `revert: "feat: 메뉴 삭제 기능 추가"` |
#### 코드 컨벤션
| 항목 | 규칙 |
| --- | --- |
| **클래스 / 객체 이름** | UpperCamelCase |
| **변수 / 함수 이름** | lowerCamelCase |
| **상수 이름** | UPPER_SNAKE_CASE |
| **Boolean 변수 접두사** | `is_`, `has_`, `can_` 등 의미를 명확히 |
| **컬렉션 변수 복수형** | **복수형 사용 권장** |
| **파일명** | 클래스/모듈명과 동일하게 유지 |
| **패키지명** | 소문자, 언더스코어 없이 |





