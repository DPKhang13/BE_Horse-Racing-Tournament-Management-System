# Project Context: Horse Racing Tournament Management System (HTMS)

## 1. General Information

- Project name: htms
- Package root: com.group5.htms
- Description: Web-based system for managing horse racing tournaments, including users, roles, horses, jockeys, referees, race registrations, schedules, betting, wallet transactions, referee reports, race results, prize distributions, and notifications.

Main actors:

- Admin
- Horse Owner
- Jockey
- Race Referee
- Spectator

---

## 2. Tech Stack

- Java 25
- Spring Boot 4.0.6
- Spring Data JPA
- Spring Security
- JWT Authentication
- PostgreSQL as main database
- MSSQL supported if needed
- H2 for testing
- Swagger/OpenAPI
- Lombok
- Validation
- Java Mail Sender

---

## 3. Source Code Structure

Root package:

src/main/java/com/group5/htms

Expected package structure:

config/

- SecurityConfig.java
- JwtConfig.java
- SwaggerConfig.java
- MailConfig.java

filter/

- JwtAuthenticationFilter.java

controller/

- REST controllers

service/

- Service interfaces

service/impl/

- Service implementations

repository/

- Spring Data JPA repositories

entity/

- JPA entities

dto/

- Request and Response DTOs

mapper/

- DTO/entity mapping classes

exception/

- GlobalExceptionHandler
- ApiException
- ErrorResponse

Rules:

- Do not put filter classes in config.
- Do not put service implementation directly in service root.
- Controllers must not contain business logic.
- Services handle business rules.
- Repositories only handle database access.
- DTOs must be used for request/response.
- Entity names and repository names must follow existing source code.

---

## 4. Architecture

The project uses Layered Architecture:

Client
→ JWT Filter Layer
→ Controller Layer
→ DTO / Mapper Layer
→ Service Layer
→ Repository Layer
→ Database Layer

Cross-cutting concerns:

- JWT Security
- Validation
- Exception Handling
- Mail Sender
- Swagger/OpenAPI

---

## 5. Database Tables Context

The current database contains 17 main entities.

---

# 5.1 Users

Purpose:

- Stores account identity and login information.

Main fields:

- user_id
- username
- email
- password_hash
- full_name
- phone
- status
- created_at

Business meaning:

- Users is the authentication identity.
- Password must be stored as BCrypt hash.
- Do not store role directly in Users.
- Role information is stored in Roles table.

Related tables:

- Users 1 - N Roles
- Users 1 - N Notifications
- Users 1 - N Tournaments through created_by
- Users 1 - N RaceRegistrations through approved_by
- Users 1 - N WalletTransactions through created_by
- Users 1 - N Bets through settled_by

Important logic:

- created_by, approved_by, settled_by should point to Users because they represent account-level actions.
- Authentication should load user by username or email.

---

# 5.2 Roles

Purpose:

- Stores business role of a user.

Main fields:

- role_id
- user_id
- role_type
- license_number
- address
- status
- created_at

Allowed role_type:

- admin
- horse_owner
- jockey
- race_referee
- spectator

Business meaning:

- A User can have multiple Roles.
- Role represents the actor behavior in business logic.
- For example, the same User can be spectator and horse_owner.
- license_number and address are common role profile data.
- Jockey-specific ranking data is not stored here. It is stored in JockeyProfiles.

Related tables:

- Roles N - 1 Users
- Roles 1 - 1 JockeyProfiles when role_type = jockey
- Roles 1 - N Horses when role_type = horse_owner
- Roles 1 - N RaceRegistrations as owner_role_id or jockey_role_id
- Roles 1 - N JockeyHorseAssignments as jockey_role_id
- Roles 1 - N RaceRefereeAssignments as referee_role_id
- Roles 1 - 1 Wallets when role_type = spectator
- Roles 1 - N Bets as spectator_role_id

Important service validation:

- owner_role_id must have role_type = horse_owner
- jockey_role_id must have role_type = jockey
- referee_role_id must have role_type = race_referee
- spectator_role_id must have role_type = spectator
- admin actions must be validated by checking whether the current User has a Roles record with role_type = admin

---

# 5.3 JockeyProfiles

Purpose:

- Stores jockey-specific data.

Main fields:

- jockey_profile_id
- role_id
- ranking_points
- total_wins
- experience_years
- status

Business meaning:

- JockeyProfiles only exists for Roles where role_type = jockey.
- license_number is stored in Roles, not in JockeyProfiles.
- Used for jockey ranking and profile display.

Related tables:

- JockeyProfiles 1 - 1 Roles

Important logic:

- When registering a new jockey role, create a JockeyProfiles record.
- Do not link RaceRegistrations or JockeyHorseAssignments directly to JockeyProfiles.
- Use Roles.role_id as jockey_role_id, then join to JockeyProfiles if profile details are needed.

---

# 5.4 Horses

Purpose:

- Stores horse information.

Main fields:

- horse_id
- owner_role_id
- name
- breed
- age
- weight_kg
- rank_group
- ranking_points
- total_wins
- status
- registered_at

Business meaning:

- A horse belongs to a Horse Owner role.
- owner_role_id points to Roles.role_id where role_type = horse_owner.
- Horse ranking can be updated after RaceResults are published.

Related tables:

- Horses N - 1 Roles
- Horses 1 - N RaceRegistrations

Important logic:

- Only a horse_owner role can create/manage horses.
- Service must validate current owner owns the horse before update/delete/registering it to race.

---

# 5.5 Tournaments

Purpose:

- Stores tournament information.

Main fields:

- tournament_id
- name
- location
- start_date
- end_date
- prize_pool
- status
- created_by
- created_at

Business meaning:

- Tournament is created by Admin.
- prize_pool is total prize pool for the whole tournament.
- PrizeDistribution breaks the prize pool into race-level or position-level prize records.

Related tables:

- Tournaments 1 - N TournamentSchedules
- Tournaments 1 - N RaceRegistrations
- Tournaments 1 - N PrizeDistributions
- Tournaments N - 1 Users through created_by

Important logic:

- created_by points to Users.user_id, not Roles.role_id.
- Only Admin can create/update tournaments.
- Tournament status can be draft/upcoming/registration_open/in_progress/completed/cancelled depending on implementation.

---

# 5.6 TournamentSchedules

Purpose:

- Stores tournament schedule days.

Main fields:

- schedule_id
- tournament_id
- race_date
- day_number
- title
- note

Business meaning:

- A Tournament can have many schedule days.
- Races are created under a TournamentSchedule.

Related tables:

- TournamentSchedules N - 1 Tournaments
- TournamentSchedules 1 - N Races

Important logic:

- race_date represents date of the schedule day.
- Races.scheduled_at stores exact date and time of each race.

---

# 5.7 Races

Purpose:

- Stores race information.

Main fields:

- race_id
- schedule_id
- name
- race_number
- rank_group
- scheduled_at
- prediction_closes_at
- distance_m
- track_type
- max_horses
- status

Business meaning:

- Race belongs to a TournamentSchedule.
- scheduled_at is exact date-time the race starts.
- prediction_closes_at controls betting deadline.
- track_type describes surface type such as dirt, turf, synthetic, sand.
- rank_group groups eligible horses.

Related tables:

- Races N - 1 TournamentSchedules
- Races 1 - N RaceRegistrations
- Races 1 - N JockeyHorseAssignments
- Races 1 - N RaceRefereeAssignments
- Races 1 - N PrizeDistributions

Important logic:

- Betting is allowed only if current time < prediction_closes_at.
- Race status should control whether registration/betting/result publication is allowed.

---

# 5.8 RaceRegistrations

Purpose:

- Stores horse registration into a specific race and tournament.

Main fields:

- reg_id
- tournament_id
- race_id
- horse_id
- owner_role_id
- jockey_role_id
- status
- owner_confirmation_status
- owner_confirmed_at
- registered_at
- approved_at
- approved_by

Business meaning:

- Horse Owner registers a Horse into a Race.
- tournament_id is kept to enforce one horse registration per tournament.
- race_id shows which race the horse is assigned to.
- Backend must validate race_id belongs to tournament_id.
- owner_role_id is the horse owner role.
- jockey_role_id is optional at registration time.

Related tables:

- RaceRegistrations N - 1 Tournaments
- RaceRegistrations N - 1 Races
- RaceRegistrations N - 1 Horses
- RaceRegistrations N - 1 Roles as owner_role_id
- RaceRegistrations N - 1 Roles as jockey_role_id
- RaceRegistrations N - 1 Users as approved_by
- RaceRegistrations 1 - N JockeyHorseAssignments

Important logic:

- Unique business rule: one horse can register only once per tournament.
- Admin approves/rejects registration.
- approved_by points to Users because approval is an account-level admin action.
- Service must validate horse belongs to owner_role_id.
- Service must validate race_id belongs to tournament_id.
- If owner_confirmation_status is kept, service must keep it consistent with status.

---

# 5.9 JockeyHorseAssignments

Purpose:

- Stores jockey assignment/invitation for a registered horse in a race.

Main fields:

- assignment_id
- reg_id
- race_id
- jockey_role_id
- gate_number
- status
- invited_at
- responded_at

Business meaning:

- Represents Horse Owner inviting/selecting a Jockey for a race registration.
- reg_id links to RaceRegistrations.
- race_id is intentionally kept to enforce unique jockey and gate per race.
- horse_id and owner_role_id are not stored here because they are inferred from RaceRegistrations.

Related tables:

- JockeyHorseAssignments N - 1 RaceRegistrations
- JockeyHorseAssignments N - 1 Races
- JockeyHorseAssignments N - 1 Roles as jockey_role_id
- JockeyHorseAssignments 1 - N Bets
- JockeyHorseAssignments 1 - N RefereeReports

Important logic:

- Do not add unique(race_id, reg_id), because re-invite must be allowed after rejected/cancelled.
- Backend must block new assignment only if an existing assignment for same race_id + reg_id has status pending or accepted.
- Unique race_id + jockey_role_id prevents one jockey from racing multiple horses in same race.
- Unique race_id + gate_number prevents duplicate gate number in same race.
- jockey_role_id must have role_type = jockey.

---

# 5.10 RaceRefereeAssignments

Purpose:

- Stores referee assignment to a race.

Main fields:

- ref_assign_id
- race_id
- referee_role_id
- assigned_at

Business meaning:

- Admin assigns Race Referee to a Race.
- One race can have one or multiple referees depending on business scope.

Related tables:

- RaceRefereeAssignments N - 1 Races
- RaceRefereeAssignments N - 1 Roles as referee_role_id
- RaceRefereeAssignments 1 - N RefereeReports

Important logic:

- referee_role_id must have role_type = race_referee.
- Admin is responsible for assigning referees.

---

# 5.11 RefereeReports

Purpose:

- Stores detailed referee report and race performance details for a specific assignment.

Main fields:

- report_id
- ref_assign_id
- assignment_id
- finish_position
- finish_time_sec
- points_awarded
- is_disqualified
- disqualify_reason
- point_rule
- inspection_notes
- summary
- violations
- verdict
- status
- submitted_at

Business meaning:

- RefereeReport is the detailed result record.
- It contains finish position, finish time, points, violations, disqualification information, and referee verdict.
- RaceResults are generated from confirmed/published RefereeReports.

Related tables:

- RefereeReports N - 1 RaceRefereeAssignments
- RefereeReports N - 1 JockeyHorseAssignments
- RefereeReports 1 - N RaceResults or 1 - 1 RaceResults depending on final model

Important logic:

- Referee creates report after race finishes.
- Violation does not automatically cancel race result.
- If disqualified, is_disqualified = true and disqualify_reason must be provided.
- Status flow can be draft -> pending -> confirmed -> published -> cancelled.
- Only confirmed/published reports should be used to generate official RaceResults.

---

# 5.12 RaceResults

Purpose:

- Stores official published race result used for ranking, prize linking, and bet settlement.

Main fields:

- result_id
- report_id
- prize_id
- rank_position
- prize_amount
- published_at

Business meaning:

- RaceResults is the official result table.
- It should not duplicate full referee report data.
- It shows top result positions and links to PrizeDistributions.
- RaceResult is generated after RefereeReports are confirmed.

Related tables:

- RaceResults N - 1 RefereeReports
- RaceResults N - 1 PrizeDistributions

Important logic:

- rank_position should represent official rank such as 1 to 8.
- prize_id links result to prize structure.
- published_at marks when result becomes visible to users.
- When RaceResult is published:
  - Settle Bets
  - Update Horse/Jockey rankings
  - Send Notifications
- If the model supports many race results per race, service must ensure one rank_position per race.

---

# 5.13 PrizeDistributions

Purpose:

- Stores announced prize structure.

Main fields:

- prize_id
- tournament_id
- finish_position
- amount

Business meaning:

- PrizeDistribution is created before the race result.
- It defines how much prize is given for each finish position.
- RaceResult references PrizeDistribution later using prize_id.
- prize_pool in Tournaments is total prize amount, while PrizeDistribution.amount is detailed prize amount by position.

Related tables:

- PrizeDistributions N - 1 Tournaments
- PrizeDistributions 1 - N RaceResults

Important logic:

- PrizeDistribution does not need result_id because prizes are announced before results exist.
- RaceResults.prize_id points to PrizeDistributions.
- If different races have different prize structures, PrizeDistributions should include race_id.
- If all races share the same prize structure within a tournament, tournament_id + finish_position is enough.

---

# 5.14 Wallets

Purpose:

- Stores spectator betting balance.

Main fields:

- wallet_id
- spectator_role_id
- point_balance
- status
- created_at

Business meaning:

- Wallet belongs to spectator role, not Users.
- Wallet is only used for spectator betting/prediction points.
- PrizeDistribution for horse owners is not connected to wallet because owner prize can be direct reward or physical reward.

Related tables:

- Wallets 1 - 1 Roles as spectator_role_id
- Wallets 1 - N WalletTransactions

Important logic:

- spectator_role_id must have role_type = spectator.
- Service layer must resolve user -> spectator role before top-up, bet, refund, or reward settlement.

---

# 5.15 WalletTransactions

Purpose:

- Stores wallet transaction audit history.

Main fields:

- tx_id
- wallet_id
- spectator_role_id
- tx_type
- cash_amount
- points_amount
- exchange_rate
- points_before
- points_after
- status
- ref_type
- ref_id
- created_by
- created_at

Business meaning:

- Records all wallet operations.
- Examples: top_up, bet_deduct, bet_reward, refund.

Related tables:

- WalletTransactions N - 1 Wallets
- WalletTransactions N - 1 Roles as spectator_role_id
- WalletTransactions N - 1 Users as created_by

Important logic:

- created_by points to Users for admin/system audit.
- Service must ensure points_before and points_after are correct.
- WalletTransaction should not be deleted because it is financial audit data.

---

# 5.16 Bets

Purpose:

- Stores spectator betting/prediction records.

Main fields:

- bet_id
- spectator_role_id
- assignment_id
- bet_type
- predicted_position
- bet_points
- odds_rate
- estimated_win_points
- rewarded_points
- status
- placed_at
- settled_at
- settled_by
- settled_type

Business meaning:

- A Spectator places a Bet on a JockeyHorseAssignment.
- Bet belongs to spectator role.
- assignment_id represents the horse + jockey entry being predicted.
- bet_type determines the rule of winning.

bet_type examples:

- winner: selected assignment must finish rank 1
- top3: selected assignment must finish within top 3
- top5: selected assignment must finish within top 5

Renamed fields:

- stake_points -> bet_points
- odds_decimal -> odds_rate
- potential_payout_points -> estimated_win_points
- payout_points -> rewarded_points
- market_type -> bet_type

Related tables:

- Bets N - 1 Roles as spectator_role_id
- Bets N - 1 JockeyHorseAssignments
- Bets N - 1 Users as settled_by

Important logic:

- Before placing a bet:
  - Race must be open for betting
  - current time must be before prediction_closes_at
  - spectator wallet must have enough points
- After bet is placed:
  - deduct bet_points from Wallet
  - create WalletTransaction
  - create Bet with status pending
- When RaceResult is published:
  - evaluate bet by bet_type
  - set status to won/lost/refunded/cancelled
  - update rewarded_points
  - update Wallet if won/refunded
  - create WalletTransaction
  - send Notification
- settled_by points to Users only when admin manually settles.
- NULL settled_by can mean system settlement.
- settled_type can be system or admin.

---

# 5.17 Notifications

Purpose:

- Stores notification messages for users.

Main fields:

- notification_id
- user_id
- title
- message
- type
- ref_id
- ref_type
- is_read
- created_at

Business meaning:

- Sends notifications to users for important events.

Examples:

- Registration approved/rejected
- Jockey invitation
- Race result published
- Bet won/lost/refunded
- Prize announced
- Race schedule updated

Related tables:

- Notifications N - 1 Users

Important logic:

- Notification is a side effect of service operations.
- Notification should be created by services, not directly by controllers.

---

## 6. Main Business Flow

# Flow 1: Admin Tournament Flow

1. Admin logs in.
2. Admin creates Tournament.
3. Admin creates TournamentSchedule.
4. Admin creates Races.
5. Admin creates PrizeDistributions.
6. Admin opens registration.
7. Horse Owner registers Horse into Race.
8. Admin checks RaceRegistration.
9. If eligible, Admin approves registration.
10. Horse Owner invites Jockey.
11. Jockey accepts invitation.
12. System creates/updates JockeyHorseAssignment.
13. Admin assigns Race Referee.
14. Admin closes registration.
15. Race goes in progress.
16. Race completes.
17. Referee report flow starts.

---

# Flow 2: Spectator Betting Flow

1. Spectator logs in.
2. Spectator views Tournament and Race.
3. Spectator selects a Race.
4. System checks whether prediction is still open.
5. Spectator selects Horse/Jockey assignment.
6. Spectator chooses bet_type.
7. Spectator enters bet_points.
8. System checks Wallet balance.
9. System deducts Wallet points.
10. System creates Bet with status pending.
11. Spectator waits for RaceResult.
12. System settles Bet after RaceResult is published.
13. System updates Bet status and Wallet.
14. System sends Notification.

---

# Flow 3: Race Referee Result Flow

1. Race Referee logs in.
2. Race Referee views assigned races.
3. Race Referee checks horse information before race.
4. Race Referee monitors race.
5. After race ends, Race Referee creates RefereeReport.
6. Referee records finish_position, finish_time_sec, points_awarded.
7. If violation exists, Referee records violations and disqualification details.
8. Referee submits report.
9. System/Admin confirms report.
10. System generates RaceResults from confirmed RefereeReports.
11. System links RaceResults with PrizeDistributions.
12. System publishes RaceResults.
13. System settles Bets.
14. System updates rankings and points.
15. System sends Notifications.

---

## 7. Current Development Status

Completed:

- ERD design
- Entity classes
- Repository interfaces
- Swagger configuration
- Project package structure
- Main business flow

In progress:

- JWT authentication
- SecurityConfig
- Auth APIs

Not implemented yet:

- Full service layer
- DTO request/response
- Controller layer
- Mapper layer
- Global Exception Handler
- Mail Sender
- Bet settlement engine
- Wallet transaction service
- Notification service

---

## 8. Code Generation Rules for AI

When generating code:

- Read existing source code before writing.
- Do not rename existing entities or repositories unless explicitly requested.
- Follow package structure:
  - controller
  - service
  - service.impl
  - dto
  - mapper
  - repository
  - entity
  - exception
  - config
  - filter
- Do not place filter classes inside config.
- Do not place service implementations inside service root.
- Use DTOs for API request and response.
- Use services for business logic.
- Use repositories only for database operations.
- Validate role_type before executing role-specific actions.
- Use Users for account-level audit fields.
- Use Roles for business actor references.
- Use Wallets only for spectator betting.
- Use PrizeDistributions only for official tournament/race prizes.
- Do not connect PrizeDistributions to Wallets.
- Do not delete financial records such as Bets and WalletTransactions.
- Prefer status changes over hard deletes for important business records.
