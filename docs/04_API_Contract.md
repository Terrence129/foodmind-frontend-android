# FoodMind 前后端 API 契约

版本：1.2  
日期：2026-06-23  
后端：Spring Boot 3 + PostgreSQL  
客户端：Android

## 1. 契约说明

本文件是 Android 与后端之间的通信依据。字段名称、枚举值、空值、状态码或响应结构发生变化时，后端和 Android 必须同步修改本文档。

本文档基于原始 `FoodMind_Frontend_API_Documentation(1).md` 整理，并补充：

- App 启动和 Profile 完成状态。
- 页面成功后的导航行为。
- 401 统一处理。
- 尚未定义清楚的业务规则。
- Android 本地后端连接说明。

## 2. Base URL

服务地址：

```text
http://localhost:8080
```

API Prefix：

```text
/api
```

Android 实际配置：

| 环境 | Base URL |
|---|---|
| Android Emulator | `http://10.0.2.2:8080/` |
| 真机 | `http://<电脑局域网IP>:8080/` |
| Web/电脑本地测试 | `http://localhost:8080/` |

Retrofit 的 Base URL 必须以 `/` 结尾。

## 3. 通用请求规则

### 3.1 JSON

```http
Content-Type: application/json
Accept: application/json
```

MVP 不包含图片上传。

### 3.2 JWT

受保护接口必须包含：

```http
Authorization: Bearer <jwt_token>
```

Login、Register、Health 不需要 Token。

### 3.3 时间

- Datetime：ISO 8601，必须带时区 offset。
- 示例：`2026-06-23T12:30:00+08:00`。
- Date：`YYYY-MM-DD`。
- Month：`YYYY-MM`。
- `consumedAt` 是实际食用时间。
- `createdAt` 是记录创建时间。

### 3.4 分页

请求：

```http
?page=0&size=20
```

`page` 从 0 开始。

响应数据：

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

## 4. 通用响应

### 4.1 成功

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

### 4.2 失败

推荐失败结构：

```json
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "details": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

当前 Spring 后端全局异常处理实际返回：

```json
{
  "timestamp": "2026-06-23T10:30:00+08:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "email": "Email is required"
  }
}
```

Android 错误解析需要同时兼容 `details` 列表和 `errors` map。

### 4.3 删除

建议所有 Delete 统一返回 HTTP 200 和通用响应：

```json
{
  "success": true,
  "message": "Deleted successfully",
  "data": null
}
```

如果后端选择 HTTP 204，则 Response Body 必须为空。两种方式只能选一种。当前 Android 契约推荐统一使用 HTTP 200，减少解析分支。

## 5. 状态码和 Android 行为

| Status | 含义 | Android 行为 |
|---|---|---|
| 200 | Success | 更新 UI |
| 201 | Created | 显示成功并导航 |
| 204 | No content | 仅在后端统一采用时支持 |
| 400 | Validation | 映射 field error 或 Snackbar |
| 401 | Token invalid | 清除 Session，跳转 Login |
| 403 | Forbidden | 显示权限错误 |
| 404 | Not found | 显示不存在状态 |
| 409 | Conflict | 显示后端 message |
| 500 | Server error | 通用错误和 Retry |

任意受保护接口返回 401 时，不由各业务 Activity 重复处理页面跳转，而是走统一 SessionExpired 流程：清除 Token，使用显式 Intent 打开 LoginActivity，并通过 `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` 清空所有受保护页面。

## 6. 枚举

枚举区分大小写，Android 必须发送以下字符串。

```text
MealType:
BREAKFAST, LUNCH, DINNER, SUPPER, SNACK

PrivacyLevel:
PRIVATE, GROUP

FoodGoal:
AVOID_REPETITION, SAVE_MONEY, EAT_HEALTHIER,
DISCOVER_NEW_FOOD, FOLLOW_FRIENDS

GroupRole:
OWNER, ADMIN, MEMBER

GroupMemberStatus:
ACTIVE, LEFT, REMOVED

SweetnessLevel:
ZERO, LESS, HALF, NORMAL, EXTRA

IceLevel:
NO_ICE, LESS_ICE, NORMAL_ICE, EXTRA_ICE, HOT

RecommendationType:
SAFE_OPTION, EXPLORE_OPTION, FRIEND_PICK

RecommendationSource:
HISTORY, PREFERENCE, HISTORY_AND_PREFERENCE,
FRIEND_TRUST, GROUP_POPULAR, MANUAL_CATALOG, AI_ASSISTED
```

Group Feed 额外使用：

```text
RecordType:
MEAL, DRINK
```

## 7. Endpoint 总览

| Domain | Method | Endpoint | Auth |
|---|---|---|---|
| Health | GET | `/api/health` | No |
| Auth | POST | `/api/auth/register` | No |
| Auth | POST | `/api/auth/login` | No |
| Auth | GET | `/api/auth/me` | Yes |
| Profile | GET | `/api/profile/me` | Yes |
| Profile | PUT | `/api/profile/me` | Yes |
| Profile | PUT | `/api/profile/cuisine-preferences` | Yes |
| Profile | PUT | `/api/profile/dietary-restrictions` | Yes |
| Meal | POST | `/api/meals` | Yes |
| Meal | GET | `/api/meals` | Yes |
| Meal | GET | `/api/meals/{mealId}` | Yes |
| Meal | PUT | `/api/meals/{mealId}` | Yes |
| Meal | DELETE | `/api/meals/{mealId}` | Yes |
| Drink | POST | `/api/drinks` | Yes |
| Drink | GET | `/api/drinks` | Yes |
| Drink | GET | `/api/drinks/{drinkId}` | Yes |
| Drink | PUT | `/api/drinks/{drinkId}` | Yes |
| Drink | DELETE | `/api/drinks/{drinkId}` | Yes |
| Group | POST | `/api/groups` | Yes |
| Group | GET | `/api/groups` | Yes |
| Group | POST | `/api/groups/join` | Yes |
| Group | GET | `/api/groups/{groupId}` | Yes |
| Group | GET | `/api/groups/{groupId}/members` | Yes |
| Group | GET | `/api/groups/{groupId}/feed` | Yes |
| Group | DELETE | `/api/groups/{groupId}/members/{userId}` | Yes |
| Recommendation | POST | `/api/recommendations/today` | Yes |
| Recommendation | GET | `/api/recommendations/history` | Yes |
| Recommendation | POST | `/api/recommendations/{id}/select` | Yes |
| Recommendation | POST | `/api/recommendations/{id}/reject` | Yes |
| Recommendation | POST | `/api/recommendations/{id}/eaten` | Yes |
| Analytics | GET | `/api/analytics/weekly` | Yes |
| Analytics | GET | `/api/analytics/monthly` | Yes |
| Analytics | GET | `/api/analytics/repetition` | Yes |
| Analytics | GET | `/api/analytics/spending` | Yes |

## 8. Health API

### GET `/api/health`

Response data：

```json
{
  "status": "UP",
  "service": "foodmind-backend",
  "timestamp": "2026-06-23T10:30:00Z"
}
```

仅用于开发诊断，不是正常 App 启动的强制步骤。

## 9. Authentication API

### 9.1 Register

`POST /api/auth/register`

Request：

```json
{
  "email": "dadao@example.com",
  "password": "Password123!",
  "username": "Dadao"
}
```

验证：

| Field | Rule |
|---|---|
| email | Required、合法、唯一 |
| password | Required、至少 8 字符 |
| username | Required、2～80 字符 |

当前后端 Response body：

```json
{
  "token": "jwt_token_here",
  "user": {
    "id": 1,
    "email": "dadao@example.com",
    "username": "Dadao",
    "avatarUrl": null,
    "profileCompleted": false
  }
}
```

当前 Auth 后端直接返回上述 `AuthResponse`，不包裹在通用 `success/message/data` 结构中。

Android：

- 保存 Token。
- 进入 Profile Setup。
- 409 或重复邮箱错误显示在 Email。

### 9.2 Login

`POST /api/auth/login`

Request：

```json
{
  "email": "dadao@example.com",
  "password": "Password123!"
}
```

当前后端 Response body 与 Register 相同。

Android：

- 保存 Token。
- `profileCompleted=false` → Profile Setup。
- `profileCompleted=true` → Home。
- 401 → 显示 Email/Password 错误，不触发“Session expired”文案。

### 9.3 Current User

`GET /api/auth/me`

Response data：

```json
{
  "id": 1,
  "email": "dadao@example.com",
  "username": "Dadao",
  "avatarUrl": null,
  "status": "ACTIVE",
  "profileCompleted": true,
  "createdAt": "2026-06-23T10:30:00Z"
}
```

`profileCompleted` 是前端启动导航所需字段。若当前后端尚无此字段，需要新增，或提供等价且明确的判断方式。

## 10. Profile API

### 10.1 Get Profile

`GET /api/profile/me`

Response data：

```json
{
  "userId": 1,
  "username": "Dadao",
  "avatarUrl": null,
  "budgetMin": 4.00,
  "budgetMax": 15.00,
  "spicyTolerance": 3,
  "locationArea": "NUS",
  "foodGoal": "AVOID_REPETITION",
  "defaultPrivacyLevel": "PRIVATE",
  "likedCuisines": ["Chinese", "Japanese"],
  "dislikedCuisines": ["Western Fast Food"],
  "dietaryRestrictions": ["No beef"]
}
```

未设置 Profile 时，后端应返回 200 和可空/空集合字段，不应返回 404。

### 10.2 Update Basic Profile

`PUT /api/profile/me`

Request：

```json
{
  "budgetMin": 4.00,
  "budgetMax": 15.00,
  "spicyTolerance": 3,
  "locationArea": "NUS",
  "foodGoal": "AVOID_REPETITION",
  "defaultPrivacyLevel": "PRIVATE"
}
```

规则：

- Budget 非负。
- `budgetMin <= budgetMax`。
- `spicyTolerance` 为 0～5。

### 10.3 Update Cuisine Preferences

`PUT /api/profile/cuisine-preferences`

```json
{
  "likedCuisines": ["Chinese", "Japanese"],
  "dislikedCuisines": ["Western Fast Food"]
}
```

提交内容完整替换旧列表。同一值不得同时出现在两边。

### 10.4 Update Dietary Restrictions

`PUT /api/profile/dietary-restrictions`

```json
{
  "dietaryRestrictions": ["No beef", "No peanuts"]
}
```

提交内容完整替换旧列表。

## 11. Meal API

### 11.1 Meal 对象

| Field | Type | Nullable | 说明 |
|---|---|---:|---|
| id | Long | No | Record ID |
| userId | Long | No | Owner |
| username | String | No | Owner display name |
| groupId | Long | Yes | GROUP 时所属群组 |
| foodName | String | No | 食物名称 |
| restaurantName | String | Yes | 餐厅 |
| cuisineType | String | Yes | 菜系 |
| mealType | MealType | No | Meal 类型 |
| price | Decimal | Yes | `>=0` |
| rating | Decimal | Yes | 0～5 |
| comment | String | Yes | 评论 |
| photoUrl | String | Yes | MVP 通常为 null |
| wouldEatAgain | Boolean | Yes | 是否愿意再吃 |
| privacyLevel | PrivacyLevel | No | PRIVATE/GROUP |
| consumedAt | Datetime | No | 食用时间 |
| createdAt | Datetime | No | 创建时间 |
| updatedAt | Datetime | Yes | 更新时间 |

### 11.2 Create Meal

`POST /api/meals`

```json
{
  "groupId": 1,
  "foodName": "Chicken Rice",
  "restaurantName": "NUS Canteen Store 3",
  "cuisineType": "Chinese",
  "mealType": "LUNCH",
  "price": 5.50,
  "rating": 4.5,
  "comment": "Good portion",
  "photoUrl": null,
  "wouldEatAgain": true,
  "privacyLevel": "GROUP",
  "consumedAt": "2026-06-23T12:30:00+08:00"
}
```

规则：

- `foodName`、`mealType`、`privacyLevel`、`consumedAt` 必填。
- GROUP 时 `groupId` 必填，并且用户必须为 ACTIVE 成员。
- PRIVATE 时 `groupId` 应为 null。

成功：HTTP 201，data 返回完整 Meal。

### 11.3 List Meals

`GET /api/meals`

Query：

| Parameter | Type | Required |
|---|---|---:|
| mealType | MealType | No |
| cuisineType | String | No |
| from | Datetime | No |
| to | Datetime | No |
| keyword | String | No |
| page | Int | No，默认 0 |
| size | Int | No，默认 20 |

Response data：`PageResponse<MealRecordDto>`。

### 11.4 Meal Detail

`GET /api/meals/{mealId}`

权限：

- Owner 可以查看。
- Group member 仅能查看同组且 GROUP 可见的记录。

### 11.5 Update Meal

`PUT /api/meals/{mealId}`

Request 与 Create 相同。只有 Owner 可更新。

### 11.6 Delete Meal

`DELETE /api/meals/{mealId}`

只有 Owner 可删除。

## 12. Drink API

### 12.1 Drink 对象

| Field | Type | Nullable |
|---|---|---:|
| id | Long | No |
| userId | Long | No |
| username | String | No |
| groupId | Long | Yes |
| drinkName | String | No |
| shopName | String | Yes |
| sweetnessLevel | SweetnessLevel | Yes |
| iceLevel | IceLevel | Yes |
| price | Decimal | Yes |
| rating | Decimal | Yes |
| comment | String | Yes |
| photoUrl | String | Yes |
| wouldBuyAgain | Boolean | Yes |
| privacyLevel | PrivacyLevel | No |
| consumedAt | Datetime | No |
| createdAt | Datetime | No |
| updatedAt | Datetime | Yes |

### 12.2 Create Drink

`POST /api/drinks`

```json
{
  "groupId": 1,
  "drinkName": "Matcha Latte",
  "shopName": "NUS Coffee House",
  "sweetnessLevel": "HALF",
  "iceLevel": "LESS_ICE",
  "price": 4.80,
  "rating": 4.0,
  "comment": "Good matcha taste",
  "photoUrl": null,
  "wouldBuyAgain": true,
  "privacyLevel": "GROUP",
  "consumedAt": "2026-06-23T15:00:00+08:00"
}
```

必填：`drinkName`、`privacyLevel`、`consumedAt`。GROUP 规则与 Meal 相同。

### 12.3 List Drinks

`GET /api/drinks`

Query：

- `sweetnessLevel`
- `iceLevel`
- `from`
- `to`
- `keyword`
- `page`
- `size`

Response data：`PageResponse<DrinkRecordDto>`。

### 12.4 Detail/Update/Delete

```text
GET    /api/drinks/{drinkId}
PUT    /api/drinks/{drinkId}
DELETE /api/drinks/{drinkId}
```

更新和删除仅限 Owner。查看权限与 Meal 一致。

## 13. Group API

### 13.1 Create Group

`POST /api/groups`

Request：

```json
{
  "name": "NUS Food Squad",
  "description": "Food sharing group for lunch and dinner ideas"
}
```

Response data：

```json
{
  "id": 1,
  "name": "NUS Food Squad",
  "description": "Food sharing group for lunch and dinner ideas",
  "ownerId": 1,
  "ownerUsername": "Dadao",
  "inviteCode": "A8X9K2",
  "memberCount": 1,
  "createdAt": "2026-06-23T10:30:00+08:00"
}
```

创建者自动成为 OWNER 和 ACTIVE member。

### 13.2 My Groups

`GET /api/groups`

Response data：

```json
[
  {
    "id": 1,
    "name": "NUS Food Squad",
    "description": "Food sharing group",
    "role": "OWNER",
    "memberCount": 5,
    "inviteCode": "A8X9K2",
    "createdAt": "2026-06-23T10:30:00+08:00"
  }
]
```

### 13.3 Join Group

`POST /api/groups/join`

```json
{
  "inviteCode": "A8X9K2"
}
```

Response data：

```json
{
  "groupId": 1,
  "groupName": "NUS Food Squad",
  "role": "MEMBER",
  "joinedAt": "2026-06-23T11:00:00+08:00"
}
```

无效 Code → 404 或 400；已加入 → 409。后端应固定状态码。

### 13.4 Group Detail

`GET /api/groups/{groupId}`

仅 ACTIVE member 可访问。

返回字段：

- id
- name
- description
- ownerId
- ownerUsername
- inviteCode
- memberCount
- myRole
- createdAt

### 13.5 Group Members

`GET /api/groups/{groupId}/members`

成员字段：

- userId
- username
- avatarUrl
- role
- status
- joinedAt

### 13.6 Group Feed

`GET /api/groups/{groupId}/feed`

Query：

| Parameter | Type |
|---|---|
| recordType | MEAL/DRINK |
| mealType | MealType |
| from | Datetime |
| to | Datetime |
| minRating | Decimal |
| keyword | String |
| page | Int |
| size | Int |

Feed item：

```json
{
  "recordType": "MEAL",
  "recordId": 101,
  "groupId": 1,
  "userId": 1,
  "username": "Dadao",
  "avatarUrl": null,
  "itemName": "Chicken Rice",
  "placeName": "NUS Canteen Store 3",
  "cuisineType": "Chinese",
  "mealType": "LUNCH",
  "price": 5.50,
  "rating": 4.5,
  "comment": "Good portion",
  "photoUrl": null,
  "wouldHaveAgain": true,
  "consumedAt": "2026-06-23T12:30:00+08:00",
  "createdAt": "2026-06-23T12:35:00+08:00"
}
```

### 13.7 Remove Member

`DELETE /api/groups/{groupId}/members/{userId}`

规则：

- OWNER/ADMIN 可移除 MEMBER。
- ADMIN 是否能移除 ADMIN 需后端确认。
- 不能移除 OWNER。

### 13.8 缺失但需要确认的接口

当前产品流程可能需要：

```http
POST /api/groups/{groupId}/leave
DELETE /api/groups/{groupId}
```

如果不进入本次 MVP，应在 UI 中不展示 Leave/Delete 功能。

## 14. Recommendation API

### 14.1 Generate Today Recommendations

`POST /api/recommendations/today`

Request：

```json
{
  "mealType": "LUNCH",
  "budgetMax": 12.00,
  "groupId": 1,
  "mood": "quick",
  "avoidItems": ["Fried Chicken"],
  "locationArea": "NUS"
}
```

Response data：

```json
{
  "requestId": 501,
  "recommendations": [
    {
      "id": 9001,
      "rankPosition": 1,
      "name": "Chicken Rice",
      "restaurantName": "NUS Canteen Store 3",
      "recommendationType": "SAFE_OPTION",
      "source": "HISTORY_AND_PREFERENCE",
      "score": 86.50,
      "reason": "It fits your budget and you have not eaten it recently.",
      "selected": false,
      "rejected": false
    }
  ]
}
```

数据不足时建议仍返回 HTTP 200：

```json
{
  "success": true,
  "message": "Not enough data for recommendations",
  "data": {
    "requestId": 501,
    "recommendations": []
  }
}
```

### 14.2 Recommendation History

`GET /api/recommendations/history?page=0&size=20`

Response data：分页的 Recommendation Request 历史。

### 14.3 Select

`POST /api/recommendations/{recommendationItemId}/select`

无 Request Body。

返回：

- recommendationItemId
- selected
- selectedAt

### 14.4 Reject

`POST /api/recommendations/{recommendationItemId}/reject`

```json
{
  "reason": "Too expensive"
}
```

返回：

- recommendationItemId
- rejected
- rejectedAt

### 14.5 Mark as Eaten

`POST /api/recommendations/{recommendationItemId}/eaten`

```json
{
  "rating": 4.5,
  "comment": "Good recommendation",
  "wouldEatAgain": true,
  "privacyLevel": "PRIVATE",
  "groupId": null,
  "consumedAt": "2026-06-23T12:30:00+08:00"
}
```

返回：

```json
{
  "mealRecordId": 101,
  "recommendationItemId": 9001,
  "foodName": "Chicken Rice",
  "restaurantName": "NUS Canteen Store 3"
}
```

### 14.6 推荐状态待确认

后端必须明确：

- 同一 Request 是否只能选择一个 Item。
- Select 后能否 Reject。
- Reject 后能否 Select。
- Eaten 是否要求先 Select。
- 同一 Item 重复 Eaten 的响应。
- 生成新推荐是否改变旧推荐状态。

建议规则：

- 一个 Request 最多一个 Selected。
- Selected 和 Rejected 互斥。
- Eaten 要求 Selected。
- Eaten 只能执行一次，重复请求返回 409。

## 15. Analytics API

### 15.1 Weekly

`GET /api/analytics/weekly?weekStart=2026-06-22`

Response data：

```json
{
  "weekStart": "2026-06-22",
  "weekEnd": "2026-06-28",
  "mealCount": 12,
  "drinkCount": 5,
  "totalSpending": 86.30,
  "averageMealRating": 4.2,
  "mostRepeatedFood": "Fried Chicken",
  "mostFrequentCuisine": "Chinese",
  "spicyMealCount": 4,
  "sweetDrinkCount": 3,
  "suggestion": "Try a lighter meal tomorrow."
}
```

新用户时文本和平均值字段可为 null，Count/Spending 建议为 0。

### 15.2 Monthly

`GET /api/analytics/monthly?month=2026-06`

返回：

- month
- mealCount
- drinkCount
- totalSpending
- averageRating
- topFoods: `{name, count}[]`
- topCuisines: `{name, count}[]`

### 15.3 Repetition

`GET /api/analytics/repetition?days=7`

返回：

- days
- repeatedFoods: `{foodName, count, lastConsumedAt}[]`
- repeatedCuisines: `{cuisineType, count}[]`

### 15.4 Spending

`GET /api/analytics/spending?from=2026-06-01&to=2026-06-30`

返回：

- from
- to
- totalMealSpending
- totalDrinkSpending
- totalSpending
- dailySpending: `{date, amount}[]`

## 16. Screen 到 API 映射

| Screen | API |
|---|---|
| Login | POST auth/login |
| Register | POST auth/register |
| App Launch | GET auth/me |
| Profile Setup/Profile | GET/PUT profile |
| Home | GET auth/me、analytics/weekly、meals/drinks |
| Add Meal | GET groups、POST/PUT meals |
| Add Drink | GET groups、POST/PUT drinks |
| History | GET meals、GET drinks |
| Meal Detail | GET/DELETE meal |
| Drink Detail | GET/DELETE drink |
| Groups | GET/POST groups、POST groups/join |
| Group Detail | GET group、GET members、DELETE member |
| Group Feed | GET group feed |
| Recommendation | POST today/select/reject/eaten |
| Analytics | GET analytics/* |

## 17. Android DTO 空值规则

以下字段必须 nullable：

- 所有 avatarUrl/photoUrl。
- restaurantName/shopName/placeName。
- cuisineType。
- groupId。
- price、rating。
- comment。
- wouldEatAgain/wouldBuyAgain/wouldHaveAgain。
- Analytics 中尚无统计结果的文本/平均值。

以下字段不应为 null：

- 所有 ID。
- `success`、`message`。
- 分页字段和 `content`。
- 必填 enum。
- `consumedAt`、`createdAt`。
- 列表字段；无值时返回 `[]`，不要返回 null。

## 18. API 兼容验收

- Register/Login 返回可用 Token。
- Interceptor 发送 Bearer Token。
- `/auth/me` 可判断 Profile 是否完成。
- Profile 三类数据可读取和覆盖保存。
- Meal/Drink 完整 CRUD 可用。
- History 和 Feed 分页结构一致。
- Group 权限返回 403，而不是 500。
- Recommendation 状态规则固定。
- Analytics 对新用户安全返回。
- 错误响应始终包含 `success=false` 和可读 `message`。
- 所有时间为 ISO 8601。
