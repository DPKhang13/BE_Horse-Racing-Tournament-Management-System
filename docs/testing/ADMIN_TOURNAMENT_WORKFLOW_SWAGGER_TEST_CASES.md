# Swagger Test Cases - Admin Tournament Workflow

Mục tiêu: test luồng quản lý giải đấu theo trạng thái rõ ràng:

```text
create tournament
-> create schedule/race
-> create AdminPointRule
-> create prize
-> open registration
-> owner register
-> admin approve/reject
-> owner invite jockey
-> jockey accept
-> owner confirm assignment/registration
-> close registration
-> assign referee
-> tournament in progress
-> race in progress
-> race result/publish
-> race completed
-> tournament completed
```

Base URL:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## 0. Chuẩn bị token và ID

Login đúng format:

```http
POST /api/auth/login
```

Admin body:

```json
{
  "usernameOrEmail": "admin@example.com",
  "password": "123456"
}
```

Owner body:

```json
{
  "usernameOrEmail": "owner1@example.com",
  "password": "123456"
}
```

Jockey body:

```json
{
  "usernameOrEmail": "jockey1@example.com",
  "password": "123456"
}
```

Lưu lại:

```text
ADMIN_TOKEN=
OWNER_TOKEN=
JOCKEY_TOKEN=
REFEREE_TOKEN=

tournamentId=
scheduleId=
raceId=
ruleId1=
prizeId=
horseId=
registrationId=
assignmentId=
refereeId=
raceResultId=
```

Header cho API cần đăng nhập:

```http
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

## 1. Admin tạo tournament

Endpoint:

```http
POST /api/tournaments/create-tournament
```

Role:

```text
ADMIN
```

Body:

```json
{
  "name": "Admin Workflow Tournament 2026",
  "location": "Ho Chi Minh City Racecourse",
  "startDate": "2026-07-10",
  "endDate": "2026-07-12",
  "prizePool": 1000000,
  "status": "upcoming"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu tournamentId.
tournament.status = upcoming.
```

Kiểm tra lại:

```http
GET /api/tournaments/get-tournament/{tournamentId}
```

## 2. Admin tạo schedule cho tournament

Endpoint:

```http
POST /api/v1/admin/tournaments/{tournamentId}/create-schedule
```

Role:

```text
ADMIN
```

Body:

```json
{
  "raceDate": "2026-07-10",
  "dayNumber": 1,
  "title": "Race Day 1",
  "note": "Opening day for admin workflow test"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu scheduleId.
```

Kiểm tra lại:

```http
GET /api/v1/admin/tournaments/{tournamentId}/get-schedule-list
```

## 3. Admin tạo race trong schedule

Endpoint:

```http
POST /api/v1/admin/schedules/{scheduleId}/create-race
```

Role:

```text
ADMIN
```

Body:

```json
{
  "name": "Admin Workflow Opening Race",
  "raceNumber": 1,
  "rankGroup": "A",
  "lapCount": 3,
  "scheduledAt": "2026-07-10T09:00:00Z",
  "predictionClosesAt": "2026-07-10T08:50:00Z",
  "distanceM": 1200,
  "trackType": "grass",
  "maxHorses": 8,
  "maxReferees": 3,
  "pointRuleNote": "Point rules are configured by AdminPointRule API",
  "status": "scheduled"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu raceId.
race.status = scheduled.
```

Kiểm tra lại:

```http
GET /api/v1/admin/races/get-race/{raceId}
```

## 4. Admin tạo điểm AdminPointRule cho race

Nên tạo point rule sau khi race đã được tạo và trước khi race bắt đầu.

### 4.1. Create point rules

Endpoint:

```http
POST /api/v1/admin/races/{raceId}/point-rules/create
```

Role:

```text
ADMIN
```

Body:

```json
[
  {
    "finishPosition": 1,
    "points": 10,
    "note": "First place"
  },
  {
    "finishPosition": 2,
    "points": 7,
    "note": "Second place"
  },
  {
    "finishPosition": 3,
    "points": 5,
    "note": "Third place"
  },
  {
    "finishPosition": 4,
    "points": 3,
    "note": "Fourth place"
  }
]
```

Kết quả mong đợi:

```text
HTTP 201
Response trả danh sách point rules theo race.
Lưu ruleId1 nếu muốn test delete.
```

### 4.2. Get point rules

Endpoint:

```http
GET /api/v1/admin/races/{raceId}/point-rules/get
```

Kết quả mong đợi:

```text
HTTP 200
Danh sách point rule sắp theo finishPosition tăng dần.
```

### 4.3. Replace point rules

Endpoint:

```http
PUT /api/v1/admin/races/{raceId}/point-rules/update
```

Body:

```json
[
  {
    "finishPosition": 1,
    "points": 12,
    "note": "Champion"
  },
  {
    "finishPosition": 2,
    "points": 8,
    "note": "Runner up"
  },
  {
    "finishPosition": 3,
    "points": 5,
    "note": "Third place"
  }
]
```

Kết quả mong đợi:

```text
HTTP 200
Point rules cũ bị replace bằng danh sách mới.
```

### 4.4. Delete một point rule

Endpoint:

```http
DELETE /api/v1/admin/races/{raceId}/point-rules/delete/{ruleId}
```

Kết quả mong đợi:

```text
HTTP 204
Rule bị xóa nếu rule thuộc đúng raceId.
```

### 4.5. Validation point rules

Các case cần test:

```text
Body null/rỗng -> HTTP 400, Point rules are required.
finishPosition null -> HTTP 400.
finishPosition < 1 -> HTTP 400.
points null -> HTTP 400.
points < 0 -> HTTP 400.
Duplicate finishPosition trong cùng request -> HTTP 400, Duplicate finish position in point rules.
Create thêm finishPosition đã tồn tại -> HTTP 400, Finish position already exists in race point rules.
Update/delete khi race status = in_progress/completed/cancelled -> HTTP 400, Cannot update point rules after race has started.
```

Ghi chú nghiệp vụ:

```text
RaceResult sẽ tự apply pointsAwarded theo point rule dựa trên raceId + finishPosition.
Nếu không có point rule cho finishPosition thì pointsAwarded mặc định là 0.
```

## 5. Admin tạo prize cho tournament

Endpoint:

```http
POST /api/v1/admin/tournaments/{tournamentId}/create-prizes
```

Role:

```text
ADMIN
```

Body:

```json
{
  "prizes": [
    {
      "finishPosition": 1,
      "prizeName": "Champion Prize",
      "prizeAmount": 500000
    },
    {
      "finishPosition": 2,
      "prizeName": "Runner-up Prize",
      "prizeAmount": 300000
    },
    {
      "finishPosition": 3,
      "prizeName": "Third Place Prize",
      "prizeAmount": 200000
    }
  ]
}
```

Kết quả mong đợi:

```text
HTTP 201
Tổng prizeAmount không vượt tournament.prizePool.
finishPosition không trùng trong cùng tournament.
```

Kiểm tra lại:

```http
GET /api/v1/admin/tournaments/{tournamentId}/get-prizes
```

## 6. Admin mở đăng ký tournament

Endpoint:

```http
PATCH /api/v1/admin/tournaments/{tournamentId}/open-registration
```

Role:

```text
ADMIN
```

Body:

```json
{
  "registrationOpenAt": "2026-06-27T00:00:00Z",
  "registrationCloseAt": "2026-07-08T23:59:59Z"
}
```

Kết quả mong đợi:

```text
HTTP 200
tournament.status = registration_open.
race.status = registration_open.
```

Kiểm tra lại:

```http
GET /api/tournaments/get-tournament/{tournamentId}
GET /api/v1/admin/races/get-race/{raceId}
```

## 7. Owner tạo horse

Endpoint:

```http
POST /api/horses/create
```

Role:

```text
HORSE_OWNER
```

Body:

```json
{
  "name": "Admin Workflow Thunder",
  "breed": "Arabian",
  "age": 4,
  "weightKg": 450.5,
  "rankGroup": "A",
  "avatarUrl": "https://example.com/horses/admin-workflow-thunder.png"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu horseId.
Horse owner lấy từ token.
Horse rankGroup phải khớp race.rankGroup.
```

## 8. Owner đăng ký horse vào race

Endpoint:

```http
POST /api/race-registrations/create
```

Role:

```text
HORSE_OWNER
```

Body:

```json
{
  "tournamentId": 1,
  "raceId": 1,
  "horseId": 1
}
```

Thay `1` bằng `tournamentId`, `raceId`, `horseId`.

Kết quả mong đợi:

```text
HTTP 201
Lưu registrationId.
registration.status = pending.
Không gửi jockeyId ở bước đăng ký.
```

## 9. Admin approve hoặc reject registration

### 9.1. Approve registration

Endpoint:

```http
PATCH /api/v1/admin/race-registrations/{registrationId}/approve
```

Role:

```text
ADMIN
```

Body optional:

```json
{
  "note": "Horse is eligible"
}
```

Kết quả mong đợi:

```text
HTTP 200
registration.status = approved.
approvedAt có giá trị.
approvedById là admin hiện tại.
```

### 9.2. Reject registration

Endpoint:

```http
PATCH /api/v1/admin/race-registrations/{registrationId}/reject
```

Body optional:

```json
{
  "reason": "Horse does not meet requirements"
}
```

Kết quả mong đợi:

```text
HTTP 200
registration.status = rejected.
```

## 10. Owner invite jockey

Endpoint:

```http
POST /api/jockey-assignments/create-invitation
```

Role:

```text
HORSE_OWNER
```

Body:

```json
{
  "registrationId": 1,
  "raceId": 1,
  "jockeyId": 3,
  "gateNumber": 1
}
```

Thay bằng ID thật.

Kết quả mong đợi:

```text
HTTP 201
Lưu assignmentId.
assignment.status = pending.
responseDeadline có giá trị.
```

## 11. Jockey accept invitation

Endpoint:

```http
PUT /api/jockey-assignments/respond/{assignmentId}
```

Role:

```text
JOCKEY
```

Body:

```json
{
  "status": "accepted"
}
```

Kết quả mong đợi:

```text
HTTP 200
assignment.status = accepted.
Chưa chính thức cho tới khi owner confirm.
```

## 12. Owner confirm assignment/registration

Endpoint:

```http
PATCH /api/v1/owner/jockey-assignments/{assignmentId}/confirm
```

Role:

```text
HORSE_OWNER
```

Kết quả mong đợi:

```text
HTTP 200
assignment.status = confirmed.
registration.ownerConfirmationStatus = confirmed.
registration.ownerConfirmedAt có giá trị.
registration.jockey được set.
```

## 13. Admin close registration

Endpoint:

```http
PATCH /api/v1/admin/tournaments/{tournamentId}/close-registration
```

Role:

```text
ADMIN
```

Body:

```json
{
  "autoRejectPending": true,
  "autoCancelUnconfirmed": false
}
```

Kết quả mong đợi:

```text
HTTP 200
tournament.status = registration_closed.
Race có assignment confirmed -> race.status = ready.
Race không có assignment confirmed -> race.status = registration_closed.
```

Kiểm tra race:

```http
GET /api/v1/admin/races/get-race/{raceId}
```

## 14. Admin assign referee

Endpoint:

```http
POST /api/v1/admin/races/{raceId}/create-referee-assignment
```

Role:

```text
ADMIN
```

Body:

```json
{
  "refereeId": 4,
  "refereeRole": "finish_judge"
}
```

Kết quả mong đợi:

```text
HTTP 201
Race phải có status registration_closed hoặc ready.
Race phải có ít nhất một jockey assignment confirmed.
Nếu entity có status thì status = assigned.
```

Kiểm tra lại:

```http
GET /api/v1/admin/races/{raceId}/get-referee-assignment-list
```

## 15. Admin start race

Trước khi start race, admin chuyển tournament sang `in_progress`.

Endpoint:

```http
PATCH /api/v1/admin/tournaments/{tournamentId}/start
```

Role:

```text
ADMIN
```

Kết quả mong đợi:

```text
HTTP 200
tournament.status = in_progress.
Chỉ start được khi tournament.status = registration_closed.
Tournament phải có ít nhất một race ở trạng thái ready/open_for_betting/in_progress.
```

## 16. Admin start race

Endpoint:

```http
PATCH /api/v1/admin/races/{raceId}/start
```

Role:

```text
ADMIN
```

Body optional:

```json
{
  "forceCloseBetting": true,
  "note": "Start admin workflow race"
}
```

Kết quả mong đợi:

```text
HTTP 200
race.status = in_progress.
```

Sau bước này không còn được sửa point rules:

```http
PUT /api/v1/admin/races/{raceId}/point-rules/update
```

Kỳ vọng:

```text
HTTP 400
Cannot update point rules after race has started.
```

## 17. Admin hoặc referee tạo race result

Endpoint:

```http
POST /api/race-results/create
```

Role:

```text
ADMIN hoặc RACE_REFEREE
```

Body:

```json
{
  "assignmentId": 1,
  "reportId": null,
  "finalRound": 3,
  "finishPosition": 1,
  "finishTimeSec": 96.25,
  "pointsAwarded": null,
  "isDisqualified": false,
  "disqualifyReason": null,
  "status": "draft",
  "recordedAt": "2026-07-10T09:30:00Z"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu raceResultId.
pointsAwarded được tự apply theo AdminPointRule:
finishPosition = 1 -> pointsAwarded = 12 nếu đã replace rule ở bước 4.3.
Nếu không có rule cho finishPosition thì pointsAwarded = 0.
```

## 18. Admin hoặc referee publish race result

Endpoint:

```http
PUT /api/race-results/publish/{raceResultId}
```

Role:

```text
ADMIN hoặc RACE_REFEREE
```

Body:

```json
{
  "status": "published",
  "publishedAt": "2026-07-10T10:00:00Z"
}
```

Kết quả mong đợi:

```text
HTTP 200
raceResult.status = published.
publishedAt có giá trị.
pointsAwarded vẫn theo point rule.
```

## 19. Admin complete race

Endpoint:

```http
PATCH /api/v1/admin/races/{raceId}/complete
```

Role:

```text
ADMIN
```

Kết quả mong đợi:

```text
HTTP 200
race.status = completed.
Chỉ complete được khi race.status = in_progress.
Race phải có ít nhất một race result status = published.
```

## 20. Admin complete tournament

Endpoint:

```http
PATCH /api/v1/admin/tournaments/{tournamentId}/complete
```

Role:

```text
ADMIN
```

Kết quả mong đợi:

```text
HTTP 200
tournament.status = completed.
Chỉ complete được khi tournament.status = in_progress.
Tournament phải có ít nhất một race completed.
Tất cả race trong tournament phải completed hoặc cancelled.
```

## 21. Checklist test nhanh toàn luồng

```text
1. POST  /api/auth/login với admin.
2. POST  /api/tournaments/create-tournament.
3. POST  /api/v1/admin/tournaments/{tournamentId}/create-schedule.
4. POST  /api/v1/admin/schedules/{scheduleId}/create-race.
5. POST  /api/v1/admin/races/{raceId}/point-rules/create.
6. GET   /api/v1/admin/races/{raceId}/point-rules/get.
7. POST  /api/v1/admin/tournaments/{tournamentId}/create-prizes.
8. PATCH /api/v1/admin/tournaments/{tournamentId}/open-registration.
9. POST  /api/horses/create bằng owner.
10. POST /api/race-registrations/create bằng owner.
11. PATCH /api/v1/admin/race-registrations/{registrationId}/approve.
12. POST /api/jockey-assignments/create-invitation bằng owner.
13. PUT  /api/jockey-assignments/respond/{assignmentId} bằng jockey.
14. PATCH /api/v1/owner/jockey-assignments/{assignmentId}/confirm.
15. PATCH /api/v1/admin/tournaments/{tournamentId}/close-registration.
16. POST /api/v1/admin/races/{raceId}/create-referee-assignment.
17. PATCH /api/v1/admin/tournaments/{tournamentId}/start.
18. PATCH /api/v1/admin/races/{raceId}/start.
19. POST /api/race-results/create.
20. PUT  /api/race-results/publish/{raceResultId}.
21. PATCH /api/v1/admin/races/{raceId}/complete.
22. PATCH /api/v1/admin/tournaments/{tournamentId}/complete.
```

## 22. Negative cases bắt buộc

```text
1. Create point rules với body [] -> HTTP 400.
2. Create point rules trùng finishPosition trong body -> HTTP 400.
3. Create point rule trùng finishPosition đã tồn tại -> HTTP 400.
4. Update/delete point rules sau khi race in_progress -> HTTP 400.
5. Open registration khi tournament không upcoming -> HTTP 400.
6. Owner register khi tournament/race chưa registration_open -> HTTP 400.
7. Owner register horse rankGroup không khớp race -> HTTP 400.
8. Owner register trùng horse trong cùng tournament -> HTTP 400.
9. Approve registration không pending -> HTTP 400.
10. Invite jockey khi registration chưa approved -> HTTP 400.
11. Jockey accept invitation quá deadline -> status expired và HTTP 400.
12. Owner confirm assignment chưa accepted -> HTTP 400.
13. Close registration khi có approved registration chưa confirmed và autoCancelUnconfirmed=false -> HTTP 400.
14. Assign referee khi race chưa registration_closed/ready -> HTTP 400.
15. Assign referee khi race chưa có confirmed assignment -> HTTP 400.
16. Start race khi race chưa ready/open_for_betting -> HTTP 400.
17. Start tournament khi tournament chưa registration_closed -> HTTP 400.
18. Complete race khi chưa publish result -> HTTP 400.
19. Complete tournament khi còn race chưa completed/cancelled -> HTTP 400.
```
