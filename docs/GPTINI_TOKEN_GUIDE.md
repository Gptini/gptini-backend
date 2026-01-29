# gptini-token 스크립트 가이드

로컬 개발 시 API 토큰을 빠르게 발급받아 클립보드에 복사하는 zsh 함수입니다.

---

## 설치 위치

```
~/.zsh/functions/gptini.zsh
```

> `~/.zshrc`에서 자동으로 로드됩니다.

---

## 사용법

### 기본 사용 (기본값 사용)

```bash
gptini-token
```

**기본값:**
- URL: `http://localhost:8080`
- Email: `test1@test.com`
- Password: `testtest1!`

### 파라미터 지정

```bash
# URL만 변경
gptini-token http://localhost:9090

# URL + 이메일 변경
gptini-token http://localhost:8080 myemail@test.com

# 전부 지정
gptini-token http://localhost:8080 myemail@test.com mypassword
```

### 파라미터 순서

```
gptini-token [URL] [EMAIL] [PASSWORD]
```

| 순서 | 파라미터 | 기본값 |
|------|----------|--------|
| 1 | URL | `http://localhost:8080` |
| 2 | Email | `test1@test.com` |
| 3 | Password | `testtest1!` |

---

## 출력 예시

### 성공 시

```
Logging in as test1@test.com on http://localhost:8080 ...
Token copied to clipboard!
```

→ 클립보드에 JWT 토큰이 복사됨 (바로 붙여넣기 가능)

### 실패 시

```
Logging in as test1@test.com on http://localhost:8080 ...
Login failed:
{
  "success": false,
  "message": "이메일 또는 비밀번호가 일치하지 않습니다",
  "data": null
}
```

---

## 활용 예시

### Swagger에서 사용

1. `gptini-token` 실행
2. Swagger UI → Authorize 버튼 클릭
3. `Cmd + V`로 토큰 붙여넣기

### curl에서 사용

```bash
# 토큰 발급
gptini-token

# API 호출 (토큰 붙여넣기)
curl -H "Authorization: Bearer <Cmd+V>" http://localhost:8080/api/v1/users/me
```

### HTTPie에서 사용

```bash
gptini-token
http GET localhost:8080/api/v1/users/me "Authorization: Bearer $(pbpaste)"
```

---

## 테스트 계정 생성

스크립트를 사용하려면 먼저 테스트 계정이 있어야 합니다.

```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test1@test.com",
    "password": "testtest1!",
    "nickname": "테스터"
  }'
```

---

## 스크립트 수정

기본값을 변경하고 싶으면:

```bash
vi ~/.zsh/functions/gptini.zsh
```

```bash
# 기본값 수정
local baseurl="${1:-http://localhost:8080}"    # URL
local email="${2:-test1@test.com}"             # 이메일
local password="${3:-testtest1!}"              # 비밀번호
```

수정 후 적용:

```bash
source ~/.zshrc
```

---

## 요구사항

- `jq` 설치 필요 (JSON 파싱용)
  ```bash
  brew install jq
  ```
- `pbcopy` (macOS 기본 제공)
