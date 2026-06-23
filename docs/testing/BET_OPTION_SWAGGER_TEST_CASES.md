# Swagger Test Cases - Race, Prize, BetOption, Bet Flow

Tài liệu này dùng để test thủ công trên Swagger cho các chức năng đã implement gần đây: Tournament, Race Schedule, Race, Race Registration, Jockey Assignment, Prize, BetOption auto-generation, Bet, WalletTransaction liên quan khi đặt cược, và các API public count cho landing page.

Base URL:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## 0. Quy ước trước khi test

Các giá trị cần lưu lại trong lúc test:

```text
ADMIN_TOKEN=
OWNER_TOKEN=
JOCKEY_TOKEN=
SPECTATOR_TOKEN=

tournamentId=
scheduleId=
raceId=
horseId1=
horseId2=
registrationId1=
registrationId2=
assignmentId1=
assignmentId2=
optionId1=
optionId2=
betId=
prizeId=
```

Header cho API cần đăng nhập:

```http
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

Role cần có:

```text
ADMIN: tạo tournament, schedule, race, prize, generate/update bet option.
HORSE_OWNER: tạo horse, đăng ký race, mời jockey.
JOCKEY: accept/reject invitation.
SPECTATOR hoặc user có ví: đặt cược.
```

Lưu ý quan trọng khi test Swagger:

```text
PUT /api/v1/admin/races/update-race/{raceId}
```

`RaceUpdateRequest` cho phép gửi partial body. Khi chỉ muốn đổi status sang `open_for_betting`, chỉ gửi:

```json
{
  "status": "open_for_betting"
}
```

Không dùng nguyên body mẫu Swagger nếu có `distanceM: 0.1`, vì backend validate `distanceM >= 1.0` và sẽ trả:

```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "fields": {
    "distanceM": "Distance must be greater than 0"
  },
  "status": 400
}
```

## 1. Login lấy token

### 1.1. Login ADMIN

Endpoint:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "admin@example.com",
  "password": "123456"
}
```

Kết quả mong đợi:

```text
HTTP 200
Response có accessToken.
Lưu accessToken vào ADMIN_TOKEN.
```

### 1.2. Login HORSE_OWNER

Endpoint:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "owner@example.com",
  "password": "123456"
}
```

Kết quả mong đợi:

```text
HTTP 200
Lưu accessToken vào OWNER_TOKEN.
```

### 1.3. Login JOCKEY

Endpoint:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "jockey@example.com",
  "password": "123456"
}
```

Kết quả mong đợi:

```text
HTTP 200
Lưu accessToken vào JOCKEY_TOKEN.
```

### 1.4. Login SPECTATOR

Endpoint:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "spectator@example.com",
  "password": "123456"
}
```

Kết quả mong đợi:

```text
HTTP 200
Lưu accessToken vào SPECTATOR_TOKEN.
```

## 2. Tournament

### 2.1. Tạo tournament

Role:

```text
ADMIN
```

Endpoint:

```http
POST /api/tournaments/create-tournament
```

Body:

```json
{
  "name": "Spring Derby Championship 2026",
  "location": "Ho Chi Minh City Racecourse",
  "startDate": "2026-07-01",
  "endDate": "2026-07-03",
  "prizePool": 1000000,
  "status": "upcoming"
}
```

Kết quả mong đợi:

```text
HTTP 201
Response có tournamentId/id.
Lưu lại tournamentId.
```

### 2.2. Lấy chi tiết tournament

Endpoint:

```http
GET /api/tournaments/get-tournament/{tournamentId}
```

Kết quả mong đợi:

```text
HTTP 200
Response có thông tin tournament, schedules và prizes nếu đã có.
```

### 2.3. Lấy danh sách tournament

Endpoint:

```http
GET /api/tournaments/get-tournament-list
```

Hoặc lọc theo status:

```http
GET /api/tournaments/get-tournament-list?status=upcoming
```

Kết quả mong đợi:

```text
HTTP 200
Response là danh sách tournament summary.
```

## 3. Prize

### 3.1. Tạo prizes cho tournament

Role:

```text
ADMIN
```

Endpoint:

```http
POST /api/v1/admin/tournaments/{tournamentId}/create-prizes
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
Response có 3 prize.
Lưu prizeId của một prize để test update/delete.
```

Validation cần test:

```text
prizes rỗng -> HTTP 400.
finishPosition <= 0 -> HTTP 400.
prizeName blank -> HTTP 400.
prizeAmount <= 0 -> HTTP 400.
Tạo trùng finishPosition trong cùng tournament -> HTTP 400.
Tổng prizeAmount vượt prizePool của tournament -> HTTP 400.
```

### 3.2. Lấy danh sách prizes

Endpoint:

```http
GET /api/v1/admin/tournaments/{tournamentId}/get-prizes
```

Kết quả mong đợi:

```text
HTTP 200
Danh sách prize sắp theo finishPosition tăng dần.
```

### 3.3. Lấy một prize

Endpoint:

```http
GET /api/v1/admin/tournaments/{tournamentId}/get-prize/{prizeId}
```

Kết quả mong đợi:

```text
HTTP 200
Response đúng prizeId và tournamentId.
```

### 3.4. Update prize

Endpoint:

```http
PUT /api/v1/admin/tournaments/{tournamentId}/update-prize/{prizeId}
```

Body:

```json
{
  "finishPosition": 1,
  "prizeName": "Champion Prize Updated",
  "prizeAmount": 550000
}
```

Kết quả mong đợi:

```text
HTTP 200
Response có prizeName/prizeAmount mới.
```

Validation cần test:

```text
Update sang finishPosition đã tồn tại ở prize khác -> HTTP 400.
Update prizeAmount <= 0 -> HTTP 400.
Update tổng prizeAmount vượt prizePool -> HTTP 400.
prizeId không thuộc tournamentId -> HTTP 404 hoặc bad request theo handler hiện tại.
```

### 3.5. Delete prize

Endpoint:

```http
DELETE /api/v1/admin/tournaments/{tournamentId}/delete-prize/{prizeId}
```

Kết quả mong đợi:

```json
{
  "message": "Prize deleted successfully"
}
```

## 4. Tournament schedule và Race

### 4.1. Tạo schedule cho tournament

Role:

```text
ADMIN
```

Endpoint:

```http
POST /api/v1/admin/tournaments/{tournamentId}/create-schedule
```

Body:

```json
{
  "raceDate": "2026-07-01",
  "dayNumber": 1,
  "title": "Race Day 1",
  "note": "Opening day"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu scheduleId.
```

Validation cần test:

```text
raceDate nằm ngoài startDate/endDate của tournament -> HTTP 400.
dayNumber <= 0 -> HTTP 400.
Trùng dayNumber trong cùng tournament -> HTTP 400.
Trùng raceDate trong cùng tournament -> HTTP 400.
```

### 4.2. Tạo race trong schedule

Endpoint:

```http
POST /api/v1/admin/schedules/{scheduleId}/create-race
```

Body:

```json
{
  "name": "Opening Race",
  "raceNumber": 1,
  "rankGroup": "A",
  "lapCount": 3,
  "scheduledAt": "2026-07-01T09:00:00Z",
  "predictionClosesAt": "2026-07-01T08:50:00Z",
  "distanceM": 1200,
  "trackType": "grass",
  "maxHorses": 8,
  "maxReferees": 3,
  "pointRuleNote": "Standard points",
  "status": "scheduled"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu raceId.
```

Validation cần test:

```text
raceNumber <= 0 -> HTTP 400.
lapCount <= 0 -> HTTP 400.
distanceM < 1.0 -> HTTP 400.
maxHorses <= 0 -> HTTP 400.
maxReferees <= 0 -> HTTP 400.
scheduledAt không thuộc raceDate của schedule -> HTTP 400 nếu service đang enforce.
predictionClosesAt sau scheduledAt -> HTTP 400 nếu service đang enforce.
Trùng raceNumber trong cùng schedule/tournament -> HTTP 400.
```

### 4.3. Lấy race detail

Endpoint:

```http
GET /api/v1/admin/races/get-race/{raceId}
```

Kết quả mong đợi:

```text
HTTP 200
Response có raceId, scheduleId, tournamentId, scheduledAt, predictionClosesAt, status.
```

### 4.4. Update race thông thường

Endpoint:

```http
PUT /api/v1/admin/races/update-race/{raceId}
```

Body ví dụ:

```json
{
  "name": "Opening Race Updated",
  "raceNumber": 1,
  "rankGroup": "A",
  "lapCount": 3,
  "scheduledAt": "2026-07-01T09:00:00Z",
  "predictionClosesAt": "2026-07-01T08:50:00Z",
  "distanceM": 1200,
  "trackType": "grass",
  "maxHorses": 8,
  "maxReferees": 3,
  "pointRuleNote": "Updated rule",
  "status": "scheduled"
}
```

Kết quả mong đợi:

```text
HTTP 200
Response có field đã update.
```

### 4.5. Cancel race

Endpoint:

```http
PATCH /api/v1/admin/races/cancel-race/{raceId}
```

Kết quả mong đợi:

```json
{
  "message": "Race cancelled successfully"
}
```

Ghi chú nghiệp vụ:

```text
Cancel race dùng PATCH vì đây là đổi trạng thái nghiệp vụ sang cancelled, không phải xóa vật lý race khỏi database.
```

## 5. Horse và Race Registration

### 5.1. Tạo horse 1

Role:

```text
HORSE_OWNER
```

Endpoint:

```http
POST /api/horses/create
```

Body:

```json
{
  "name": "Thunder Flash",
  "breed": "Arabian",
  "age": 4,
  "weightKg": 450.5,
  "rankGroup": "A",
  "avatarUrl": "https://example.com/horses/thunder-flash.png"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu horseId1.
ownerId được lấy từ token, không gửi trong body.
```

### 5.2. Tạo horse 2 để test rate động

Endpoint:

```http
POST /api/horses/create
```

Body:

```json
{
  "name": "Quiet Arrow",
  "breed": "Thoroughbred",
  "age": 5,
  "weightKg": 470.0,
  "rankGroup": "A",
  "avatarUrl": "https://example.com/horses/quiet-arrow.png"
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu horseId2.
```

### 5.3. Đăng ký horse 1 vào race

Endpoint:

```http
POST /api/race-registrations/create
```

Body:

```json
{
  "tournamentId": 1,
  "raceId": 1,
  "horseId": 1,
  "jockeyId": null
}
```

Thay `1` bằng `tournamentId`, `raceId`, `horseId1`.

Kết quả mong đợi:

```text
HTTP 201
Lưu registrationId1.
ownerId được lấy từ token, không gửi trong body.
```

### 5.4. Đăng ký horse 2 vào race

Endpoint:

```http
POST /api/race-registrations/create
```

Body:

```json
{
  "tournamentId": 1,
  "raceId": 1,
  "horseId": 2,
  "jockeyId": null
}
```

Thay `1`, `2` bằng ID thật.

Kết quả mong đợi:

```text
HTTP 201
Lưu registrationId2.
```

Validation cần test:

```text
Đăng ký cùng horse vào cùng tournament lần 2 -> HTTP 400.
raceId không thuộc tournamentId -> HTTP 400.
horse không thuộc owner hiện tại -> HTTP 403 hoặc 400 theo service hiện tại.
Race đã đủ maxHorses -> HTTP 400.
```

### 5.5. Admin approve registration nếu flow cần

Endpoint:

```http
PUT /api/race-registrations/approve/{registrationId}
```

Body:

```json
{
  "status": "approved"
}
```

Kết quả mong đợi:

```text
HTTP 200
Registration chuyển sang approved.
approvedById lấy từ ADMIN token, không gửi trong body.
```

Ghi chú:

```text
Nếu service hiện tại cho mời jockey ngay sau khi tạo registration thì bước approve có thể dùng như test bổ sung.
```

## 6. Jockey Assignment

### 6.1. Owner mời jockey cho horse 1

Role:

```text
HORSE_OWNER
```

Endpoint:

```http
POST /api/jockey-assignments/create-invitation
```

Body:

```json
{
  "registrationId": 1,
  "raceId": 1,
  "jockeyId": 5,
  "gateNumber": 1
}
```

Thay bằng `registrationId1`, `raceId`, và `jockeyId` thật.

Kết quả mong đợi:

```text
HTTP 201
Lưu assignmentId1.
Status ban đầu thường là pending/invited theo service.
```

### 6.2. Jockey accept invitation cho horse 1

Role:

```text
JOCKEY
```

Endpoint:

```http
PUT /api/jockey-assignments/respond/{assignmentId}
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
Assignment có status accepted.
```

### 6.3. Owner mời jockey cho horse 2

Endpoint:

```http
POST /api/jockey-assignments/create-invitation
```

Body:

```json
{
  "registrationId": 2,
  "raceId": 1,
  "jockeyId": 6,
  "gateNumber": 2
}
```

Kết quả mong đợi:

```text
HTTP 201
Lưu assignmentId2.
```

### 6.4. Jockey accept invitation cho horse 2

Endpoint:

```http
PUT /api/jockey-assignments/respond/{assignmentId}
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
Assignment có status accepted.
```

Validation cần test:

```text
Trùng jockey trong cùng race -> HTTP 400.
Trùng gateNumber trong cùng race -> HTTP 400.
registrationId không thuộc owner hiện tại -> HTTP 403 hoặc 400 theo service hiện tại.
raceId không khớp race của registration -> HTTP 400.
Jockey không phải user role JOCKEY -> HTTP 400.
```

## 7. Mở betting và auto-generate BetOptions

### 7.1. Mở betting cho race

Role:

```text
ADMIN
```

Endpoint:

```http
PUT /api/v1/admin/races/update-race/{raceId}
```

Body:

```json
{
  "status": "open_for_betting"
}
```

Kết quả mong đợi:

```text
HTTP 200
Race status = open_for_betting.
Backend tự generate BetOptions cho các assignment có status accepted hoặc confirmed.
```

Nếu lỗi:

```text
No accepted or confirmed assignments found for this race
```

Kiểm tra lại:

```text
Đã có race registration chưa.
Đã tạo jockey assignment chưa.
Jockey đã accept chưa.
raceId của registration và assignment có khớp không.
```

### 7.2. Lấy BetOptions theo race

Endpoint:

```http
GET /api/bet-options/get-by-race/{raceId}
```

Kết quả mong đợi:

```text
HTTP 200
Response có danh sách BetOptions.
Mỗi horse accepted/confirmed trong race có đúng 1 BetOption.
Lưu optionId1 và optionId2.
```

Các field nên kiểm tra:

```text
id/optionId
raceId
raceName
raceNumber
horseId
horseName
jockeyId
jockeyFullName
assignmentId
gateNumber
betRate
currentRate
totalBetPoints
totalBetCount
status
```

### 7.3. Lấy tất cả BetOptions

Endpoint:

```http
GET /api/bet-options/get-all
```

Kết quả mong đợi:

```text
HTTP 200
Response là danh sách tất cả BetOptions.
```

### 7.4. Lấy tất cả BetOptions có filter raceId

Endpoint:

```http
GET /api/bet-options/get-all?raceId={raceId}
```

Kết quả mong đợi:

```text
HTTP 200
Response giống danh sách theo race.
```

### 7.5. Lấy một BetOption

Endpoint:

```http
GET /api/bet-options/get-by-id/{optionId}
```

Kết quả mong đợi:

```text
HTTP 200
Response đúng optionId.
```

### 7.6. Manual generate BetOptions

Role:

```text
ADMIN
```

Endpoint:

```http
POST /api/admin/bet-options/generate-by-race/{raceId}
```

Kết quả mong đợi:

```text
HTTP 201
Nếu BetOptions đã tồn tại thì không tạo trùng.
Gọi lại lần 2 vẫn không tăng số lượng option.
```

Nghiệp vụ cần xác nhận khi test:

```text
BetOption được sinh theo accepted/confirmed jockey assignment.
Unique theo raceId + horseId.
Một horse trong một race không được có nhiều BetOption.
```

### 7.7. Admin update currentRate thủ công

Role:

```text
ADMIN
```

Endpoint:

```http
PUT /api/admin/bet-options/{optionId}/rate
```

Body:

```json
{
  "currentRate": 2.5
}
```

Kết quả mong đợi:

```text
HTTP 200
currentRate = 2.5.
```

Validation cần test:

```text
currentRate null -> HTTP 400.
currentRate <= 0 -> HTTP 400.
User không phải ADMIN gọi API này -> HTTP 403.
```

## 8. Bet và WalletTransaction

### 8.1. Điều kiện ví trước khi đặt cược

User đặt cược phải có wallet active và đủ points.

Nếu chưa có tiền trong ví, dùng flow nạp tiền hiện có hoặc seed DB trước khi test. Payment endpoint hiện có:

```http
POST /api/payments/create-payment
```

Body:

```json
{
  "amount": 100000,
  "bankCode": "VNBANK",
  "locale": "vn"
}
```

Kết quả mong đợi:

```text
HTTP 200
Response có paymentUrl và transaction.
Sau khi VNPay return/IPN thành công, WalletTransaction và Wallet được cập nhật theo flow Payment.
```

Nếu ví không đủ điểm khi đặt cược, API bet sẽ trả lỗi dạng:

```text
Wallet balance is not enough to place this bet
```

### 8.2. Tạo bet cho option 1

Role:

```text
SPECTATOR hoặc user có ví đủ điểm
```

Endpoint:

```http
POST /api/bets/create
```

Body:

```json
{
  "optionId": 1,
  "betType": true,
  "betPoints": 100
}
```

Thay `optionId` bằng `optionId1`.

Không gửi:

```text
userId
betRate
```

Vì:

```text
userId lấy từ token.
betRate được snapshot từ BetOption.currentRate tại thời điểm đặt cược.
```

Kết quả mong đợi:

```text
HTTP 201
Response có betId.
betRate là rate snapshot lúc đặt cược.
currentRate là rate mới sau khi hệ thống recalculation.
totalBetPoints tăng thêm 100.
totalBetCount tăng thêm 1.
Wallet bị trừ 100 points.
WalletTransaction được tạo với txType = bet, status = completed, refType = bet, refId = betId.
```

Ví dụ response hợp lệ:

```json
{
  "assignmentId": 1,
  "betId": 1,
  "betPoints": 100,
  "betRate": 2.5,
  "betType": true,
  "currentRate": 1.1,
  "horseId": 1,
  "horseName": "Thunder Flash",
  "id": 1,
  "jockeyFullName": "Pham Duc Anh",
  "jockeyId": 5,
  "optionId": 1,
  "placedAt": "2026-06-19T16:18:42.171774600Z",
  "predictionClosesAt": "2026-07-01T08:50:00Z",
  "raceId": 1,
  "raceName": "Opening Race",
  "raceNumber": 1,
  "rewardPoints": 0,
  "scheduledAt": "2026-07-01T09:00:00Z",
  "settledAt": null,
  "status": "pending",
  "totalBetCount": 1,
  "totalBetPoints": 100,
  "userFullName": "Nguyen Hoang Son",
  "userId": 13
}
```

Giải thích case `betRate = 2.5`, `currentRate = 1.1`:

```text
betRate là snapshot cũ lúc user bấm đặt cược.
currentRate là rate mới sau khi option đó nhận thêm tiền cược.
Nếu option đó đang chiếm phần lớn hoặc toàn bộ tiền cược của race, rate bị giảm xuống. Min rate hiện tại là 1.10.
```

### 8.3. Lấy bet theo id

Endpoint:

```http
GET /api/bets/get-by-id/{betId}
```

Kết quả mong đợi:

```text
HTTP 200
Response đúng betId, optionId, raceId, horseId, betPoints, betRate snapshot.
```

### 8.4. Lấy danh sách bets

Endpoint:

```http
GET /api/bets/get-all
```

Kết quả mong đợi:

```text
HTTP 200
Response là danh sách bets.
```

## 9. Test dynamic BetOption rate

Mục tiêu nghiệp vụ:

```text
Ngựa mạnh, win rate/ranking tốt và được nhiều người đặt cược thì currentRate giảm.
Ngựa ít được đặt cược hoặc win rate/ranking thấp hơn thì currentRate tăng để hấp dẫn người chơi.
```

### 9.1. Chuẩn bị ít nhất 2 option trong cùng race

Đã làm ở các bước trên:

```text
horseId1 + assignment accepted -> optionId1.
horseId2 + assignment accepted -> optionId2.
race status = open_for_betting.
```

Nếu race đã mở betting trước khi tạo option 2:

```http
POST /api/admin/bet-options/generate-by-race/{raceId}
```

Sau đó kiểm tra:

```http
GET /api/bet-options/get-by-race/{raceId}
```

### 9.2. Set rate ban đầu để dễ nhìn

Endpoint:

```http
PUT /api/admin/bet-options/{optionId1}/rate
```

Body:

```json
{
  "currentRate": 2.5
}
```

Endpoint:

```http
PUT /api/admin/bet-options/{optionId2}/rate
```

Body:

```json
{
  "currentRate": 3.5
}
```

### 9.3. Đặt nhiều bet vào option 1

Gọi nhiều lần:

```http
POST /api/bets/create
```

Body:

```json
{
  "optionId": 1,
  "betType": true,
  "betPoints": 100
}
```

Kết quả mong đợi:

```text
optionId1.totalBetPoints tăng.
optionId1.totalBetCount tăng.
optionId1.currentRate giảm dần, thấp nhất là 1.10.
```

### 9.4. Kiểm tra option 2 sau khi option 1 nhận nhiều tiền

Endpoint:

```http
GET /api/bet-options/get-by-race/{raceId}
```

Kết quả mong đợi:

```text
optionId2 nếu ít hoặc chưa có ai đặt thì currentRate cao hơn option đang hot.
Đây là logic cân bằng thị trường để kéo người chơi sang option ít được đặt.
```

### 9.5. Đặt bet vào option 2

Endpoint:

```http
POST /api/bets/create
```

Body:

```json
{
  "optionId": 2,
  "betType": true,
  "betPoints": 100
}
```

Kết quả mong đợi:

```text
optionId2.totalBetPoints tăng.
optionId2.currentRate được tính lại theo ranking/win history và tỷ trọng tiền cược mới.
```

## 10. Các case lỗi bắt buộc cho BetOption/Bet

### 10.1. Generate BetOptions khi chưa có assignment accepted

Endpoint:

```http
POST /api/admin/bet-options/generate-by-race/{raceId}
```

Điều kiện:

```text
Race chưa có assignment status accepted hoặc confirmed.
```

Kết quả mong đợi:

```text
HTTP 400
Message: No accepted or confirmed assignments found for this race
```

### 10.2. Generate BetOptions nhiều lần

Endpoint:

```http
POST /api/admin/bet-options/generate-by-race/{raceId}
```

Gọi 2-3 lần liên tiếp.

Kết quả mong đợi:

```text
Không tạo duplicate.
Số lượng BetOptions theo race không tăng sai.
Unique nghiệp vụ là raceId + horseId.
```

### 10.3. Đặt cược khi race chưa open_for_betting

Điều kiện:

```text
Race status khác open_for_betting.
```

Endpoint:

```http
POST /api/bets/create
```

Body:

```json
{
  "optionId": 1,
  "betType": true,
  "betPoints": 100
}
```

Kết quả mong đợi:

```text
HTTP 400
Message: Race is not open for betting
```

### 10.4. Đặt cược sau predictionClosesAt

Điều kiện:

```text
predictionClosesAt <= thời điểm hiện tại.
```

Kết quả mong đợi:

```text
HTTP 400
Message: Prediction is already closed
```

### 10.5. Đặt cược với betPoints không hợp lệ

Body:

```json
{
  "optionId": 1,
  "betType": true,
  "betPoints": 0
}
```

Kết quả mong đợi:

```text
HTTP 400
Field betPoints: Bet points must be greater than 0
```

### 10.6. Đặt cược khi ví không đủ điểm

Body:

```json
{
  "optionId": 1,
  "betType": true,
  "betPoints": 999999999
}
```

Kết quả mong đợi:

```text
HTTP 400
Message: Wallet balance is not enough to place this bet
```

### 10.7. Đặt cược option không tồn tại

Body:

```json
{
  "optionId": 999999,
  "betType": true,
  "betPoints": 100
}
```

Kết quả mong đợi:

```text
HTTP 404 hoặc 400 theo handler hiện tại.
Message báo không tìm thấy BetOption.
```

## 11. Public APIs cho landing page

Các API này dùng để landing page hiển thị số lượng và không yêu cầu đăng nhập nếu SecurityConfig đang permit đúng.

### 11.1. Public horse count

Endpoint:

```http
GET /api/horses/get-horse-count
```

Kết quả mong đợi:

```text
HTTP 200
Response có horseCount.
Không cần Authorization header.
```

### 11.2. Public scheduled race count

Endpoint:

```http
GET /api/races/get-scheduled-race-count
```

Kết quả mong đợi:

```text
HTTP 200
Response có scheduledRaceCount.
Không cần Authorization header.
```

### 11.3. Public global tournament count

Endpoint:

```http
GET /api/tournaments/get-global-tournament-count
```

Kết quả mong đợi:

```text
HTTP 200
Response có globalTournamentCount.
Không cần Authorization header.
```

## 12. Checklist end-to-end ngắn

Chạy theo thứ tự này để test full flow nhanh:

```text
1. Login ADMIN, HORSE_OWNER, JOCKEY, SPECTATOR.
2. ADMIN tạo tournament.
3. ADMIN tạo prizes.
4. ADMIN tạo schedule trong tournament.
5. ADMIN tạo race trong schedule.
6. HORSE_OWNER tạo horse 1 và horse 2.
7. HORSE_OWNER đăng ký 2 horse vào race.
8. ADMIN approve registrations nếu flow cần.
9. HORSE_OWNER tạo invitation cho jockey 1, gate 1.
10. JOCKEY 1 accept invitation.
11. HORSE_OWNER tạo invitation cho jockey 2, gate 2.
12. JOCKEY 2 accept invitation.
13. ADMIN update race status = open_for_betting.
14. GET BetOptions theo race, lưu optionId1/optionId2.
15. ADMIN gọi manual generate lại để kiểm tra không duplicate.
16. ADMIN update currentRate nếu muốn set rate ban đầu.
17. SPECTATOR đặt bet option 1.
18. GET BetOptions theo race để thấy totalBetPoints/totalBetCount/currentRate đổi.
19. SPECTATOR đặt thêm nhiều bet vào option 1 để thấy currentRate giảm.
20. SPECTATOR đặt bet vào option 2 để thấy rate cân bằng lại.
21. Kiểm tra public count APIs không cần token.
```

## 13. Ghi chú nghiệp vụ đã implement

BetOption:

```text
Sinh tự động khi race được mở betting.
Có API admin generate thủ công cho một race.
Chỉ lấy assignment status accepted hoặc confirmed.
Không tạo trùng option cho cùng raceId + horseId.
Rate ban đầu dựa trên sức ngựa/ranking/win history.
Rate động thay đổi theo tỷ trọng tiền cược của option trong race.
Ngựa được đặt nhiều sẽ giảm currentRate.
Ngựa ít được đặt sẽ có currentRate hấp dẫn hơn.
Admin có thể update currentRate thủ công.
```

Bet:

```text
User chỉ gửi optionId, betType, betPoints.
Backend tự lấy userId từ token.
Backend tự snapshot betRate từ currentRate tại thời điểm đặt cược.
Chỉ cho đặt khi race status = open_for_betting.
Không cho đặt sau predictionClosesAt.
Ví bị trừ điểm ngay khi đặt cược thành công.
Tạo WalletTransaction loại bet để ghi nhận giao dịch.
Sau mỗi bet, BetOption cập nhật totalBetPoints, totalBetCount và recalculation currentRate.
```

Race:

```text
Race thuộc TournamentSchedule.
TournamentSchedule thuộc Tournament.
RaceSchedulesController là nhóm API admin để tạo lịch thi đấu và quản lý race trong lịch.
RaceController hiện có API public/count và API list race theo tournament cho owner chọn khi đăng ký.
Cancel race dùng PATCH vì đổi trạng thái nghiệp vụ, không xóa vật lý.
```

Prize:

```text
Prize thuộc Tournament.
Mỗi finishPosition trong cùng tournament không được trùng.
Prize amount phải lớn hơn 0.
Tổng prizeAmount không được vượt prizePool của tournament.
Có API create list, get list, get detail, update, delete.
```
