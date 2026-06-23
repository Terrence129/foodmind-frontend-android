# FoodMind 开发与测试计划

版本：1.0  
日期：2026-06-23

## 1. 开发原则

先完成一条可运行的纵向业务链路，再扩展其他功能。不要先创建几十个空 Fragment 和 DTO。

推荐第一条链路：

```text
Login
→ Save token
→ Create Meal
→ History displays Meal
→ Open Meal Detail
→ Edit/Delete Meal
```

这条链路会验证 Navigation、Retrofit、JWT、Repository、ViewModel、RecyclerView、表单和错误处理。

## 2. 优先级

### P0：必须完成

- Login/Register。
- Session restore。
- Profile Setup。
- Add Meal/Drink。
- Meal/Drink History。
- Detail/Edit/Delete。
- Groups list/create/join。
- Group Feed。
- Recommendations。
- Weekly Analytics。
- 全局错误和 401。

### P1：应完成

- Monthly/Repetition/Spending Analytics。
- Search 和基础筛选。
- Group member 管理。
- Recommendation history。
- 更完整的 Home 摘要。

### P2：时间允许

- 图表。
- 高级筛选。
- 动画。
- EncryptedSharedPreferences。
- 更多 UI polish。

## 3. Milestone

### M1 项目骨架

交付：

- Gradle 依赖。
- MainActivity。
- NavHost。
- Bottom Navigation。
- View Binding。
- Retrofit/OkHttp。
- ApiResponse/NetworkResult。
- TokenManager。

验收：

- App 可构建运行。
- Login 空页面可导航到 Register。
- Health API 可从开发环境调用。

### M2 Authentication 和 Profile

交付：

- Login/Register。
- Token 保存。
- `/auth/me` 恢复会话。
- Profile Setup。
- 统一 401。

验收：

- 新用户完整进入 Home。
- 已登录用户重新启动后进入正确页面。
- 过期 Token 返回 Login。

### M3 Records

交付：

- Add Meal。
- Add Drink。
- History。
- Detail/Edit/Delete。
- 分页基础实现。

验收：

- CRUD 与真实后端一致。
- GROUP 隐私要求选择 Group。
- 删除成功后列表同步。

### M4 Groups

交付：

- Group list/create/join。
- Group detail/members。
- Group Feed。

验收：

- Invite Code 可加入。
- 只有 GROUP 记录出现在 Feed。
- Meal/Drink 点击进入正确 Detail。

### M5 Recommendation 和 Analytics

交付：

- Recommendation input/results。
- Select/Reject/Eaten。
- Weekly Analytics。
- 其他 Analytics（P1）。

验收：

- 推荐理由正确显示。
- Eaten 后创建 Meal。
- 新用户无数据时不崩溃。

### M6 稳定与交付

交付：

- 错误状态。
- Empty State。
- UI 统一。
- 测试。
- Demo 数据和演示脚本。

## 4. 测试策略

### 4.1 ViewModel 单元测试

重点测试：

- 输入验证。
- Loading → Success/Error 状态。
- 400 field error 映射。
- 401 SessionExpired。
- 分页合并和 `last`。
- 推荐状态更新。

### 4.2 Repository 测试

使用 Fake API 或 MockWebServer：

- 成功响应解析。
- 400/401/403/404/409/500。
- 无网络和超时。
- `data=null` 的异常情况。

### 4.3 UI/手工测试

每个页面检查：

- 小屏是否可滚动。
- 键盘是否遮住按钮。
- 重复点击是否重复提交。
- 返回键和返回栈。
- Loading、Empty、Error。
- 旋转后是否重复请求或重复导航。

## 5. 核心验收用例

### TC-AUTH-01 注册成功

前置：邮箱未注册。  
步骤：填写合法 Username、Email、Password 并提交。  
预期：

- 返回 Token。
- Token 被保存。
- 进入 Profile Setup。
- 返回键不回到 Register。

### TC-AUTH-02 Token 过期

前置：本地保存无效 Token。  
步骤：启动 App。  
预期：

- `/auth/me` 返回 401。
- Token 清除。
- 进入 Login。
- 只显示一次 Session expired。

### TC-PROFILE-01 Budget 非法

输入：Min=20、Max=10。  
预期：本地阻止提交并显示字段关系错误。

### TC-MEAL-01 创建 PRIVATE Meal

预期：

- `groupId=null`。
- 创建成功进入 History。
- 新记录显示在列表顶部。

### TC-MEAL-02 创建 GROUP Meal

预期：

- 必须选择 Group。
- 请求携带 groupId。
- 记录出现在个人 History 和对应 Group Feed。

### TC-HISTORY-01 分页

前置：Meal 超过 20 条。  
预期：

- 首次加载 20 条。
- 滚动后加载下一页。
- 不重复数据。
- `last=true` 后不继续请求。

### TC-GROUP-01 无效邀请码

预期：显示后端提供的无效邀请码错误，对话框保持打开。

### TC-REC-01 推荐数据不足

预期：显示推荐 Empty State，不显示通用服务器错误。

### TC-REC-02 Mark Eaten

预期：

- 创建 Meal。
- 返回 `mealRecordId`。
- 进入 Meal Detail。
- History 可看到记录。

### TC-ANALYTICS-01 新用户

前置：无 Meal/Drink。  
预期：

- Count 为 0。
- Null 文本显示为 `—` 或 Empty State。
- 不崩溃。

## 6. API 联调清单

- [ ] Emulator 使用 `10.0.2.2`。
- [ ] Manifest 有 INTERNET permission。
- [ ] 本地 HTTP 配置正确。
- [ ] Login/Register 不带无效 Token。
- [ ] Protected API 带 Bearer Token。
- [ ] 请求和响应字段大小写一致。
- [ ] Enum 使用 API 原值。
- [ ] ISO 时间包含 Offset。
- [ ] Page 从 0 开始。
- [ ] Empty list 是 `[]`。
- [ ] Nullable 字段解析安全。
- [ ] 401 会清除 Session。

## 7. Definition of Done

一个 Feature 只有满足以下条件才算完成：

- 页面按规格实现。
- 真实 API 已联调，不只使用假数据。
- Loading/Content/Empty/Error 齐全。
- 输入验证齐全。
- 主要成功和失败路径已测试。
- 无硬编码 Token、User ID、Group ID。
- 用户可见文本在 `strings.xml`。
- View Binding 正确释放。
- 返回栈符合页面文档。
- 相关文档和接口变更已同步。

## 8. 演示脚本

建议课程演示按以下顺序：

1. 注册新账号。
2. 设置预算、地区和喜欢的菜系。
3. 添加一条 PRIVATE Meal。
4. 在 History 查看并编辑记录。
5. 创建 Group 并展示 Invite Code。
6. 添加一条 GROUP Drink。
7. 在 Group Feed 查看共享记录。
8. 请求 Recommendation 并展示解释理由。
9. 选择推荐并 Mark as Eaten。
10. 打开 Weekly Analytics。

演示前准备：

- 后端和数据库可用。
- 至少一个已有账号和一个群组。
- 准备足够历史数据以生成推荐和 Analytics。
- 准备无网络或错误场景的说明，但不要让演示依赖临时手工修复。
