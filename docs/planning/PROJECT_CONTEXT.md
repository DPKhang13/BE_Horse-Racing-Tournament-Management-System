# Project Context: Horse Racing Tournament Management System (HTMS)

## 1. Thông tin chung

- **Tên project:** htms
- **Mô tả:** Hệ thống quản lý giải đấu đua ngựa (Quản lý người dùng, ví tiền, giải đấu, ngựa, nài ngựa, đặt cược, kết quả, trọng tài).

## 2. Tech Stack & Dependencies

- **Ngôn ngữ:** Java 25
- **Framework chính:** Spring Boot 4.0.6
- **Database:** Hỗ trợ PostgreSQL (driver cài sẵn), MSSQL. Sử dụng `spring-boot-starter-data-jpa`. Cấu hình ddl-auto update. Kèm database H2 cho Test.
- **Bảo mật:** `spring-boot-starter-security`, `oauth2-client`.
- **API Documentation:** OpenAPI (Swagger 3) thông qua `springdoc-openapi-starter-webmvc-ui`.
- **Tiện ích:** Lombok, Spring Boot DevTools, Validation, Mail.

## 3. Cấu trúc Project (`src/main/java/com/group5/htms`)

Dự án được chia theo kiến trúc Layered chuẩn của Spring Boot:

- **`config/`**: Đã chứa `SwaggerConfig.java` để khai báo OpenAPI (Swagger UI có thể truy cập `/swagger-ui/index.html` và docs tại `/v3/api-docs`).
- **`entity/`**: Đã hoàn thiện toàn bộ sơ đồ CSDL (Dựa vào ERD), bao gồm 17 entities:
  - _Quản lý User & Vai trò:_ `Users`, `Roles`
  - _Quản lý Tài chính:_ `Wallets`, `WalletTransactions`
  - _Quản lý Thành phần đua:_ `Horses`, `JockeyProfiles`, `JockeyHorseAssignments`
  - _Quản lý Giải đấu & Lịch trình:_ `Tournaments`, `TournamentSchedules`, `Races`, `RaceRegistrations`
  - _Kết quả & Cược:_ `RaceResults`, `Bets`, `PrizeDistributions`
  - _Trọng tài:_ `RaceRefereeAssignments`, `RefereeReports`
  - _Thông báo:_ `Notifications`
- **`repository/`**: Đã tạo đầy đủ 17 interface Repositories (như `UsersRepository`, `RacesRepository`, `BetsRepository`,...) hỗ trợ giao tiếp với DB cho từng Entity.
- **`controller/`**, **`service/`**, **`dto/`**, **`mapper/`**, **`exception/`**: Các thư mục đã được khởi tạo nhưng hiện tại **đang trống** (chưa có file logic nào).

## 4. Trạng thái hiện tại

- **Phân hệ Data Access (Entity & Repository):** Đã hoàn thiện.
- **Phân hệ API Config:** Swagger đã chạy.
- **Chưa làm:**
  - Chưa triển khai các Service (Business Logic) như xử lý đặt cược, phân bổ giải thưởng, xếp lịch đấu.
  - Chưa tạo DTOs và MapStruct/ModelMapper tương ứng cho dữ liệu ra vào.
  - Chưa có Controllers (REST APIs).
  - Chưa có Global Exception Handler (phần xử lý biệt lệ dùng chung).
  - Config Security và JWT/OAuth2 đang ở mức dependecy, chưa có code cài đặt chi tiết (`SecurityConfig`).

## 5. Bước tiếp theo đề xuất

1. Xác định quy chuẩn tạo DTOs (Request / Response) & thư viện mapping (nếu dùng MapStruct).
2. Viết các logic xử lý nghiệp vụ (CRUD cơ bản) trong tầng `service`.
3. Tạo các `controller` tương ứng phơi bày API.
4. Customize `SecurityConfig` và luồng Xác thực/Phân quyền (Auth).
5. Áp dụng `@ControllerAdvice` để handle các ngoại lệ (Exception) trả về format Error chuẩn nhất.
