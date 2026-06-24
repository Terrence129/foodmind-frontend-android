# FoodMind 用户流程与页面规格

版本：1.0  
日期：2026-06-23

## 1. 导航总览

```text
App Launch
└── Welcome / Simple Splash
    ├── No token ───────────────→ Login
    │                              └── Register → Profile Setup → Home
    └── Has token → /auth/me
                   ├── 401 ─────→ Login
                   ├── profile incomplete → Profile Setup
                   └── profile complete ──→ Home

Main application
├── Home
│   ├── Recommendation
│   ├── Analytics
│   └── Quick Add → Add Meal / Add Drink
├── Log → Add Meal / Add Drink
├── History → Meal Detail / Drink Detail
├── Groups → Group Detail → Group Feed → Record Detail
└── Profile → Edit Profile / Analytics / Logout
```

## 2. 页面清单

| ID | 页面 | Fragment | 级别 |
|---|---|---|---|
| SCR-00 | Welcome / Simple Splash | `WelcomeFragment` | Launch |
| SCR-01 | Login | `LoginFragment` | Auth |
| SCR-02 | Register | `RegisterFragment` | Auth |
| SCR-03 | Profile Setup | `ProfileSetupFragment` | Auth/Onboarding |
| SCR-04 | Home | `HomeFragment` | Bottom navigation |
| SCR-05 | Log | `LogFragment` | Bottom navigation |
| SCR-06 | Add Meal | `AddMealFragment` | Secondary |
| SCR-07 | Add Drink | `AddDrinkFragment` | Secondary |
| SCR-08 | History | `HistoryFragment` | Bottom navigation |
| SCR-09 | Meal Detail | `MealDetailFragment` | Secondary |
| SCR-10 | Drink Detail | `DrinkDetailFragment` | Secondary |
| SCR-11 | Groups | `GroupsFragment` | Bottom navigation |
| SCR-12 | Group Detail | `GroupDetailFragment` | Secondary |
| SCR-13 | Group Feed | `GroupFeedFragment` | Secondary |
| SCR-14 | Recommendation | `RecommendationFragment` | Secondary |
| SCR-15 | Analytics | `AnalyticsFragment` | Secondary |
| SCR-16 | Profile | `ProfileFragment` | Bottom navigation |

## 3. 全局导航规则

- Auth 页面不显示 Toolbar 的返回按钮，Register 除外。
- 登录成功后清空 Auth 返回栈，系统返回键不能回到 Login。
- Profile Setup 完成后清空 Onboarding 返回栈。
- Bottom Navigation 切换一级页面时保留各自状态。
- Detail 页面使用 Toolbar 返回上一个页面。
- Fragment 之间只传 ID、来源页面等轻量参数，不传完整 DTO。
- 发生统一 401 时跳转 Login，并清空所有受保护页面返回栈。

## 4. SCR-00 Welcome / Simple Splash

### 页面目的

用户点击 App 后先看到简洁开屏，只展示 FoodMind 图标和欢迎文字，避免在正式登录或首页流程完成前承载额外表单。

### UI 元素

- FoodMind App 图标，进入页面时执行弹出动画。
- 一行欢迎文字：`欢迎使用 FoodMind`。

### 用户操作

| 操作 | 结果 |
|---|---|
| 点击 App 图标 | 显示轻提示：`下一步即将接入` |

### 后续导航

当前阶段不做真实跳转。后续接入真实 Auth / Home 流程时，应根据 Session 状态进入 Login、Profile Setup 或 Home。

## 5. SCR-01 Login

### 页面目的

让已有用户登录，并提供注册入口。

### UI 元素

- FoodMind 标题或 Logo。
- Email 输入框。
- Password 输入框，可切换显示/隐藏。
- Login 按钮。
- “Create account”入口。
- 页面级错误文本或 Snackbar。
- 提交中的 ProgressIndicator。

### 用户操作

| 操作 | 结果 |
|---|---|
| 点击 Login | 校验输入后调用登录接口 |
| 点击 Create account | 导航至 Register |
| 键盘 Done | 等价于点击 Login |

### API

- `POST /api/auth/login`

### 验证

- Email 不能为空且格式有效。
- Password 不能为空。

### 状态

- Submitting：禁用按钮和输入框，避免重复提交。
- Field Error：显示在 TextInputLayout。
- Authentication Error：显示“Email or password is incorrect.”
- Network Error：显示 Retry 或 Snackbar。

### 成功导航

- `profileCompleted=true` → Home。
- `profileCompleted=false` → Profile Setup。

## 6. SCR-02 Register

### UI 元素

- Username。
- Email。
- Password。
- Confirm Password。
- Register 按钮。
- 返回 Login。

### API

- `POST /api/auth/register`

### 验证

- Username 2～80 字符。
- Email 合法。
- Password 至少 8 字符。
- Confirm Password 必须一致。

### 成功导航

保存 Token，清除 Register/Login 返回栈，进入 Profile Setup。

## 7. SCR-03 Profile Setup

### 页面目的

收集推荐所需的首批偏好。

### 推荐布局

一个可滚动表单，分成三部分：

1. Budget and location。
2. Food goal and privacy。
3. Cuisine and dietary restrictions。

### API

- `GET /api/profile/me`
- `PUT /api/profile/me`
- `PUT /api/profile/cuisine-preferences`
- `PUT /api/profile/dietary-restrictions`

### 提交流程

```text
Validate local form
→ Update profile
→ Update cuisine preferences
→ Update dietary restrictions
→ All successful → Home
```

若后端无法提供事务接口：

- 任一请求失败时保留表单。
- 明确提示哪一部分保存失败。
- 再次提交应安全覆盖已有值。

### 验证

- Budget 必须为非负数。
- `budgetMin <= budgetMax`。
- Spicy tolerance 为 0～5。
- 同一 Cuisine 不得同时被 Like 和 Dislike。

## 8. SCR-04 Home

### 页面目的

提供用户当前状态摘要和最常用功能入口。

### UI 模块

- Toolbar：用户名或问候。
- Weekly summary：Meal 数、Drink 数、Spending。
- “What should I eat?”推荐入口。
- Quick actions：Add Meal、Add Drink。
- Recent records：最多 3～5 条。
- Analytics 入口。

### API

- `GET /api/auth/me`
- `GET /api/analytics/weekly`
- 可选：`GET /api/meals?size=3`
- 可选：`GET /api/drinks?size=3`

### 降级规则

- Analytics 失败不影响 Quick Actions。
- Recent records 失败不阻止推荐入口。
- 每个模块可独立显示错误或空状态。

## 9. SCR-05 Log

### 页面目的

作为快速记录入口，不承载完整大表单。

### UI 元素

- Add Meal 卡片。
- Add Drink 卡片。
- 简短说明。

### 导航

- Add Meal → SCR-06。
- Add Drink → SCR-07。

## 10. SCR-06 Add Meal

### UI 字段

| 字段 | 控件 | 必填 |
|---|---|---|
| Food name | TextInputEditText | 是 |
| Restaurant | TextInputEditText | 否 |
| Cuisine | AutoCompleteTextView | 否 |
| Meal type | Dropdown | 是 |
| Price | Decimal input | 否 |
| Rating | RatingBar/Slider | 否 |
| Comment | Multiline input | 否 |
| Would eat again | Switch | 否 |
| Privacy | Dropdown/Radio | 是 |
| Group | Dropdown | GROUP 时必填 |
| Consumed at | Date + Time picker | 是 |

### API

- `GET /api/groups`
- `POST /api/meals`
- 编辑模式额外使用 `GET/PUT /api/meals/{id}`

### 行为

- 默认 `consumedAt` 为当前时间。
- 默认 Privacy 来自 Profile。
- Privacy 为 GROUP 时显示 Group 选择器。
- 没有 Group 时禁用 GROUP。
- 创建成功后返回 History。
- 从 Detail 进入编辑模式时，标题和按钮文案变为 Edit/Save。

### 未保存退出

表单有修改时按返回键，显示“Discard changes?”确认对话框。

## 11. SCR-07 Add Drink

行为与 Add Meal 相同，字段替换为：

- Drink name。
- Shop。
- Sweetness。
- Ice。
- Would buy again。

API：

- `GET /api/groups`
- `POST /api/drinks`
- 编辑模式使用 `GET/PUT /api/drinks/{id}`

## 12. SCR-08 History

### 页面目的

展示用户自己的 Meal 和 Drink。

### UI 元素

- Meal/Drink Tab。
- Search 输入。
- Filter 按钮。
- SwipeRefreshLayout。
- RecyclerView。
- Loading、Empty、Error。

### API

- `GET /api/meals`
- `GET /api/drinks`

### 行为

- 默认第一页 `page=0&size=20`。
- 默认按最新记录显示。
- 搜索建议在输入停止 300～500ms 后执行。
- 新筛选条件会清空旧列表并从第 0 页加载。
- `last=true` 后停止分页。
- 点击 Meal → Meal Detail。
- 点击 Drink → Drink Detail。

### Empty State

- Meal：`No meals logged yet.`
- Drink：`No drinks logged yet.`
- 提供 Add 按钮。

## 13. SCR-09 Meal Detail

### 页面目的

显示完整 Meal 信息并提供编辑、删除操作。

### 导航参数

- `mealId: Long`
- `source: String?`

### API

- `GET /api/meals/{mealId}`
- `DELETE /api/meals/{mealId}`

### 操作

- Edit → AddMealFragment 编辑模式。
- Delete → 确认对话框 → 删除 → 返回并刷新来源列表。
- 非所有者从 Group Feed 进入时隐藏 Edit/Delete。

### 错误

- 403：显示权限错误并返回。
- 404：显示记录不存在并允许返回。

## 14. SCR-10 Drink Detail

与 Meal Detail 相同，使用 Drink API 和 `drinkId`。

## 15. SCR-11 Groups

### 页面目的

显示用户群组，并提供创建和加入入口。

### UI 元素

- RecyclerView。
- Create Group 按钮。
- Join Group 按钮。
- Empty State。
- Create/Join Dialog。

### API

- `GET /api/groups`
- `POST /api/groups`
- `POST /api/groups/join`

### 创建成功

- 显示群组名和 Invite Code。
- 提供 Copy 操作。
- 刷新列表。

### 加入成功

- 关闭对话框。
- 刷新列表。
- 可直接进入 Group Detail。

## 16. SCR-12 Group Detail

### 导航参数

- `groupId: Long`

### UI 模块

- Group 名称和描述。
- Invite Code（根据权限显示）。
- 当前用户角色。
- 成员列表。
- Open Feed 按钮。
- OWNER/ADMIN 的成员管理操作。

### API

- `GET /api/groups/{groupId}`
- `GET /api/groups/{groupId}/members`
- `DELETE /api/groups/{groupId}/members/{userId}`

### 权限显示

| Role | 查看 | 邀请码 | 移除 MEMBER |
|---|---:|---:|---:|
| OWNER | 是 | 是 | 是 |
| ADMIN | 是 | 是 | 是，具体范围需后端确认 |
| MEMBER | 是 | 产品决定，建议是 | 否 |

## 17. SCR-13 Group Feed

### UI 元素

- Record Type 筛选。
- Rating/Keyword 等筛选。
- 混合 RecyclerView。
- Meal 和 Drink 使用不同视觉标签。

### API

- `GET /api/groups/{groupId}/feed`

### 点击行为

- `recordType=MEAL` → Meal Detail。
- `recordType=DRINK` → Drink Detail。

### Empty State

`No group records yet. Share a meal or drink with this group.`

## 18. SCR-14 Recommendation

### 页面阶段

#### Input

- Meal Type。
- Budget Max。
- Group（可选）。
- Mood。
- Avoid Items。
- Location。

#### Loading

展示生成中的明确状态。

#### Result

最多三张 Recommendation Card：

- Rank。
- Name。
- Restaurant。
- Type Label。
- Reason。
- Score（可选展示）。
- Select、Reject。

### API

- `POST /api/recommendations/today`
- `POST /api/recommendations/{id}/select`
- `POST /api/recommendations/{id}/reject`
- `POST /api/recommendations/{id}/eaten`

### 状态转换

```text
Default
├── Select → Selected → Log this meal / I ate this
└── Reject → Rejected (disabled or visually muted)

Selected
└── Eaten → Meal created → Meal Detail
```

具体互斥规则以 API 文档“需后端确认”项为准。

## 19. SCR-15 Analytics

### UI

- Weekly、Monthly、Repetition、Spending Tab。
- 日期或月份选择。
- 摘要卡片。
- Top foods/cuisines 列表。
- Daily spending 列表或简单图表。

### API

- `GET /api/analytics/weekly`
- `GET /api/analytics/monthly`
- `GET /api/analytics/repetition`
- `GET /api/analytics/spending`

### 空值

- 数值无数据：`0` 或 `—`，按字段语义决定。
- 文本无数据：`Not enough data`。
- 不允许直接显示 `null`。

## 20. SCR-16 Profile

### UI

- Username 和 Email。
- Budget、location、goal 摘要。
- Cuisine preferences。
- Dietary restrictions。
- Edit Profile。
- Analytics 入口。
- Logout。

### API

- `GET /api/auth/me`
- `GET /api/profile/me`
- 修改时使用 Profile PUT 接口。

### Logout

- 显示确认。
- 清除 Token、本地 User 和页面状态。
- 导航 Login 并清空返回栈。

## 21. 通用错误文案

| 场景 | 建议文案 |
|---|---|
| 无网络 | `No internet connection. Check your network and try again.` |
| 超时 | `The request took too long. Please try again.` |
| 401 | `Your session has expired. Please log in again.` |
| 403 | `You do not have permission to perform this action.` |
| 404 | `This item no longer exists.` |
| 409 | 使用后端 message，例如邮箱或群组冲突 |
| 500 | `Something went wrong on the server. Please try again later.` |

## 22. 页面完成检查

每个 Fragment 完成前检查：

- 有正常布局和小屏滚动处理。
- 有 Loading、Content、Empty、Error。
- 有输入验证。
- 支持旋转后的 ViewModel 状态恢复。
- 不重复观察 LiveData/StateFlow。
- 页面销毁时清空 Binding。
- 导航不会重复触发。
- API 错误不会导致崩溃。
