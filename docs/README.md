# FoodMind 项目文档

版本：1.0  
文档日期：2026-06-23  
目标客户端：Android（Kotlin + XML Layout）  
后端：Spring Boot 3 + PostgreSQL

## 1. 文档用途

这套文档用于让产品、Android、后端、测试和课程评审人员对 FoodMind 的目标、页面、业务规则、接口和代码组织方式形成一致理解。

| 文档 | 主要读者 | 解决的问题 |
|---|---|---|
| [01_Product_Requirements.md](./01_Product_Requirements.md) | 全体成员 | 为什么做、为谁做、MVP 做什么 |
| [02_User_Flows_and_Screens.md](./02_User_Flows_and_Screens.md) | 产品、设计、Android、测试 | 用户如何操作、每个页面显示什么 |
| [03_Frontend_Architecture.md](./03_Frontend_Architecture.md) | Android 开发者 | 代码如何分层、文件放在哪里 |
| [04_API_Contract.md](./04_API_Contract.md) | Android、后端、测试 | 前后端如何通信、字段和错误如何处理 |
| [05_Development_and_Test_Plan.md](./05_Development_and_Test_Plan.md) | 开发者、测试、项目负责人 | 按什么顺序开发、怎样判断完成 |

## 2. 文档优先级

发生冲突时按以下规则处理：

1. 产品范围和业务目标以 `01_Product_Requirements.md` 为准。
2. 页面行为和导航以 `02_User_Flows_and_Screens.md` 为准。
3. Android 实现方式以 `03_Frontend_Architecture.md` 为准。
4. 请求、响应、字段和状态码以 `04_API_Contract.md` 为准。
5. 发现冲突时不得自行猜测，应记录问题并同步修改相关文档。

## 3. 已确定的技术决策

- 单 Activity、多 Fragment。
- 所有页面使用 XML Layout，不使用 Jetpack Compose。
- 使用 Navigation Component 管理导航和返回栈。
- 使用 MVVM：Fragment → ViewModel → Repository → Retrofit API。
- 使用 View Binding 访问视图。
- 使用 Kotlin Coroutines 处理异步请求。
- 使用 RecyclerView 展示列表。
- JWT 保存于应用私有存储，并由 OkHttp Interceptor 自动附加。
- Android 模拟器访问电脑后端时使用 `http://10.0.2.2:8080/`。

## 4. 术语

| 术语 | 含义 |
|---|---|
| Meal | 正餐或食物记录 |
| Drink | 饮品记录 |
| Group | 用户通过邀请码加入的私密饮食群组 |
| Group Feed | 群组成员共享的 Meal/Drink 动态 |
| Recommendation | 根据历史、偏好和群组数据生成的饮食推荐 |
| Profile Setup | 首次注册后完成预算、口味和饮食限制设置 |
| MVP | 本课程项目必须完成的最小可用版本 |

## 5. 当前待后端确认事项

以下内容在产品流程中需要，但原始 API 尚未完全定义：

- `/api/auth/me` 是否返回 `profileCompleted`。
- 用户主动退出群组的接口和规则。
- OWNER 如何解散群组或转移所有权。
- 推荐能否同时存在多个 `selected=true`。
- `/recommendations/today` 在数据不足三条时的行为。
- Profile 三个更新接口是否需要事务式提交。

这些事项已在 API 文档中标记为“需后端确认”，Android 开发时不应依赖未经确认的行为。
