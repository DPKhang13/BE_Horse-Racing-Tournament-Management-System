# Referee Report & Race Result Flow Test Guide

File nay dung de test luong trong tai bao cao ket qua race, admin xac nhan ket qua, publish ket qua, tinh diem cho ngua/jockey va settle bet.

## 1. Dieu Kien Chuan Bi

Can co san:

- Token `ADMIN`.
- Token `RACE_REFEREE`.
- Race da co ngua/jockey tham gia voi `JockeyHorseAssignment.status = confirmed`.
- Race da duoc assign referee.
- Race dang o status `in_progress`.
- Neu race co betting thi bet dang o trang thai hop le truoc khi publish.

Bien test mau:

```text
BASE_URL=http://localhost:8080
raceId=1
reportId=1
assignmentId1=1
assignmentId2=2
adminToken=Bearer <ADMIN_TOKEN>
refereeToken=Bearer <RACE_REFEREE_TOKEN>
```

## 2. Tom Tat API Lien Quan

| Step | Role | Method | API | Muc dich |
| --- | --- | --- | --- | --- |
| Create race | ADMIN | POST | `/api/v1/admin/schedules/{scheduleId}/create-race` | Tao race. Khong con truyen `pointRules` trong body create race. |
| Create point rules | ADMIN | POST | `/api/v1/admin/races/{raceId}/point-rules/create` | Tao barem diem theo hang cho race. |
| Get point rules | ADMIN | GET | `/api/v1/admin/races/{raceId}/point-rules/get` | Xem barem diem cua race. |
| Replace point rules | ADMIN | PUT | `/api/v1/admin/races/{raceId}/point-rules/update` | Thay toan bo barem diem cua race. |
| Delete point rule | ADMIN | DELETE | `/api/v1/admin/races/{raceId}/point-rules/delete/{ruleId}` | Xoa mot rule diem. |
| Assign referee | ADMIN | POST | `/api/v1/admin/races/{raceId}/create-referee-assignment` | Gan referee vao race. |
| Get referee assignments | ADMIN | GET | `/api/v1/admin/races/{raceId}/get-referee-assignment-list` | Xem danh sach referee cua race. |
| Start race | ADMIN | PATCH | `/api/v1/admin/races/{raceId}/start` | Chuyen race sang `in_progress`. |
| Get my assigned races | RACE_REFEREE | GET | `/api/v1/referee/races/get-my-assigned` | Referee xem race duoc phan cong. |
| Create report | RACE_REFEREE | POST | `/api/v1/referee/races/{raceId}/reports/create` | Referee tao bao cao race. |
| Get reports | RACE_REFEREE | GET | `/api/v1/referee/races/{raceId}/reports/get` | Referee xem report cua race. |
| Create draft result | RACE_REFEREE | POST | `/api/v1/referee/races/{raceId}/results/draft/create` | Chief/main referee nhap ket qua draft. |
| Update draft result | RACE_REFEREE | PUT | `/api/v1/referee/races/{raceId}/results/draft/update` | Chief/main referee sua ket qua draft. |
| Get draft result | RACE_REFEREE | GET | `/api/v1/referee/races/{raceId}/results/draft/get` | Referee xem ket qua draft. |
| Admin get results | ADMIN | GET | `/api/v1/admin/races/{raceId}/results/get` | Admin xem ket qua hien tai. |
| Confirm results | ADMIN | PATCH | `/api/v1/admin/races/{raceId}/results/confirm` | Admin xac nhan ket qua va tinh `pointsAwarded`. |
| Cancel results | ADMIN | PATCH | `/api/v1/admin/races/{raceId}/results/cancel` | Admin huy ket qua draft/confirmed khi chua publish. |
| Publish results | ADMIN | PATCH | `/api/v1/admin/races/{raceId}/results/publish` | Publish ket qua, cong diem, cong win, settle bet, complete race. |
| Public results | Public | GET | `/api/v1/races/{raceId}/results/public/get` | Xem ket qua da publish. |

## 3. Luong Test Chinh

### Step 1: Admin tao point rules

Endpoint:

```http
POST /api/v1/admin/races/{raceId}/point-rules/create
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json
```

Body:

```json
[
  {
    "finishPosition": 1,
    "points": 100,
    "note": "Winner"
  },
  {
    "finishPosition": 2,
    "points": 70,
    "note": "Second place"
  },
  {
    "finishPosition": 3,
    "points": 40,
    "note": "Third place"
  }
]
```

Expected:

- HTTP `201`.
- Response tra ve list point rules.
- Moi `finishPosition` trong cung race khong duoc trung.
- Neu race da `in_progress`/`completed` thi khong nen cho sua barem diem nua.

### Step 2: Admin start race

Endpoint:

```http
PATCH /api/v1/admin/races/{raceId}/start
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json
```

Body neu can force close betting:

```json
{
  "forceCloseBetting": true
}
```

Expected:

- Race chuyen sang `in_progress`.
- Neu race dang `open_for_betting` ma `predictionClosesAt = null` thi bi chan voi message:

```text
Prediction close time is required before starting race from betting status
```

### Step 3: Referee xem race duoc phan cong

Endpoint:

```http
GET /api/v1/referee/races/get-my-assigned
Authorization: Bearer <RACE_REFEREE_TOKEN>
```

Expected:

- Response co race can report.
- Referee assignment hop le moi duoc tinh la assigned/active.

### Step 4: Referee tao report

Endpoint:

```http
POST /api/v1/referee/races/{raceId}/reports/create
Authorization: Bearer <RACE_REFEREE_TOKEN>
Content-Type: application/json
```

Body khong co violation:

```json
{
  "reportType": "final",
  "inspectionNotes": "All horses passed inspection.",
  "violationNotes": null,
  "resultNotes": "Race completed normally.",
  "verdict": "clean"
}
```

Body co violation:

```json
{
  "reportType": "final",
  "inspectionNotes": "Horse 2 crossed lane boundary.",
  "violationNotes": "Horse 2 violated lane rule at lap 2.",
  "resultNotes": "Horse 2 should be disqualified.",
  "verdict": "violation"
}
```

Expected:

- HTTP `201`.
- Race phai dang `in_progress`.
- Referee phai duoc assign vao race.
- Neu `verdict = violation` thi bat buoc co `violationNotes`.

### Step 5: Chief/main referee tao draft result

Endpoint:

```http
POST /api/v1/referee/races/{raceId}/results/draft/create
Authorization: Bearer <RACE_REFEREE_TOKEN>
Content-Type: application/json
```

Body:

```json
{
  "reportId": 1,
  "results": [
    {
      "assignmentId": 1,
      "finishPosition": 1,
      "finishTimeSec": 72.531,
      "isDisqualified": false,
      "disqualifyReason": null
    },
    {
      "assignmentId": 2,
      "finishPosition": 2,
      "finishTimeSec": 74.208,
      "isDisqualified": false,
      "disqualifyReason": null
    },
    {
      "assignmentId": 3,
      "finishPosition": null,
      "finishTimeSec": 76.900,
      "isDisqualified": true,
      "disqualifyReason": "Lane violation"
    }
  ]
}
```

Expected:

- HTTP `201`.
- Tao result status `draft`.
- Chi `chief_referee` hoac `main_referee` duoc create/update draft.
- Moi confirmed jockey-horse assignment trong race phai co dung 1 result.
- Result DQ co `pointsAwarded = 0` va `finishPosition = null`.
- Result khong DQ bat buoc co `finishPosition`.
- Backend khong tu don hang khi co DQ. Final position la vi tri chinh thuc do referee/admin nhap.

### Step 6: Referee xem draft result

Endpoint:

```http
GET /api/v1/referee/races/{raceId}/results/draft/get
Authorization: Bearer <RACE_REFEREE_TOKEN>
```

Expected:

- Response tra ve danh sach result dang `draft`.
- Referee duoc assign vao race co the xem draft.

### Step 7: Admin xem result truoc confirm

Endpoint:

```http
GET /api/v1/admin/races/{raceId}/results/get
Authorization: Bearer <ADMIN_TOKEN>
```

Expected:

- Response tra ve result draft hien tai.
- Admin dung report/violation de quyet dinh co confirm, cancel, hoac yeu cau referee update draft.

### Step 8: Admin confirm result

Endpoint:

```http
PATCH /api/v1/admin/races/{raceId}/results/confirm
Authorization: Bearer <ADMIN_TOKEN>
```

Expected:

- Result chuyen tu `draft` sang `confirmed`.
- He thong tinh `pointsAwarded` dua tren `race_point_rules`.
- Neu thieu point rule cho mot `finishPosition` thi bao loi va khong confirm:

```text
Missing point rule for finish position
```

- Phai co dung 1 winner hop le: `finishPosition = 1` va khong DQ.
- Non-DQ positions phai lien tuc tu `1..N`.
- DQ khong nhan diem.
- Buoc confirm chua cong diem ranking, chua settle bet, chua complete race.

### Step 9: Admin publish result

Endpoint:

```http
PATCH /api/v1/admin/races/{raceId}/results/publish
Authorization: Bearer <ADMIN_TOKEN>
```

Expected:

- Result chuyen sang `published`.
- Race chuyen sang `completed`.
- Cong `pointsAwarded` vao `horses.ranking_points`.
- Cong `pointsAwarded` vao `jockeys.ranking_points`.
- Winner horse va winner jockey duoc tang `totalWins`.
- Bet pending cua race duoc settle:
  - Bet dung winner horse: `won`, wallet duoc cong reward.
  - Bet sai winner horse: `lost`.
- Tao notification lien quan.

Response mau:

```json
{
  "raceId": 1,
  "raceName": "Race 1",
  "raceStatus": "completed",
  "publishedAt": "2026-06-28T17:00:00+07:00",
  "totalResults": 3,
  "winnerHorseId": 10,
  "winnerHorseName": "Thunder",
  "winnerJockeyId": 5,
  "winnerJockeyName": "Nguyen Van A",
  "totalBetsSettled": 12,
  "totalRewardsPaid": 3500,
  "message": "Race results published successfully"
}
```

### Step 10: Public xem result

Endpoint:

```http
GET /api/v1/races/{raceId}/results/public/get
```

Expected:

- Chi tra ve result khi da `published`.
- Neu chua publish thi bao loi:

```text
Published results are not available for this race
```

## 4. Cac Case Can Test Ky

### Case 1: Violation report khong co violation notes

Body:

```json
{
  "reportType": "final",
  "inspectionNotes": "Violation happened.",
  "violationNotes": null,
  "resultNotes": "Need admin review.",
  "verdict": "violation"
}
```

Expected:

- HTTP `400`.
- Khong tao report.

### Case 2: Referee khong phai chief/main tao draft result

Expected:

- HTTP `403` hoac `400` tuy exception mapping.
- Khong tao result draft.

### Case 3: Thieu result cho assignment da confirmed

Expected:

- HTTP `400`.
- Message nen the hien result khong du cho confirmed assignments.

### Case 4: Trung finishPosition

Body co 2 result cung `"finishPosition": 1`.

Expected:

- HTTP `400`.
- Khong tao/update draft.

### Case 5: Non-DQ position khong lien tuc

Vi du co position `1` va `3`, thieu `2`.

Expected:

- HTTP `400`.
- Khong confirm/publish.

### Case 6: DQ nhung khong co disqualifyReason

Expected:

- HTTP `400`.
- DQ result phai co ly do.

### Case 7: Thieu point rule

Vi du result co position `3` nhung race chi co point rule cho position `1`, `2`.

Expected:

- HTTP `400`.
- Message:

```text
Missing point rule for finish position
```

### Case 8: Khong co winner hop le

Vi du tat ca result deu DQ, hoac khong co ai position `1`.

Expected:

- HTTP `400`.
- Khong confirm/publish.

### Case 9: Publish lan 2

Expected:

- HTTP `400`.
- Khong cong diem/win/reward lan 2.

### Case 10: Public result truoc publish

Expected:

- HTTP `400` hoac `404` tuy exception mapping.
- Message:

```text
Published results are not available for this race
```

### Case 11: Dung API complete race cu

Endpoint:

```http
PATCH /api/v1/admin/races/{raceId}/complete
```

Expected:

- Bi chan.
- Message:

```text
Use race result publish workflow to complete race
```

### Case 12: Dung API publish result cu

Endpoint:

```http
PUT /api/race-results/publish/{id}
```

Expected:

- Bi chan.
- Message:

```text
Use race result publish workflow to publish results
```

## 5. Checklist Sau Khi Publish

Kiem tra database/API sau publish:

- `races.status = completed`.
- Tat ca active race results cua race co `status = published`.
- `race_results.published_at` co gia tri.
- `race_results.points_awarded` dung voi `race_point_rules`.
- Horse/Jockey DQ khong duoc cong diem.
- Horse/Jockey khong DQ duoc cong diem theo final position.
- Winner horse `total_wins` tang 1.
- Winner jockey `total_wins` tang 1.
- Pending bets cua race khong con pending.
- Bet dung winner horse co status `won`.
- Bet sai winner horse co status `lost`.
- Wallet cua user thang bet duoc cong reward.
- Co wallet transaction type `reward`.
- Public API tra ve result da publish.

## 6. Ghi Chu Nghiep Vu

- `race_point_rules` la barem diem goc do admin tao truoc race.
- Referee report chi la bao cao race/violation/result note.
- Admin confirm result se tinh diem chinh thuc dua tren final position va point rules.
- Backend khong tu don hang khi co DQ.
- Referee/admin phai nhap final position chinh thuc.
- DQ result co `finishPosition = null`, `pointsAwarded = 0`.
- Cancel result khong refund bet. Refund neu co nen nam trong flow huy race rieng.
