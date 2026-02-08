# EC2 자동 시작/중지 스케줄러 설정

한국 시간 기준 새벽 2시 ~ 오전 6시 동안 EC2를 자동으로 중지하여 비용을 절감합니다.

---

## 1. Lambda 함수 생성

### 1-1. EC2 중지 함수

1. AWS Lambda 콘솔 접속
2. **함수 생성** > **새로 작성**
3. 설정:
   - 함수 이름: `gptini-ec2-stop`
   - 런타임: Python 3.12
   - 아키텍처: arm64
4. 코드:

```python
import boto3

INSTANCE_ID = "i-여기에인스턴스ID입력"
REGION = "ap-northeast-2"

def lambda_handler(event, context):
    ec2 = boto3.client("ec2", region_name=REGION)
    ec2.stop_instances(InstanceIds=[INSTANCE_ID])
    return {"status": "stopped", "instance": INSTANCE_ID}
```

### 1-2. EC2 시작 함수

1. **함수 생성** > **새로 작성**
2. 설정:
   - 함수 이름: `gptini-ec2-start`
   - 런타임: Python 3.12
   - 아키텍처: arm64
3. 코드:

```python
import boto3

INSTANCE_ID = "i-여기에인스턴스ID입력"
REGION = "ap-northeast-2"

def lambda_handler(event, context):
    ec2 = boto3.client("ec2", region_name=REGION)
    ec2.start_instances(InstanceIds=[INSTANCE_ID])
    return {"status": "started", "instance": INSTANCE_ID}
```

---

## 2. IAM 권한 설정

Lambda 함수의 실행 역할에 아래 인라인 정책을 추가합니다.

Lambda 함수 > 구성 > 권한 > 역할 이름 클릭 > 인라인 정책 생성

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ec2:StartInstances",
                "ec2:StopInstances",
                "ec2:DescribeInstances"
            ],
            "Resource": "*"
        }
    ]
}
```

---

## 3. EventBridge 스케줄 규칙 생성

Amazon EventBridge > 규칙 > 규칙 생성

### 3-1. EC2 중지 규칙 (새벽 2시)

- 규칙 이름: `gptini-ec2-stop-schedule`
- 규칙 유형: 일정
- cron 식: `cron(0 17 * * ? *)`
  - UTC 17:00 = KST 02:00 (새벽 2시)
- 대상: Lambda 함수 `gptini-ec2-stop`

### 3-2. EC2 시작 규칙 (오전 6시)

- 규칙 이름: `gptini-ec2-start-schedule`
- 규칙 유형: 일정
- cron 식: `cron(0 21 * * ? *)`
  - UTC 21:00 = KST 06:00 (오전 6시)
- 대상: Lambda 함수 `gptini-ec2-start`

---

## 4. 테스트

1. Lambda 콘솔에서 각 함수의 **Test** 버튼 클릭
2. 빈 이벤트 `{}` 로 실행
3. EC2 콘솔에서 인스턴스 상태 변경 확인

---

## 참고

- EC2 인스턴스 ID 확인: EC2 콘솔 > 인스턴스 > 인스턴스 ID 열
- 중지된 EC2는 EBS 스토리지 비용만 발생 (인스턴스 요금 없음)
- Elastic IP가 연결된 경우 중지 상태에서도 IP 요금 발생
- 스케줄 일시 중지: EventBridge 규칙을 비활성화하면 됨

---

# S3 수명 주기 규칙 (자동 삭제)

업로드된 파일을 4일 후 자동 삭제하여 스토리지 비용을 절감합니다.

## 설정 방법

1. S3 콘솔 > `gptini-attachments` 버킷 > **관리** 탭
2. **수명 주기 규칙 생성** 클릭
3. 설정:

| 항목 | 값 |
|------|------|
| 규칙 이름 | `auto-delete-after-4-days` |
| 접두사 필터 | `chat/` (채팅 파일만 대상) |
| 수명 주기 규칙 작업 | **객체의 현재 버전 만료** 체크 |
| 객체 생성 후 경과 일수 | **4** |

4. 규칙 생성 완료

## 참고

- 삭제는 4일 경과 후 최대 24시간 내에 처리됨 (실질적으로 4~5일)
- 삭제된 이미지는 프론트엔드에서 만료 처리됨 (placeholder 표시)
- 규칙 비활성화하면 자동 삭제 중지
