# Flyway 마이그레이션 가이드

## Flyway란?

Flyway는 데이터베이스 스키마 버전 관리 도구입니다. Git이 코드 버전을 관리하듯이, Flyway는 DB 스키마 변경 이력을 관리합니다.

```
V1__init.sql  →  V2__add_column.sql  →  V3__create_table.sql
     ↓                  ↓                       ↓
   테이블 생성       컬럼 추가              새 테이블 생성
```

---

## 프로젝트 설정 (이미 완료됨)

### 의존성 (build.gradle.kts)
```kotlin
implementation("org.springframework.boot:spring-boot-starter-flyway")
runtimeOnly("org.flywaydb:flyway-mysql")
```

### 설정 (application.yml)
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### 마이그레이션 파일 위치
```
src/main/resources/db/migration/
```

---

## 마이그레이션 파일 작성법

### 파일 명명 규칙

```
V{버전}__{설명}.sql
  │       │
  │       └─ 언더스코어 2개(__)로 구분
  └─ 버전 번호 (순차적으로 증가)
```

**예시:**
```
V1__init_schema.sql          # 초기 스키마
V2__add_user_status.sql      # 사용자 상태 컬럼 추가
V3__create_friends_table.sql # 친구 테이블 생성
V1.1__add_index.sql          # 소수점 버전도 가능
V20260128__daily_patch.sql   # 날짜 형식도 가능
```

### 파일 내용 예시

**V1__init_schema.sql**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL
);
```

**V2__add_nickname.sql**
```sql
ALTER TABLE users ADD COLUMN nickname VARCHAR(50) NOT NULL DEFAULT '';
```

---

## 실행 방법

### 1. 애플리케이션 실행 시 자동 마이그레이션

Spring Boot 앱을 실행하면 Flyway가 자동으로 마이그레이션을 실행합니다.

```bash
# 프로젝트 루트에서
cd /Users/seochaeyeon/IdeaProjects/gptini/backend

# Gradle로 실행 (prod 프로필 활성화)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### 2. application.yml DB 설정 확인

prod 프로필 사용 시 아래 설정이 적용됩니다:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gptini?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: admin
    password: 1biwpass
```

### 3. 실행 로그 확인

성공 시 아래와 같은 로그가 출력됩니다:
```
Flyway Community Edition 10.x.x
Database: jdbc:mysql://localhost:3306/gptini (MySQL 8.x)
Successfully validated 1 migration
Creating Schema History table `gptini`.`flyway_schema_history`
Current version of schema `gptini`: << Empty Schema >>
Migrating schema `gptini` to version "1 - init schema"
Successfully applied 1 migration to schema `gptini`
```

---

## 주요 명령어 (Gradle Task)

```bash
# 마이그레이션 실행
./gradlew flywayMigrate

# 마이그레이션 상태 확인
./gradlew flywayInfo

# 마이그레이션 검증
./gradlew flywayValidate

# 스키마 완전 삭제 (주의!)
./gradlew flywayClean

# 기존 DB를 Flyway 관리 하에 두기
./gradlew flywayBaseline
```

> **참고:** Gradle Task를 사용하려면 build.gradle.kts에 Flyway 플러그인 설정이 필요합니다.

---

## Flyway가 관리하는 테이블

Flyway는 `flyway_schema_history` 테이블을 자동 생성하여 마이그레이션 이력을 관리합니다.

```sql
SELECT * FROM flyway_schema_history;
```

| installed_rank | version | description | script | checksum | installed_on | success |
|----------------|---------|-------------|--------|----------|--------------|---------|
| 1 | 1 | init schema | V1__init_schema.sql | -123456 | 2026-01-28 | 1 |

---

## 자주 하는 실수 & 해결법

### 1. 이미 적용된 마이그레이션 파일 수정

**문제:** V1__init.sql을 수정하면 checksum 불일치 에러 발생
```
Migration checksum mismatch for migration version 1
```

**해결:**
- 이미 적용된 파일은 **절대 수정하지 않음**
- 새로운 버전 파일(V2__fix_xxx.sql)을 만들어서 수정

### 2. 버전 번호 중복

**문제:** V2 파일이 이미 있는데 또 V2 파일 생성
```
Found more than one migration with version 2
```

**해결:** 항상 다음 버전 번호 사용

### 3. 개발 중 스키마 자주 변경

**해결:** 개발 환경에서는 Flyway 비활성화하고 `ddl-auto: create-drop` 사용
```yaml
# application.yml (기본 프로필)
spring:
  flyway:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 4. 로컬에서 prod 프로필 테스트

```bash
# MySQL에 직접 연결해서 테이블 확인
mysql -u admin -p1biwpass gptini -e "SHOW TABLES;"

# 앱 실행
./gradlew bootRun --args='--spring.profiles.active=prod'
```

---

## 새 마이그레이션 추가 순서

1. **새 SQL 파일 생성**
   ```
   src/main/resources/db/migration/V2__add_user_status.sql
   ```

2. **SQL 작성**
   ```sql
   ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
   ```

3. **앱 실행 또는 Gradle Task 실행**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=prod'
   ```

4. **적용 확인**
   ```bash
   mysql -u admin -p1biwpass gptini -e "DESCRIBE users;"
   ```

---

## 환경별 설정 요약

| 환경 | Flyway | DDL-Auto | DB | 용도 |
|------|--------|----------|-----|------|
| **default** | 비활성화 | create-drop | H2 (인메모리) | 빠른 개발/테스트 |
| **prod** | 활성화 | validate | MySQL | 실제 운영 |

---

## 참고 자료

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [Spring Boot Flyway 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
