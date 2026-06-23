# FoodMind Android 前端架构

版本：1.0  
日期：2026-06-23  
技术约束：Kotlin + XML Layout

## 1. 架构目标

本架构服务于课程项目和 MVP，优先考虑：

- 开发者容易理解。
- 页面职责清楚。
- API 映射直接。
- 可以逐功能完成和测试。
- 不为了“看起来高级”引入不必要复杂度。

采用：

```text
Single Activity
    ↓
Navigation Component
    ↓
Fragments + XML Layout
    ↓
ViewModel + UiState
    ↓
Repository
    ↓
Retrofit API
    ↓
Spring Boot backend
```

## 2. 技术栈

| 范围 | 技术 |
|---|---|
| Language | Kotlin |
| UI | XML Layout、Material Components |
| Navigation | Jetpack Navigation Component |
| Architecture | MVVM |
| State | StateFlow（或课程要求下使用 LiveData） |
| Async | Kotlin Coroutines |
| HTTP | Retrofit + OkHttp |
| JSON | Gson 或 Moshi，项目内只选一种 |
| Lists | RecyclerView + ListAdapter + DiffUtil |
| View access | View Binding |
| Local session | SharedPreferences；可选 EncryptedSharedPreferences |
| Image | MVP 暂无上传；远程 URL 可使用 Coil/Glide |

## 3. 模块职责

### 3.1 Activity

`MainActivity` 只负责：

- 承载 NavHostFragment。
- 管理全局 Toolbar。
- 管理 BottomNavigationView。
- 根据当前 Destination 显示或隐藏全局导航。
- 响应全局 SessionExpired 事件。

Activity 不应：

- 直接调用业务 API。
- 保存具体页面表单数据。
- 包含 Meal、Group 等业务逻辑。

### 3.2 Fragment

Fragment 负责：

- Inflate/绑定 XML。
- 接收用户输入。
- 观察 UiState 和 UiEvent。
- 渲染 Loading、Content、Empty、Error。
- 调用 ViewModel 的公开方法。
- 执行页面导航。

Fragment 不应：

- 直接创建 Retrofit。
- 直接调用 Api。
- 手动启动线程。
- 实现复杂业务校验。
- 长期保存服务器数据。

### 3.3 ViewModel

ViewModel 负责：

- 页面状态。
- 输入验证。
- 调用 Repository。
- 将网络结果转换为 UiState。
- 发出一次性 UiEvent，如导航、Snackbar。

建议模式：

```kotlin
data class HistoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val meals: List<MealRecordDto> = emptyList(),
    val drinks: List<DrinkRecordDto> = emptyList(),
    val selectedTab: HistoryTab = HistoryTab.MEAL,
    val error: UiText? = null
)
```

### 3.4 Repository

Repository 负责：

- 组合一个 Feature 所需的 API 调用。
- 将 Retrofit 异常转换为 `NetworkResult`。
- 隐藏网络实现细节。
- 必要时处理多接口顺序调用。

Repository 不应持有 Fragment、Activity 或 View。

### 3.5 API interface

Retrofit interface 只定义 HTTP 契约：

- Method。
- Path。
- Query。
- Body。
- Response DTO。

不放 UI 文案或导航逻辑。

### 3.6 DTO

DTO 与后端 JSON 一一对应。

- 请求对象以 `Request` 结尾。
- API 返回对象以 `Dto` 或 `Response` 结尾。
- ID 使用 `Long`。
- 金额建议使用 `BigDecimal`；若课程范围有限，可使用 `Double`，但只在显示层格式化。
- 可空字段必须声明为 nullable。

## 4. 项目根目录结构

```text
FoodMind/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       ├── test/
│       │   └── java/com/foodmind/app/
│       │       ├── core/network/NetworkErrorHandlerTest.kt
│       │       └── feature/
│       │           ├── auth/AuthViewModelTest.kt
│       │           ├── records/HistoryViewModelTest.kt
│       │           └── recommendation/RecommendationViewModelTest.kt
│       └── androidTest/
│           └── java/com/foodmind/app/
│               ├── LoginFlowTest.kt
│               └── RecordFlowTest.kt
├── docs/
│   ├── README.md
│   ├── 01_Product_Requirements.md
│   ├── 02_User_Flows_and_Screens.md
│   ├── 03_Frontend_Architecture.md
│   ├── 04_API_Contract.md
│   └── 05_Development_and_Test_Plan.md
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
└── README.md
```

注意：

- `local.properties` 不提交版本控制。
- 密钥、Token、真实生产地址不得写入 Git。
- 根目录 `README.md` 用于项目启动说明；`docs/README.md` 是产品与技术文档索引。

## 5. `app/src/main` 完整文件结构

```text
app/src/main/
├── AndroidManifest.xml
│
├── java/com/foodmind/app/
│   ├── FoodMindApplication.kt
│   ├── MainActivity.kt
│   │
│   ├── core/
│   │   ├── network/
│   │   │   ├── ApiClient.kt
│   │   │   ├── AuthInterceptor.kt
│   │   │   ├── ApiResponse.kt
│   │   │   ├── ApiError.kt
│   │   │   ├── FieldError.kt
│   │   │   ├── PageResponse.kt
│   │   │   ├── NetworkResult.kt
│   │   │   └── NetworkErrorHandler.kt
│   │   │
│   │   ├── session/
│   │   │   ├── SessionManager.kt
│   │   │   └── SessionEvent.kt
│   │   │
│   │   ├── storage/
│   │   │   └── TokenManager.kt
│   │   │
│   │   ├── model/
│   │   │   ├── MealType.kt
│   │   │   ├── PrivacyLevel.kt
│   │   │   ├── FoodGoal.kt
│   │   │   ├── GroupRole.kt
│   │   │   ├── GroupMemberStatus.kt
│   │   │   ├── SweetnessLevel.kt
│   │   │   ├── IceLevel.kt
│   │   │   ├── RecommendationType.kt
│   │   │   └── RecommendationSource.kt
│   │   │
│   │   ├── ui/
│   │   │   ├── UiText.kt
│   │   │   ├── UiEvent.kt
│   │   │   └── BaseListAdapter.kt
│   │   │
│   │   └── util/
│   │       ├── DateUtils.kt
│   │       ├── FormatUtils.kt
│   │       ├── ValidationUtils.kt
│   │       └── ViewExtensions.kt
│   │
│   ├── feature/
│   │   ├── auth/
│   │   │   ├── data/
│   │   │   │   ├── AuthApi.kt
│   │   │   │   ├── AuthRepository.kt
│   │   │   │   └── dto/
│   │   │   │       ├── LoginRequest.kt
│   │   │   │       ├── RegisterRequest.kt
│   │   │   │       ├── AuthResponse.kt
│   │   │   │       └── UserDto.kt
│   │   │   └── ui/
│   │   │       ├── LoginFragment.kt
│   │   │       ├── RegisterFragment.kt
│   │   │       ├── AuthViewModel.kt
│   │   │       ├── AuthUiState.kt
│   │   │       └── AuthUiEvent.kt
│   │   │
│   │   ├── profile/
│   │   │   ├── data/
│   │   │   │   ├── ProfileApi.kt
│   │   │   │   ├── ProfileRepository.kt
│   │   │   │   └── dto/
│   │   │   │       ├── ProfileDto.kt
│   │   │   │       ├── UpdateProfileRequest.kt
│   │   │   │       ├── CuisinePreferenceRequest.kt
│   │   │   │       └── DietaryRestrictionRequest.kt
│   │   │   └── ui/
│   │   │       ├── ProfileSetupFragment.kt
│   │   │       ├── ProfileFragment.kt
│   │   │       ├── ProfileViewModel.kt
│   │   │       ├── ProfileUiState.kt
│   │   │       └── ProfileUiEvent.kt
│   │   │
│   │   ├── home/
│   │   │   └── ui/
│   │   │       ├── HomeFragment.kt
│   │   │       ├── HomeViewModel.kt
│   │   │       └── HomeUiState.kt
│   │   │
│   │   ├── records/
│   │   │   ├── meal/
│   │   │   │   ├── data/
│   │   │   │   │   ├── MealApi.kt
│   │   │   │   │   ├── MealRepository.kt
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── MealRecordDto.kt
│   │   │   │   │       ├── CreateMealRequest.kt
│   │   │   │   │       └── UpdateMealRequest.kt
│   │   │   │   └── ui/
│   │   │   │       ├── AddMealFragment.kt
│   │   │   │       ├── MealDetailFragment.kt
│   │   │   │       ├── MealAdapter.kt
│   │   │   │       ├── MealFormState.kt
│   │   │   │       ├── MealDetailViewModel.kt
│   │   │   │       └── MealDetailUiState.kt
│   │   │   │
│   │   │   ├── drink/
│   │   │   │   ├── data/
│   │   │   │   │   ├── DrinkApi.kt
│   │   │   │   │   ├── DrinkRepository.kt
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── DrinkRecordDto.kt
│   │   │   │   │       ├── CreateDrinkRequest.kt
│   │   │   │   │       └── UpdateDrinkRequest.kt
│   │   │   │   └── ui/
│   │   │   │       ├── AddDrinkFragment.kt
│   │   │   │       ├── DrinkDetailFragment.kt
│   │   │   │       ├── DrinkAdapter.kt
│   │   │   │       ├── DrinkFormState.kt
│   │   │   │       ├── DrinkDetailViewModel.kt
│   │   │   │       └── DrinkDetailUiState.kt
│   │   │   │
│   │   │   └── ui/
│   │   │       ├── LogFragment.kt
│   │   │       ├── HistoryFragment.kt
│   │   │       ├── LogViewModel.kt
│   │   │       ├── LogUiState.kt
│   │   │       ├── HistoryViewModel.kt
│   │   │       └── HistoryUiState.kt
│   │   │
│   │   ├── group/
│   │   │   ├── data/
│   │   │   │   ├── GroupApi.kt
│   │   │   │   ├── GroupRepository.kt
│   │   │   │   └── dto/
│   │   │   │       ├── GroupDto.kt
│   │   │   │       ├── GroupMemberDto.kt
│   │   │   │       ├── GroupFeedItemDto.kt
│   │   │   │       ├── CreateGroupRequest.kt
│   │   │   │       ├── CreateGroupResponse.kt
│   │   │   │       ├── JoinGroupRequest.kt
│   │   │   │       └── JoinGroupResponse.kt
│   │   │   └── ui/
│   │   │       ├── GroupsFragment.kt
│   │   │       ├── GroupDetailFragment.kt
│   │   │       ├── GroupFeedFragment.kt
│   │   │       ├── GroupViewModel.kt
│   │   │       ├── GroupUiState.kt
│   │   │       ├── GroupUiEvent.kt
│   │   │       └── adapter/
│   │   │           ├── GroupAdapter.kt
│   │   │           ├── GroupFeedAdapter.kt
│   │   │           └── GroupMemberAdapter.kt
│   │   │
│   │   ├── recommendation/
│   │   │   ├── data/
│   │   │   │   ├── RecommendationApi.kt
│   │   │   │   ├── RecommendationRepository.kt
│   │   │   │   └── dto/
│   │   │   │       ├── RecommendationRequest.kt
│   │   │   │       ├── RecommendationResponse.kt
│   │   │   │       ├── RecommendationItemDto.kt
│   │   │   │       ├── RecommendationHistoryDto.kt
│   │   │   │       ├── RejectRecommendationRequest.kt
│   │   │   │       ├── MarkRecommendationEatenRequest.kt
│   │   │   │       └── MarkRecommendationEatenResponse.kt
│   │   │   └── ui/
│   │   │       ├── RecommendationFragment.kt
│   │   │       ├── RecommendationViewModel.kt
│   │   │       ├── RecommendationUiState.kt
│   │   │       └── RecommendationAdapter.kt
│   │   │
│   │   └── analytics/
│   │       ├── data/
│   │       │   ├── AnalyticsApi.kt
│   │       │   ├── AnalyticsRepository.kt
│   │       │   └── dto/
│   │       │       ├── WeeklyAnalyticsDto.kt
│   │       │       ├── MonthlyAnalyticsDto.kt
│   │       │       ├── RepetitionAnalyticsDto.kt
│   │       │       ├── SpendingAnalyticsDto.kt
│   │       │       ├── TopItemDto.kt
│   │       │       ├── RepeatedFoodDto.kt
│   │       │       ├── RepeatedCuisineDto.kt
│   │       │       └── DailySpendingDto.kt
│   │       └── ui/
│   │           ├── AnalyticsFragment.kt
│   │           ├── AnalyticsViewModel.kt
│   │           └── AnalyticsUiState.kt
│   │
│   └── navigation/
│       └── NavigationExtensions.kt
│
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── fragment_login.xml
    │   ├── fragment_register.xml
    │   ├── fragment_profile_setup.xml
    │   ├── fragment_profile.xml
    │   ├── fragment_home.xml
    │   ├── fragment_log.xml
    │   ├── fragment_add_meal.xml
    │   ├── fragment_add_drink.xml
    │   ├── fragment_history.xml
    │   ├── fragment_meal_detail.xml
    │   ├── fragment_drink_detail.xml
    │   ├── fragment_groups.xml
    │   ├── fragment_group_detail.xml
    │   ├── fragment_group_feed.xml
    │   ├── fragment_recommendation.xml
    │   ├── fragment_analytics.xml
    │   ├── item_meal.xml
    │   ├── item_drink.xml
    │   ├── item_group.xml
    │   ├── item_group_feed.xml
    │   ├── item_group_member.xml
    │   ├── item_recommendation.xml
    │   ├── item_top_analytics.xml
    │   ├── item_daily_spending.xml
    │   ├── dialog_create_group.xml
    │   ├── dialog_join_group.xml
    │   ├── dialog_reject_recommendation.xml
    │   ├── view_loading.xml
    │   ├── view_empty_state.xml
    │   └── view_error_state.xml
    │
    ├── drawable/
    ├── mipmap/
    ├── menu/
    │   ├── menu_bottom_navigation.xml
    │   ├── menu_history_filter.xml
    │   └── menu_detail.xml
    ├── navigation/
    │   └── nav_graph.xml
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   ├── themes.xml
    │   ├── dimens.xml
    │   ├── arrays.xml
    │   └── styles.xml
    └── xml/
        └── network_security_config.xml
```

## 6. 为什么这样拆分

### 6.1 Enum 分文件

比把所有 Enum 放进 `Enums.kt` 更容易：

- 搜索。
- 单独扩展 UI Label。
- 使用 Gson/Moshi 适配。
- 避免一个文件不断膨胀。

课程项目规模较小时，合并成 `Enums.kt` 也可接受，但必须保持 API 值不变。

### 6.2 Add 与 Detail 分开

- Add 页面是表单状态。
- Detail 页面是服务器读取状态。
- 编辑可以复用 Add 页面，通过可选 ID 切换模式。
- 避免一个 Fragment 同时承担查看、创建、编辑三种复杂状态。

### 6.3 Feature 内部再分 data/ui

阅读一个功能时，开发者只需进入对应 Feature：

```text
feature/group/
├── data    后端通信和 DTO
└── ui      页面、状态和 Adapter
```

不需要在全局 `api/`、`repository/`、`fragment/` 目录之间来回寻找。

## 7. 网络层设计

### 7.1 通用响应

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val errorCode: String? = null,
    val details: List<FieldError>? = null
)

data class FieldError(
    val field: String,
    val message: String
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)
```

### 7.2 统一结果

```kotlin
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T, val message: String) : NetworkResult<T>
    data class ValidationError(
        val message: String,
        val fields: Map<String, String>
    ) : NetworkResult<Nothing>
    data object Unauthorized : NetworkResult<Nothing>
    data class Forbidden(val message: String) : NetworkResult<Nothing>
    data class NotFound(val message: String) : NetworkResult<Nothing>
    data class Conflict(val message: String) : NetworkResult<Nothing>
    data class NetworkError(val message: String) : NetworkResult<Nothing>
    data class ServerError(val message: String) : NetworkResult<Nothing>
}
```

Repository 应调用一个统一的 `safeApiCall`，避免每个 Repository 重复 try/catch。

### 7.3 Base URL

```kotlin
object ApiClient {
    const val BASE_URL = "http://10.0.2.2:8080/"
}
```

说明：

- Android Emulator → `10.0.2.2`
- Genymotion → 通常为 `10.0.3.2`
- 真机 → 电脑局域网 IP
- `localhost` 在手机中不是开发电脑

推荐把 URL 放在 `BuildConfig`，便于切换环境：

```gradle
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
```

## 8. Session 和 401

`AuthInterceptor` 只负责添加 Header：

```text
Authorization: Bearer <token>
```

统一 401 流程：

```text
API returns 401
→ NetworkErrorHandler emits SessionExpired
→ SessionManager clears token
→ MainActivity receives event
→ navigate to Login and clear back stack
```

注意：

- Interceptor 不直接操作 NavController。
- 多个并发请求都返回 401 时，只导航一次。
- Login/Register 请求不添加空 Token。

## 9. 状态与事件

### UiState

用于可重复渲染的状态：

- Loading。
- 表单值。
- 列表数据。
- Empty。
- Field errors。

### UiEvent

用于只消费一次的行为：

- Navigate。
- ShowSnackbar。
- CloseDialog。
- CopyInviteCode。

不要把 Snackbar 文案长期保存在 State 中，否则旋转屏幕后可能重复显示。

## 10. Fragment Binding 模式

```kotlin
private var _binding: FragmentHistoryBinding? = null
private val binding get() = _binding!!

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    _binding = FragmentHistoryBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

观察 Flow 时使用 `viewLifecycleOwner` 的生命周期，防止 View 销毁后继续更新 UI。

## 11. Navigation Graph 设计

建议一个 `nav_graph.xml` 足够，不需要自定义 `AppNavigator`。

主要参数：

| Destination | Argument |
|---|---|
| AddMealFragment | `mealId: Long = -1` |
| AddDrinkFragment | `drinkId: Long = -1` |
| MealDetailFragment | `mealId: Long` |
| DrinkDetailFragment | `drinkId: Long` |
| GroupDetailFragment | `groupId: Long` |
| GroupFeedFragment | `groupId: Long` |
| RecommendationFragment | `prefillMealType: String?` |

推荐启用 Safe Args，避免手写 Bundle Key。

## 12. RecyclerView 设计

- 使用 `ListAdapter`。
- 使用稳定唯一 ID 做 DiffUtil 比较。
- Adapter 不持有 Fragment。
- 点击通过 Lambda 传回：

```kotlin
MealAdapter(
    onItemClick = viewModel::onMealClicked
)
```

Group Feed 混合类型可使用：

- 一个统一 `item_group_feed.xml`，根据类型显示标签；或
- Meal/Drink 两种 ViewHolder。

MVP 建议先用统一布局，降低复杂度。

## 13. 表单验证

分两层：

### Android 本地验证

- 必填。
- 格式。
- 数值范围。
- 字段间关系。

### 后端验证

- Email 唯一性。
- Group 权限。
- 记录所有权。
- Invite Code 有效性。
- Token 和业务冲突。

后端返回 `details` 时，ViewModel 将字段错误映射回表单。

## 14. 日期和金额

### 日期

- API 使用 ISO 8601。
- 上传时间必须带 offset，例如 `2026-06-23T12:30:00+08:00`。
- 后端推荐保存 UTC。
- UI 按设备时区显示。
- Android 建议使用 `java.time`。

### 金额

- UI 显示两位小数。
- 不在 DTO getter 中拼接货币符号。
- 货币符号和 locale 由 `FormatUtils` 处理。

## 15. XML 资源规范

- 所有用户可见文本放入 `strings.xml`。
- 间距放入 `dimens.xml`。
- 颜色放入 `colors.xml`，页面中不硬编码 Hex。
- Enum 的 UI Label 放入 `strings.xml` 或 `arrays.xml`。
- 布局 ID 使用语义命名，例如 `buttonSubmitMeal`，避免 `btn1`。
- 可复用 Loading/Empty/Error 视图使用 `<include>`。

## 16. Manifest 和开发网络

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

本地 HTTP 可配置：

```xml
<application
    android:name=".FoodMindApplication"
    android:networkSecurityConfig="@xml/network_security_config"
    ... />
```

`network_security_config.xml` 只对本地开发地址允许明文流量。正式环境不得全局开启 cleartext。

## 17. 依赖注入

对课程项目有两种可接受方案：

### 简单方案

在 `FoodMindApplication` 中创建 Api 和 Repository，通过 ViewModelFactory 注入。

优点：依赖少、容易讲解。  
缺点：手写 Factory 较多。

### Hilt 方案

使用 Hilt 注入 Retrofit、Repository 和 ViewModel。

优点：规范、扩展方便。  
缺点：课程时间有限时增加学习和配置成本。

建议：如果团队未学过 Hilt，使用简单方案，不要为架构分数冒进。

## 18. 命名规范

| 类型 | 示例 |
|---|---|
| Fragment | `HistoryFragment` |
| ViewModel | `HistoryViewModel` |
| State | `HistoryUiState` |
| Event | `HistoryUiEvent` |
| API | `MealApi` |
| Repository | `MealRepository` |
| Request | `CreateMealRequest` |
| DTO | `MealRecordDto` |
| Layout | `fragment_history.xml` |
| List item | `item_meal.xml` |
| Dialog | `dialog_create_group.xml` |

## 19. 不建议采用的实现

- 每个页面一个 Activity。
- Fragment 直接调用 Retrofit。
- 一个 `MainViewModel` 管理整个 App 所有功能。
- 一个巨大的 `ApiService` 包含全部接口。
- 将所有 DTO 放入单个 `Models.kt`。
- 使用 `Map<String, Any>` 代替明确 DTO。
- 在 Adapter 内执行导航或 API。
- 在 XML 或 Kotlin 中硬编码 Token、用户 ID、Group ID。
- 为每个小操作创建多层 UseCase；MVP 暂不需要 Domain 层。

## 20. Feature 到代码映射

| Feature | Fragment | ViewModel | Repository | API |
|---|---|---|---|---|
| Login/Register | Login/Register | AuthViewModel | AuthRepository | AuthApi |
| Profile | ProfileSetup/Profile | ProfileViewModel | ProfileRepository | ProfileApi |
| Home | Home | HomeViewModel | 多 Repository 聚合 | Auth/Analytics/Meal/Drink |
| Meal form | AddMeal | LogViewModel | MealRepository | MealApi |
| Drink form | AddDrink | LogViewModel | DrinkRepository | DrinkApi |
| History | History | HistoryViewModel | Meal/Drink Repository | Meal/Drink Api |
| Record detail | Meal/Drink Detail | Detail ViewModel | 对应 Repository | 对应 Api |
| Groups | Groups/Detail/Feed | GroupViewModel | GroupRepository | GroupApi |
| Recommendation | Recommendation | RecommendationViewModel | RecommendationRepository | RecommendationApi |
| Analytics | Analytics | AnalyticsViewModel | AnalyticsRepository | AnalyticsApi |

## 21. 架构完成标准

- 所有 Fragment 不直接引用 Retrofit Api。
- 所有列表有 RecyclerView Adapter 和 DiffUtil。
- 所有远程页面由 UiState 驱动。
- 所有受保护请求自动带 Token。
- 401 统一处理。
- Fragment Binding 在 `onDestroyView` 清理。
- 导航只传 ID，不传大型 Serializable 对象。
- 每个 Feature 可以独立找到 API、Repository、DTO 和 UI。
