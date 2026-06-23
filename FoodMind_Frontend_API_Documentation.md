# FoodMind Frontend API Documentation

Version: 1.0  
Target client: Android app / Web frontend  
Backend: Spring Boot 3 + PostgreSQL  
Base URL local: `http://localhost:8080`  
API prefix: `/api`

## 01. MVP Summary

FoodMind is an Android-first food decision assistant. The MVP should answer one core question:

> What should I eat today, based on my own history and people I trust?

The MVP backend supports this complete loop:

```text
Register/Login
→ Set food preferences
→ Log meals and drinks
→ View personal history
→ Create or join a food group
→ View group food feed
→ Request recommendations
→ Select or reject recommendations
→ View weekly analytics
```

The MVP should not become a delivery platform, public social network, restaurant marketplace, or medical nutrition product. It should focus on food memory, group sharing, and explainable recommendations.

---

## 02. MVP Goals

### Product Goals

1. Allow users to register and log in.
2. Allow users to set food preferences, budget, and dietary restrictions.
3. Allow users to log meals and drinks quickly.
4. Allow users to view personal meal and drink history.
5. Allow users to create and join food groups.
6. Allow users to view group food logs.
7. Allow users to get three explainable food recommendations.
8. Allow users to select or reject recommendations.
9. Allow users to view weekly and monthly food analytics.

### Technical Goals

1. Build a clean Spring Boot REST API.
2. Use PostgreSQL as the primary database.
3. Use JWT authentication.
4. Use ownership and group-membership permission checks.
5. Use DTOs rather than exposing JPA entities directly.
6. Use Swagger/OpenAPI for API documentation.
7. Keep recommendation logic rule-based in the MVP.
8. Keep AI integration optional until deterministic recommendation logic works.

---

## 1. Purpose

This document defines the API contract between the FoodMind backend and the frontend application.

The frontend should use this document to implement:

- Authentication screens
- Profile and preference setup
- Meal logging
- Drink logging
- Personal history
- Group creation and group feed
- Food recommendation screen
- Weekly analytics screen

---

## 2. Global API Rules

### 2.1 Base URL

Local development:

```text
http://localhost:8080
```

Example full endpoint:

```http
POST http://localhost:8080/api/auth/login
```

---

### 2.2 Request Format

All normal API requests use JSON:

```http
Content-Type: application/json
```

Photo upload is not included in the MVP. If photo upload is added later, it will use:

```http
Content-Type: multipart/form-data
```

---

### 2.3 Authentication Header

After login/register, the frontend receives a JWT token.

For protected APIs, add:

```http
Authorization: Bearer <jwt_token>
```

Example:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### 2.4 Common Success Response

All successful responses should use this wrapper:

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

Example:

```json
{
  "success": true,
  "message": "Meal created successfully",
  "data": {
    "id": 101,
    "foodName": "Chicken Rice"
  }
}
```

---

### 2.5 Common Error Response

All failed responses should use this wrapper:

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

---

### 2.6 Common HTTP Status Codes

| Status | Meaning | Frontend Handling |
|---|---|---|
| `200` | Success | Continue normal flow |
| `201` | Created | Show success state |
| `204` | Deleted successfully | Remove item from UI |
| `400` | Bad request / validation error | Show field or toast error |
| `401` | Unauthorized / token invalid | Redirect to login |
| `403` | Forbidden / no permission | Show permission error |
| `404` | Resource not found | Show not-found state |
| `409` | Conflict | Show duplicate/conflict message |
| `500` | Server error | Show generic error |

---

## 3. Enum Values

The frontend should use these exact string values.

### 3.1 Meal Type

```ts
type MealType =
  | "BREAKFAST"
  | "LUNCH"
  | "DINNER"
  | "SUPPER"
  | "SNACK";
```

---

### 3.2 Privacy Level

```ts
type PrivacyLevel =
  | "PRIVATE"
  | "GROUP";
```

MVP only needs `PRIVATE` and `GROUP`.

---

### 3.3 Food Goal

```ts
type FoodGoal =
  | "AVOID_REPETITION"
  | "SAVE_MONEY"
  | "EAT_HEALTHIER"
  | "DISCOVER_NEW_FOOD"
  | "FOLLOW_FRIENDS";
```

---

### 3.4 Group Role

```ts
type GroupRole =
  | "OWNER"
  | "ADMIN"
  | "MEMBER";
```

---

### 3.5 Group Member Status

```ts
type GroupMemberStatus =
  | "ACTIVE"
  | "LEFT"
  | "REMOVED";
```

---

### 3.6 Sweetness Level

```ts
type SweetnessLevel =
  | "ZERO"
  | "LESS"
  | "HALF"
  | "NORMAL"
  | "EXTRA";
```

---

### 3.7 Ice Level

```ts
type IceLevel =
  | "NO_ICE"
  | "LESS_ICE"
  | "NORMAL_ICE"
  | "EXTRA_ICE"
  | "HOT";
```

---

### 3.8 Recommendation Type

```ts
type RecommendationType =
  | "SAFE_OPTION"
  | "EXPLORE_OPTION"
  | "FRIEND_PICK";
```

---

### 3.9 Recommendation Source

```ts
type RecommendationSource =
  | "HISTORY"
  | "PREFERENCE"
  | "HISTORY_AND_PREFERENCE"
  | "FRIEND_TRUST"
  | "GROUP_POPULAR"
  | "MANUAL_CATALOG"
  | "AI_ASSISTED";
```

---

## 4. Pagination Format

List APIs should support pagination.

### 4.1 Request Query Parameters

```http
?page=0&size=20&sort=createdAt,desc
```

### 4.2 Common Paginated Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  }
}
```

Frontend notes:

- `page` starts from `0`
- Use `last = true` to stop infinite scroll
- Default sort should usually be newest first

---

# 5. Health API

## 5.1 Health Check

```http
GET /api/health
```

Auth required: No

### Response

```json
{
  "success": true,
  "message": "FoodMind backend is running",
  "data": {
    "status": "UP",
    "service": "foodmind-backend",
    "timestamp": "2026-06-23T10:30:00Z"
  }
}
```

Frontend usage:

- Used for backend connectivity testing
- Not required in normal user flow

---

# 6. Authentication APIs

## 6.1 Register

```http
POST /api/auth/register
```

Auth required: No

### Request

```json
{
  "email": "dadao@example.com",
  "password": "Password123!",
  "username": "Dadao"
}
```

### Validation Rules

| Field | Rule |
|---|---|
| `email` | Required, valid email, unique |
| `password` | Required, minimum 8 characters |
| `username` | Required, 2 to 80 characters |

### Response

```json
{
  "success": true,
  "message": "Registered successfully",
  "data": {
    "token": "jwt_token_here",
    "user": {
      "id": 1,
      "email": "dadao@example.com",
      "username": "Dadao",
      "avatarUrl": null
    }
  }
}
```

Frontend behavior:

- Save `token` to secure storage
- Navigate to preference setup screen
- If email already exists, show duplicate email error

---

## 6.2 Login

```http
POST /api/auth/login
```

Auth required: No

### Request

```json
{
  "email": "dadao@example.com",
  "password": "Password123!"
}
```

### Response

```json
{
  "success": true,
  "message": "Logged in successfully",
  "data": {
    "token": "jwt_token_here",
    "user": {
      "id": 1,
      "email": "dadao@example.com",
      "username": "Dadao",
      "avatarUrl": null
    }
  }
}
```

Frontend behavior:

- Save JWT token
- Navigate to Home screen
- On `401`, show invalid email/password message

---

## 6.3 Get Current User

```http
GET /api/auth/me
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "email": "dadao@example.com",
    "username": "Dadao",
    "avatarUrl": null,
    "status": "ACTIVE",
    "createdAt": "2026-06-23T10:30:00Z"
  }
}
```

Frontend behavior:

- Used to restore login state after app launch
- If `401`, clear token and navigate to login

---

# 7. Profile APIs

## 7.1 Get My Profile

```http
GET /api/profile/me
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "userId": 1,
    "username": "Dadao",
    "avatarUrl": null,
    "budgetMin": 4.00,
    "budgetMax": 15.00,
    "spicyTolerance": 3,
    "locationArea": "NUS",
    "foodGoal": "AVOID_REPETITION",
    "defaultPrivacyLevel": "PRIVATE",
    "likedCuisines": ["Chinese", "Japanese", "Vietnamese"],
    "dislikedCuisines": ["Western Fast Food"],
    "dietaryRestrictions": ["No beef"]
  }
}
```

Frontend behavior:

- Display profile setup values
- If profile does not exist, backend should return default empty values

---

## 7.2 Update My Profile

```http
PUT /api/profile/me
```

Auth required: Yes

### Request

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

### Response

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "userId": 1,
    "budgetMin": 4.00,
    "budgetMax": 15.00,
    "spicyTolerance": 3,
    "locationArea": "NUS",
    "foodGoal": "AVOID_REPETITION",
    "defaultPrivacyLevel": "PRIVATE"
  }
}
```

Frontend behavior:

- Submit when user completes preference setup
- Show validation errors for invalid budget or spicy tolerance

---

## 7.3 Update Cuisine Preferences

```http
PUT /api/profile/cuisine-preferences
```

Auth required: Yes

### Request

```json
{
  "likedCuisines": ["Chinese", "Japanese", "Vietnamese"],
  "dislikedCuisines": ["Western Fast Food"]
}
```

### Response

```json
{
  "success": true,
  "message": "Cuisine preferences updated successfully",
  "data": {
    "likedCuisines": ["Chinese", "Japanese", "Vietnamese"],
    "dislikedCuisines": ["Western Fast Food"]
  }
}
```

Frontend behavior:

- Replace old cuisine preferences with the submitted list
- Do not allow the same cuisine in both liked and disliked lists

---

## 7.4 Update Dietary Restrictions

```http
PUT /api/profile/dietary-restrictions
```

Auth required: Yes

### Request

```json
{
  "dietaryRestrictions": ["No beef", "No peanuts"]
}
```

### Response

```json
{
  "success": true,
  "message": "Dietary restrictions updated successfully",
  "data": {
    "dietaryRestrictions": ["No beef", "No peanuts"]
  }
}
```

---

# 8. Meal APIs

## 8.1 Create Meal Record

```http
POST /api/meals
```

Auth required: Yes

### Request

```json
{
  "groupId": 1,
  "foodName": "Chicken Rice",
  "restaurantName": "NUS Canteen Store 3",
  "cuisineType": "Chinese",
  "mealType": "LUNCH",
  "price": 5.50,
  "rating": 4.5,
  "comment": "Good portion, not too oily",
  "photoUrl": null,
  "wouldEatAgain": true,
  "privacyLevel": "GROUP",
  "consumedAt": "2026-06-23T12:30:00+08:00"
}
```

### Validation Rules

| Field | Rule |
|---|---|
| `foodName` | Required |
| `mealType` | Required |
| `price` | Optional, must be >= 0 |
| `rating` | Optional, 0 to 5 |
| `privacyLevel` | Required |
| `groupId` | Required if `privacyLevel = GROUP` |
| `consumedAt` | Required |

### Response

```json
{
  "success": true,
  "message": "Meal record created successfully",
  "data": {
    "id": 101,
    "userId": 1,
    "username": "Dadao",
    "groupId": 1,
    "foodName": "Chicken Rice",
    "restaurantName": "NUS Canteen Store 3",
    "cuisineType": "Chinese",
    "mealType": "LUNCH",
    "price": 5.50,
    "rating": 4.5,
    "comment": "Good portion, not too oily",
    "photoUrl": null,
    "wouldEatAgain": true,
    "privacyLevel": "GROUP",
    "consumedAt": "2026-06-23T12:30:00+08:00",
    "createdAt": "2026-06-23T12:35:00+08:00"
  }
}
```

Frontend behavior:

- After success, navigate back to History or Group Feed
- If privacy is `GROUP`, require user to select a group
- If user has no group, hide or disable `GROUP` privacy option

---

## 8.2 Get My Meal Records

```http
GET /api/meals
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `mealType` | string | No | Filter by meal type |
| `cuisineType` | string | No | Filter by cuisine |
| `from` | datetime | No | Start consumed time |
| `to` | datetime | No | End consumed time |
| `keyword` | string | No | Search food or restaurant |
| `page` | number | No | Default `0` |
| `size` | number | No | Default `20` |

### Example

```http
GET /api/meals?mealType=LUNCH&from=2026-06-01T00:00:00+08:00&to=2026-06-30T23:59:59+08:00&page=0&size=20
```

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": 101,
        "foodName": "Chicken Rice",
        "restaurantName": "NUS Canteen Store 3",
        "cuisineType": "Chinese",
        "mealType": "LUNCH",
        "price": 5.50,
        "rating": 4.5,
        "wouldEatAgain": true,
        "privacyLevel": "GROUP",
        "consumedAt": "2026-06-23T12:30:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 8.3 Get Meal Record Detail

```http
GET /api/meals/{mealId}
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 101,
    "userId": 1,
    "username": "Dadao",
    "groupId": 1,
    "foodName": "Chicken Rice",
    "restaurantName": "NUS Canteen Store 3",
    "cuisineType": "Chinese",
    "mealType": "LUNCH",
    "price": 5.50,
    "rating": 4.5,
    "comment": "Good portion, not too oily",
    "photoUrl": null,
    "wouldEatAgain": true,
    "privacyLevel": "GROUP",
    "consumedAt": "2026-06-23T12:30:00+08:00",
    "createdAt": "2026-06-23T12:35:00+08:00",
    "updatedAt": "2026-06-23T12:35:00+08:00"
  }
}
```

Permission rule:

- Owner can view
- Group member can view only if `privacyLevel = GROUP` and record belongs to their group

---

## 8.4 Update Meal Record

```http
PUT /api/meals/{mealId}
```

Auth required: Yes

### Request

```json
{
  "groupId": 1,
  "foodName": "Chicken Rice",
  "restaurantName": "NUS Canteen Store 3",
  "cuisineType": "Chinese",
  "mealType": "LUNCH",
  "price": 5.80,
  "rating": 4.0,
  "comment": "Still good, but price increased",
  "photoUrl": null,
  "wouldEatAgain": true,
  "privacyLevel": "GROUP",
  "consumedAt": "2026-06-23T12:30:00+08:00"
}
```

### Response

```json
{
  "success": true,
  "message": "Meal record updated successfully",
  "data": {
    "id": 101,
    "foodName": "Chicken Rice",
    "price": 5.80,
    "rating": 4.0,
    "updatedAt": "2026-06-23T13:00:00+08:00"
  }
}
```

Permission rule:

- Only owner can update

---

## 8.5 Delete Meal Record

```http
DELETE /api/meals/{mealId}
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "Meal record deleted successfully",
  "data": null
}
```

Permission rule:

- Only owner can delete

---

# 9. Drink APIs

## 9.1 Create Drink Record

```http
POST /api/drinks
```

Auth required: Yes

### Request

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

### Response

```json
{
  "success": true,
  "message": "Drink record created successfully",
  "data": {
    "id": 201,
    "userId": 1,
    "username": "Dadao",
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
    "consumedAt": "2026-06-23T15:00:00+08:00",
    "createdAt": "2026-06-23T15:05:00+08:00"
  }
}
```

---

## 9.2 Get My Drink Records

```http
GET /api/drinks
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `sweetnessLevel` | string | No | Filter by sweetness |
| `iceLevel` | string | No | Filter by ice level |
| `from` | datetime | No | Start consumed time |
| `to` | datetime | No | End consumed time |
| `keyword` | string | No | Search drink or shop |
| `page` | number | No | Default `0` |
| `size` | number | No | Default `20` |

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": 201,
        "drinkName": "Matcha Latte",
        "shopName": "NUS Coffee House",
        "sweetnessLevel": "HALF",
        "iceLevel": "LESS_ICE",
        "price": 4.80,
        "rating": 4.0,
        "wouldBuyAgain": true,
        "privacyLevel": "GROUP",
        "consumedAt": "2026-06-23T15:00:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 9.3 Get Drink Record Detail

```http
GET /api/drinks/{drinkId}
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 201,
    "userId": 1,
    "username": "Dadao",
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
    "consumedAt": "2026-06-23T15:00:00+08:00",
    "createdAt": "2026-06-23T15:05:00+08:00",
    "updatedAt": "2026-06-23T15:05:00+08:00"
  }
}
```

---

## 9.4 Update Drink Record

```http
PUT /api/drinks/{drinkId}
```

Auth required: Yes

### Request

```json
{
  "groupId": 1,
  "drinkName": "Matcha Latte",
  "shopName": "NUS Coffee House",
  "sweetnessLevel": "HALF",
  "iceLevel": "LESS_ICE",
  "price": 4.80,
  "rating": 4.5,
  "comment": "Better than last time",
  "photoUrl": null,
  "wouldBuyAgain": true,
  "privacyLevel": "GROUP",
  "consumedAt": "2026-06-23T15:00:00+08:00"
}
```

### Response

```json
{
  "success": true,
  "message": "Drink record updated successfully",
  "data": {
    "id": 201,
    "drinkName": "Matcha Latte",
    "rating": 4.5,
    "updatedAt": "2026-06-23T16:00:00+08:00"
  }
}
```

---

## 9.5 Delete Drink Record

```http
DELETE /api/drinks/{drinkId}
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "Drink record deleted successfully",
  "data": null
}
```

---

# 10. Group APIs

## 10.1 Create Group

```http
POST /api/groups
```

Auth required: Yes

### Request

```json
{
  "name": "NUS Food Squad",
  "description": "Food sharing group for lunch and dinner ideas"
}
```

### Response

```json
{
  "success": true,
  "message": "Group created successfully",
  "data": {
    "id": 1,
    "name": "NUS Food Squad",
    "description": "Food sharing group for lunch and dinner ideas",
    "ownerId": 1,
    "ownerUsername": "Dadao",
    "inviteCode": "A8X9K2",
    "memberCount": 1,
    "createdAt": "2026-06-23T10:30:00+08:00"
  }
}
```

Frontend behavior:

- Show invite code after group creation
- Add current user as owner automatically

---

## 10.2 Get My Groups

```http
GET /api/groups
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "name": "NUS Food Squad",
      "description": "Food sharing group for lunch and dinner ideas",
      "role": "OWNER",
      "memberCount": 5,
      "inviteCode": "A8X9K2",
      "createdAt": "2026-06-23T10:30:00+08:00"
    }
  ]
}
```

---

## 10.3 Join Group

```http
POST /api/groups/join
```

Auth required: Yes

### Request

```json
{
  "inviteCode": "A8X9K2"
}
```

### Response

```json
{
  "success": true,
  "message": "Joined group successfully",
  "data": {
    "groupId": 1,
    "groupName": "NUS Food Squad",
    "role": "MEMBER",
    "joinedAt": "2026-06-23T11:00:00+08:00"
  }
}
```

Frontend behavior:

- If invite code is invalid, show error
- If user already joined, show already-a-member message

---

## 10.4 Get Group Detail

```http
GET /api/groups/{groupId}
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "name": "NUS Food Squad",
    "description": "Food sharing group for lunch and dinner ideas",
    "ownerId": 1,
    "ownerUsername": "Dadao",
    "inviteCode": "A8X9K2",
    "memberCount": 5,
    "myRole": "OWNER",
    "createdAt": "2026-06-23T10:30:00+08:00"
  }
}
```

Permission rule:

- Only active group members can view group detail

---

## 10.5 Get Group Members

```http
GET /api/groups/{groupId}/members
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "userId": 1,
      "username": "Dadao",
      "avatarUrl": null,
      "role": "OWNER",
      "status": "ACTIVE",
      "joinedAt": "2026-06-23T10:30:00+08:00"
    },
    {
      "userId": 2,
      "username": "Alice",
      "avatarUrl": null,
      "role": "MEMBER",
      "status": "ACTIVE",
      "joinedAt": "2026-06-23T11:00:00+08:00"
    }
  ]
}
```

---

## 10.6 Get Group Feed

```http
GET /api/groups/{groupId}/feed
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `recordType` | string | No | `MEAL` or `DRINK` |
| `mealType` | string | No | Only applies to meals |
| `from` | datetime | No | Start consumed time |
| `to` | datetime | No | End consumed time |
| `minRating` | number | No | Minimum rating |
| `keyword` | string | No | Search item or place |
| `page` | number | No | Default `0` |
| `size` | number | No | Default `20` |

### Example

```http
GET /api/groups/1/feed?recordType=MEAL&minRating=4&page=0&size=20
```

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [
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
        "comment": "Good portion, not too oily",
        "photoUrl": null,
        "wouldHaveAgain": true,
        "consumedAt": "2026-06-23T12:30:00+08:00",
        "createdAt": "2026-06-23T12:35:00+08:00"
      },
      {
        "recordType": "DRINK",
        "recordId": 201,
        "groupId": 1,
        "userId": 2,
        "username": "Alice",
        "avatarUrl": null,
        "itemName": "Matcha Latte",
        "placeName": "NUS Coffee House",
        "cuisineType": null,
        "mealType": null,
        "price": 4.80,
        "rating": 4.0,
        "comment": "Good matcha taste",
        "photoUrl": null,
        "wouldHaveAgain": true,
        "consumedAt": "2026-06-23T15:00:00+08:00",
        "createdAt": "2026-06-23T15:05:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  }
}
```

Frontend behavior:

- Display mixed meal/drink feed
- Use `recordType` to choose card style
- Only group-shared records appear here

---

## 10.7 Remove Group Member

```http
DELETE /api/groups/{groupId}/members/{userId}
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "Group member removed successfully",
  "data": null
}
```

Permission rule:

- Only group owner/admin can remove members
- Normal member can only leave themselves if backend allows this endpoint for self-removal

---

# 11. Recommendation APIs

## 11.1 Get Today Recommendations

```http
POST /api/recommendations/today
```

Auth required: Yes

### Request

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

### Response

```json
{
  "success": true,
  "message": "Recommendations generated successfully",
  "data": {
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
        "reason": "You liked this before, it fits your budget, and you have not eaten it recently.",
        "selected": false,
        "rejected": false
      },
      {
        "id": 9002,
        "rankPosition": 2,
        "name": "Vietnamese Pho",
        "restaurantName": "NUS Food Court",
        "recommendationType": "EXPLORE_OPTION",
        "source": "PREFERENCE",
        "score": 79.20,
        "reason": "You ate fried food several times recently. This gives you a lighter soup-based option.",
        "selected": false,
        "rejected": false
      },
      {
        "id": 9003,
        "rankPosition": 3,
        "name": "Beef Noodles",
        "restaurantName": "NUS Canteen Store 5",
        "recommendationType": "FRIEND_PICK",
        "source": "FRIEND_TRUST",
        "score": 82.00,
        "reason": "Two group members rated similar noodle dishes highly.",
        "selected": false,
        "rejected": false
      }
    ]
  }
}
```

Frontend behavior:

- Display exactly three cards if available
- Show `reason` under each recommendation
- Use `recommendationType` for label:
  - `SAFE_OPTION`: Safe
  - `EXPLORE_OPTION`: Explore
  - `FRIEND_PICK`: Friend Pick

---

## 11.2 Get Recommendation History

```http
GET /api/recommendations/history
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `page` | number | No | Default `0` |
| `size` | number | No | Default `20` |

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [
      {
        "requestId": 501,
        "mealType": "LUNCH",
        "budgetMax": 12.00,
        "locationArea": "NUS",
        "createdAt": "2026-06-23T11:50:00+08:00",
        "recommendations": [
          {
            "id": 9001,
            "name": "Chicken Rice",
            "recommendationType": "SAFE_OPTION",
            "score": 86.50,
            "selected": true,
            "rejected": false
          }
        ]
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 11.3 Select Recommendation

```http
POST /api/recommendations/{recommendationItemId}/select
```

Auth required: Yes

### Response

```json
{
  "success": true,
  "message": "Recommendation selected successfully",
  "data": {
    "recommendationItemId": 9001,
    "selected": true,
    "selectedAt": "2026-06-23T12:00:00+08:00"
  }
}
```

Frontend behavior:

- Mark selected card
- Offer action: “Log this meal”

---

## 11.4 Reject Recommendation

```http
POST /api/recommendations/{recommendationItemId}/reject
```

Auth required: Yes

### Request

```json
{
  "reason": "Too expensive"
}
```

### Response

```json
{
  "success": true,
  "message": "Recommendation rejected successfully",
  "data": {
    "recommendationItemId": 9001,
    "rejected": true,
    "rejectedAt": "2026-06-23T12:00:00+08:00"
  }
}
```

Frontend behavior:

- Grey out or remove card
- Optionally request a new recommendation later

---

## 11.5 Mark Recommendation as Eaten

```http
POST /api/recommendations/{recommendationItemId}/eaten
```

Auth required: Yes

This endpoint converts a selected recommendation into a meal record.

### Request

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

### Response

```json
{
  "success": true,
  "message": "Recommendation converted to meal record successfully",
  "data": {
    "mealRecordId": 101,
    "recommendationItemId": 9001,
    "foodName": "Chicken Rice",
    "restaurantName": "NUS Canteen Store 3"
  }
}
```

Frontend behavior:

- Use this after user chooses “I ate this”
- Navigate to meal detail or history screen

---

# 12. Analytics APIs

## 12.1 Weekly Analytics

```http
GET /api/analytics/weekly
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `weekStart` | date | No | Example: `2026-06-22` |

If `weekStart` is not provided, backend uses the current week.

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
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
    "suggestion": "You ate fried food several times this week. Try a lighter meal tomorrow."
  }
}
```

Frontend behavior:

- Display summary cards
- Use null-safe rendering because some fields may be null for new users

---

## 12.2 Monthly Analytics

```http
GET /api/analytics/monthly
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|
| `month` | string | No | Format: `YYYY-MM`, example `2026-06` |

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "month": "2026-06",
    "mealCount": 42,
    "drinkCount": 18,
    "totalSpending": 320.50,
    "averageRating": 4.1,
    "topFoods": [
      {
        "name": "Chicken Rice",
        "count": 5
      }
    ],
    "topCuisines": [
      {
        "name": "Chinese",
        "count": 16
      }
    ]
  }
}
```

---

## 12.3 Repetition Analytics

```http
GET /api/analytics/repetition
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `days` | number | No | Default `7` |

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "days": 7,
    "repeatedFoods": [
      {
        "foodName": "Fried Chicken",
        "count": 3,
        "lastConsumedAt": "2026-06-22T12:30:00+08:00"
      }
    ],
    "repeatedCuisines": [
      {
        "cuisineType": "Chinese",
        "count": 5
      }
    ]
  }
}
```

---

## 12.4 Spending Analytics

```http
GET /api/analytics/spending
```

Auth required: Yes

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `from` | date | No | Start date |
| `to` | date | No | End date |

### Response

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "from": "2026-06-01",
    "to": "2026-06-30",
    "totalMealSpending": 240.50,
    "totalDrinkSpending": 80.00,
    "totalSpending": 320.50,
    "dailySpending": [
      {
        "date": "2026-06-23",
        "amount": 18.30
      }
    ]
  }
}
```

---

# 13. Frontend Screen-to-API Mapping

## 13.1 Login Screen

| Action | API |
|---|---|
| Register | `POST /api/auth/register` |
| Login | `POST /api/auth/login` |

---

## 13.2 App Launch

| Action | API |
|---|---|
| Validate token | `GET /api/auth/me` |
| Load profile | `GET /api/profile/me` |
| Load groups | `GET /api/groups` |

---

## 13.3 Profile Setup Screen

| Action | API |
|---|---|
| Load profile | `GET /api/profile/me` |
| Save budget/location/goal | `PUT /api/profile/me` |
| Save cuisine preferences | `PUT /api/profile/cuisine-preferences` |
| Save dietary restrictions | `PUT /api/profile/dietary-restrictions` |

---

## 13.4 Home Screen

| Action | API |
|---|---|
| Load current user | `GET /api/auth/me` |
| Load weekly analytics | `GET /api/analytics/weekly` |
| Generate recommendation | `POST /api/recommendations/today` |

---

## 13.5 Log Screen

| Action | API |
|---|---|
| Create meal | `POST /api/meals` |
| Create drink | `POST /api/drinks` |
| Load groups for sharing selector | `GET /api/groups` |

---

## 13.6 History Screen

| Action | API |
|---|---|
| Load meal history | `GET /api/meals` |
| Load drink history | `GET /api/drinks` |
| Open meal detail | `GET /api/meals/{mealId}` |
| Open drink detail | `GET /api/drinks/{drinkId}` |
| Edit meal | `PUT /api/meals/{mealId}` |
| Edit drink | `PUT /api/drinks/{drinkId}` |
| Delete meal | `DELETE /api/meals/{mealId}` |
| Delete drink | `DELETE /api/drinks/{drinkId}` |

---

## 13.7 Groups Screen

| Action | API |
|---|---|
| Load my groups | `GET /api/groups` |
| Create group | `POST /api/groups` |
| Join group | `POST /api/groups/join` |
| Load group detail | `GET /api/groups/{groupId}` |
| Load group members | `GET /api/groups/{groupId}/members` |
| Load group feed | `GET /api/groups/{groupId}/feed` |

---

## 13.8 Recommendation Screen

| Action | API |
|---|---|
| Generate recommendations | `POST /api/recommendations/today` |
| Select recommendation | `POST /api/recommendations/{recommendationItemId}/select` |
| Reject recommendation | `POST /api/recommendations/{recommendationItemId}/reject` |
| Convert to meal record | `POST /api/recommendations/{recommendationItemId}/eaten` |

---

## 13.9 Analytics Screen

| Action | API |
|---|---|
| Weekly analytics | `GET /api/analytics/weekly` |
| Monthly analytics | `GET /api/analytics/monthly` |
| Repetition analytics | `GET /api/analytics/repetition` |
| Spending analytics | `GET /api/analytics/spending` |

---

# 14. Suggested Frontend Data Models

## 14.1 User

```ts
interface User {
  id: number;
  email: string;
  username: string;
  avatarUrl?: string | null;
}
```

---

## 14.2 MealRecord

```ts
interface MealRecord {
  id: number;
  userId: number;
  username: string;
  groupId?: number | null;
  foodName: string;
  restaurantName?: string | null;
  cuisineType?: string | null;
  mealType: MealType;
  price?: number | null;
  rating?: number | null;
  comment?: string | null;
  photoUrl?: string | null;
  wouldEatAgain?: boolean | null;
  privacyLevel: PrivacyLevel;
  consumedAt: string;
  createdAt: string;
}
```

---

## 14.3 DrinkRecord

```ts
interface DrinkRecord {
  id: number;
  userId: number;
  username: string;
  groupId?: number | null;
  drinkName: string;
  shopName?: string | null;
  sweetnessLevel?: SweetnessLevel | null;
  iceLevel?: IceLevel | null;
  price?: number | null;
  rating?: number | null;
  comment?: string | null;
  photoUrl?: string | null;
  wouldBuyAgain?: boolean | null;
  privacyLevel: PrivacyLevel;
  consumedAt: string;
  createdAt: string;
}
```

---

## 14.4 GroupFeedItem

```ts
interface GroupFeedItem {
  recordType: "MEAL" | "DRINK";
  recordId: number;
  groupId: number;
  userId: number;
  username: string;
  avatarUrl?: string | null;
  itemName: string;
  placeName?: string | null;
  cuisineType?: string | null;
  mealType?: MealType | null;
  price?: number | null;
  rating?: number | null;
  comment?: string | null;
  photoUrl?: string | null;
  wouldHaveAgain?: boolean | null;
  consumedAt: string;
  createdAt: string;
}
```

---

## 14.5 RecommendationItem

```ts
interface RecommendationItem {
  id: number;
  rankPosition: number;
  name: string;
  restaurantName?: string | null;
  recommendationType: RecommendationType;
  source: RecommendationSource;
  score: number;
  reason: string;
  selected: boolean;
  rejected: boolean;
}
```

---

# 15. Frontend Error Handling Rules

## 15.1 Token Expired

When any API returns `401`:

1. Clear saved token
2. Clear cached user
3. Navigate to login screen

---

## 15.2 Forbidden

When API returns `403`:

Show:

```text
You do not have permission to perform this action.
```

---

## 15.3 Validation Error

When API returns `400` with field details:

- Show field-level error if the field exists on the current screen
- Otherwise show a toast/snackbar

---

## 15.4 Empty State Handling

The frontend should handle empty states for:

| Screen | Empty State |
|---|---|
| History | `No meals logged yet.` |
| Drinks | `No drinks logged yet.` |
| Groups | `Create or join a group to see friends' food logs.` |
| Group Feed | `No group records yet.` |
| Recommendations | `Not enough data yet. Try logging more meals first.` |
| Analytics | `Log more meals to see your food patterns.` |

---

# 16. MVP API Completion Checklist

The backend and frontend can be considered API-compatible when the following works:

- [ ] User can register
- [ ] User can login
- [ ] Frontend can store and send JWT token
- [ ] User can load current user
- [ ] User can load and update profile
- [ ] User can update cuisine preferences
- [ ] User can update dietary restrictions
- [ ] User can create meal record
- [ ] User can list meal records
- [ ] User can update/delete own meal records
- [ ] User can create drink record
- [ ] User can list drink records
- [ ] User can update/delete own drink records
- [ ] User can create group
- [ ] User can join group by invite code
- [ ] User can view group feed
- [ ] User can request recommendations
- [ ] User can select/reject recommendation
- [ ] User can convert recommendation into meal record
- [ ] User can view weekly analytics

---

# 17. Notes for Backend Developer

The frontend expects:

- Consistent response wrapper
- Stable enum string values
- Stable date format using ISO 8601
- JWT authentication through `Authorization: Bearer <token>`
- Pagination with `content`, `page`, `size`, `totalElements`, `totalPages`, and `last`
- Null-safe optional fields
- Field-level validation details for form errors

Do not expose JPA entities directly. Always return DTOs.

---

# 18. Notes for Frontend Developer

The frontend should:

- Store token securely
- Attach token to all protected APIs
- Treat all datetime strings as ISO 8601
- Handle `null` values safely
- Use enum values exactly as documented
- Avoid hardcoding group IDs
- Load `/api/groups` before showing group-sharing options
- Disable `GROUP` privacy if the user has not joined any group
- Use pagination for history and group feed
