# 간단한 계약 생성 API ⭐⭐⭐

## 초간단 계약 생성 API

### 엔드포인트
```
POST /api/v1/contracts/{vendorId}/simple-create?memberId=1
```

### 요청 파라미터
- `vendorId`: 업체 ID (Path Variable)
- `memberId`: 회원 ID (Query Parameter)

### 요청 바디 (초간단!)
```json
{
  "contractDate": "2025-09-04",
  "contractTime": "10:00:00",
  "specialRequests": "웨딩카 장식 요청"
}
```

### 응답 예시
```json
{
  "status": 201,
  "success": true,
  "message": "계약이 성공적으로 생성되었습니다.",
  "data": {
    "contractId": 1,
    "vendorName": "웨딩홀 ABC",
    "memberName": "김신랑",
    "contractDate": "2025-09-04",
    "contractTime": "10:00:00",
    "status": "PENDING",
    "createdAt": "2025-09-06T15:30:00",
    "specialRequests": "웨딩카 장식 요청",
    "message": "2025년 9월 4일 10:00:00 시간대로 계약이 완료되었습니다."
  }
}
```

### cURL 예시 (초간단!)
```bash
curl -X POST "http://localhost:8080/api/v1/contracts/1/simple-create?memberId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "contractDate": "2025-09-04",
    "contractTime": "10:00:00",
    "specialRequests": "웨딩카 장식 요청"
  }'
```

### 핵심 특징 🎯
- **초간단**: 날짜 + 시간만 입력하면 끝!
- **자동 설정**: 가격, 시간대(4시간), 계약금(20%) 자동 계산
- **중복 방지**: 이미 예약된 시간대는 계약 불가
- **즉시 완료**: 복잡한 절차 없이 바로 계약 생성

### 자동 설정값
- **종료 시간**: 시작 시간 + 4시간
- **총 금액**: 업체 최소 금액
- **계약금**: 총 금액의 20%
- **계약 상태**: PENDING (대기중)

### 에러 응답
```json
{
  "status": 409,
  "success": false,
  "message": "해당 시간대는 이미 예약되어 있습니다."
}
```

### 사용 예시
```
2025.09.04 10:00 → 계약 완료!
2025.12.25 14:00 → 계약 완료!
```

이제 정말 간단하게 "2025.09.04 10:00"만 입력하면 계약 완료! 🎉
