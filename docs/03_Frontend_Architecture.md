# FoodMind Android 前端架构

版本：1.1  
日期：2026-06-23  
技术约束：Kotlin + XML Layout + Activity + Intent

## 1. 架构目标

本架构服务于课程设计和 MVP，优先保证：

- 技术方案符合课程要求，便于讲解和现场演示。
- 每个页面职责清楚，文件与页面可以直接对应。
- 页面跳转、参数传递和返回栈行为可预测。
- UI、业务状态和网络访问保持分层。
- 可以按功能逐步开发、编译和测试。
- 不为了“看起来高级”引入课程范围外的复杂框架。

固定技术边界：

- 使用多个 Activity 承载页面。
- 使用 XML Layout 构建界面。
- 使用显式 Intent 完成页面跳转。
- 使用 Intent Extra 传递轻量参数。
- 不使用 Fragment、NavHost、Navigation Component、NavController、Safe Args 或 Jetpack Compose。

总体调用链：

```text
Activity + XML Layout
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
| UI | Activity、XML Layout、Material Components |
| Page navigation | Explicit Intent |
| Parameter passing | Intent Extra |
| Back stack | `finish()`、Intent Flags |
| Architecture | MVVM |
| State | StateFlow；课程要求时可改用 LiveData |
| Async | Kotlin Coroutines |
| HTTP | Retrofit + OkHttp |
| JSON | Gson 或 Moshi，项目内只选一种 |
| Lists | RecyclerView + ListAdapter + DiffUtil |
| View access | View Binding |
| Local session | SharedPreferences；加密存储为可选增强 |
| Image | MVP 暂无上传；远程 URL 可使用 Coil/Glide |

## 3. 页面分类

### 3.1 启动路由 Activity

`MainActivity` 是应用唯一 Launcher Activity，只负责决定首次打开哪个页面：

```text
App Launch
→ No token
    → LoginActivity
→ Has token
    → GET /api/auth/me
        → 401 → clear token → LoginActivity
        → profileCompleted=false → ProfileSetupActivity
        → profileCompleted=true → HomeActivity
```

完成路由后，`MainActivity` 必须调用 `finish()`，不能停留在业务返回栈中。

### 3.2 认证与引导 Activity

包括：

- `LoginActivity`
- `RegisterActivity`
- `ProfileSetupActivity`

认证成功或引导完成后，需要清空认证页面返回栈，用户按系统返回键不能重新进入 Login 或 Register。

### 3.3 一级业务 Activity

包括：

- `HomeActivity`
- `LogActivity`
- `HistoryActivity`
- `GroupsActivity`
- `ProfileActivity`

每个一级页面可以显示相同的底部入口控件。控件只负责通过显式 Intent 打开目标 Activity，不承载 Fragment。

课程版本优先保证返回栈简单、可演示：切换一级页面时打开目标 Activity，并结束当前一级 Activity。页面重新进入后由 ViewModel 恢复必要状态或重新加载数据。

### 3.4 二级业务 Activity

包括：

- `AddMealActivity`
- `AddDrinkActivity`
- `MealDetailActivity`
- `DrinkDetailActivity`
- `GroupDetailActivity`
- `GroupFeedActivity`
- `RecommendationActivity`
- `AnalyticsActivity`

二级页面由来源页面启动，返回时调用 `finish()`。来源列表在 `onResume()` 中按需刷新。

## 4. 分层职责

### 4.1 Activity

Activity 负责：

- Inflate 并绑定 XML Layout。
- 获取用户输入。
- 观察 UiState 和 UiEvent。
- 渲染 Loading、Content、Empty、Error 和 Submitting。
- 调用 ViewModel 的公开方法。
- 使用显式 Intent 执行页面跳转。
- 读取、验证 Intent Extra。
- 处理 Toolbar 返回和系统返回行为。

Activity 不应：

- 直接创建或调用 Retrofit API。
- 将复杂业务校验全部写在点击事件中。
- 长期保存服务器数据。
- 在 Adapter 中执行 API 调用。
- 通过 Intent 传递完整 DTO、Repository 或 ViewModel。
- 持有其他 Activity 的 View 或 Binding。

### 4.2 ViewModel

ViewModel 负责：

- 保存页面状态和表单状态。
- 执行本地输入验证。
- 调用 Repository。
- 将 NetworkResult 转换为 UiState。
- 发出一次性 UiEvent，例如打开页面、显示 Snackbar、结束当前页面。

示例：

```kotlin
data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)
```

Activity 负责执行 Intent；ViewModel 只能发出语义事件，例如 `OpenProfileSetup`，不能持有 Activity Context。

### 4.3 Repository

Repository 负责：

- 组合一个 Feature 所需的 API 调用。
- 将 Retrofit 响应和异常转换为统一的 `NetworkResult`。
- 隐藏网络实现细节。
- 必要时处理多个接口的顺序调用。
- 在认证成功后调用 TokenManager 保存 Token。

Repository 不应持有 Activity、View 或 View Binding。

### 4.4 Retrofit API interface

API interface 只定义：

- HTTP Method。
- Path。
- Query。
- Request Body。
- Response DTO。

API interface 不放 UI 文案、Intent 或返回栈逻辑。

### 4.5 DTO

DTO 与后端 JSON 一一对应：

- 请求对象以 `Request` 结尾。
- API 返回对象以 `Dto` 或 `Response` 结尾。
- ID 使用 `Long`。
- 可空字段必须声明为 nullable。
- `confirmPassword` 等仅用于本地表单的字段不得放入后端 Request DTO。

## 5. 项目目录结构

```text
app/src/main/
├── AndroidManifest.xml
│
├── java/com/foodmind/app/
│   ├── FoodMindApplication.kt
│   ├── MainActivity.kt
│   ├── LaunchViewModel.kt
│   ├── LaunchUiState.kt
│   │
│   ├── common/
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
│   │   │   ├── BaseActivity.kt
│   │   │   ├── UiText.kt
│   │   │   ├── UiEvent.kt
│   │   │   └── BaseListAdapter.kt
│   │   │
│   │   └── util/
│   │       ├── IntentKeys.kt
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
│   │   │       ├── LoginActivity.kt
│   │   │       ├── RegisterActivity.kt
│   │   │       ├── AuthViewModel.kt
│   │   │       ├── AuthUiState.kt
│   │   │       ├── AuthUiEvent.kt
│   │   │       └── AuthViewModelFactory.kt
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
│   │   │       ├── ProfileSetupActivity.kt
│   │   │       ├── ProfileActivity.kt
│   │   │       ├── ProfileViewModel.kt
│   │   │       ├── ProfileUiState.kt
│   │   │       └── ProfileUiEvent.kt
│   │   │
│   │   ├── home/ui/
│   │   │   ├── HomeActivity.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── HomeUiState.kt
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
│   │   │   │       ├── AddMealActivity.kt
│   │   │   │       ├── MealDetailActivity.kt
│   │   │   │       ├── MealAdapter.kt
│   │   │   │       ├── MealFormState.kt
│   │   │   │       └── MealDetailViewModel.kt
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
│   │   │   │       ├── AddDrinkActivity.kt
│   │   │   │       ├── DrinkDetailActivity.kt
│   │   │   │       ├── DrinkAdapter.kt
│   │   │   │       ├── DrinkFormState.kt
│   │   │   │       └── DrinkDetailViewModel.kt
│   │   │   │
│   │   │   └── ui/
│   │   │       ├── LogActivity.kt
│   │   │       ├── HistoryActivity.kt
│   │   │       ├── LogViewModel.kt
│   │   │       └── HistoryViewModel.kt
│   │   │
│   │   ├── group/
│   │   │   ├── data/
│   │   │   │   ├── GroupApi.kt
│   │   │   │   ├── GroupRepository.kt
│   │   │   │   └── dto/
│   │   │   └── ui/
│   │   │       ├── GroupsActivity.kt
│   │   │       ├── GroupDetailActivity.kt
│   │   │       ├── GroupFeedActivity.kt
│   │   │       ├── GroupViewModel.kt
│   │   │       └── adapter/
│   │   │
│   │   ├── recommendation/
│   │   │   ├── data/
│   │   │   └── ui/
│   │   │       ├── RecommendationActivity.kt
│   │   │       ├── RecommendationViewModel.kt
│   │   │       └── RecommendationAdapter.kt
│   │   │
│   │   └── analytics/
│   │       ├── data/
│   │       └── ui/
│   │           ├── AnalyticsActivity.kt
│   │           └── AnalyticsViewModel.kt
│   │
│   └── routing/
│       ├── ActivityNavigator.kt
│       └── MainSection.kt
│
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_login.xml
    │   ├── activity_register.xml
    │   ├── activity_profile_setup.xml
    │   ├── activity_profile.xml
    │   ├── activity_home.xml
    │   ├── activity_log.xml
    │   ├── activity_add_meal.xml
    │   ├── activity_add_drink.xml
    │   ├── activity_history.xml
    │   ├── activity_meal_detail.xml
    │   ├── activity_drink_detail.xml
    │   ├── activity_groups.xml
    │   ├── activity_group_detail.xml
    │   ├── activity_group_feed.xml
    │   ├── activity_recommendation.xml
    │   ├── activity_analytics.xml
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
    │   ├── view_loading.xml
    │   ├── view_empty_state.xml
    │   └── view_error_state.xml
    ├── menu/
    │   ├── menu_main_sections.xml
    │   ├── menu_history_filter.xml
    │   └── menu_detail.xml
    ├── drawable/
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

项目中不应存在：

```text
res/navigation/nav_graph.xml
FragmentContainerView
NavHostFragment
NavController
XxxFragment.kt
fragment_xxx.xml
```

### 5.1 当前工程迁移清单

从现有基础工程切换到本架构时，按以下顺序处理：

1. 删除 `common/ui/BaseFragment.kt`，创建 `common/ui/BaseActivity.kt`。
2. 删除 `res/navigation/nav_graph.xml`。
3. 确认 `activity_main.xml` 不包含 FragmentContainerView 或 NavHost。
4. 从 Gradle 中移除 Navigation Component 和 Safe Args 依赖；保留 Activity、Lifecycle、Retrofit、Coroutines 和 View Binding。
5. 创建 LoginActivity、RegisterActivity 和 ProfileSetupActivity 及对应 `activity_*.xml`。
6. 在 AndroidManifest.xml 注册新增 Activity。
7. 使用显式 Intent 打通 `MainActivity → LoginActivity → RegisterActivity → ProfileSetupActivity`。
8. 完成认证返回栈测试后，再开始 Home 和其他业务页面。

迁移过程中不得一次创建所有空 Activity。每新增一个 Activity，都应同时完成布局、Manifest 注册、Intent 入口和最小返回行为。

## 6. Intent 导航规范

### 6.1 普通页面跳转

```kotlin
val intent = Intent(this, RegisterActivity::class.java)
startActivity(intent)
```

返回上一页：

```kotlin
finish()
```

### 6.2 认证成功后清空返回栈

Login、Register 或 Profile Setup 完成后：

```kotlin
val intent = Intent(this, ProfileSetupActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TASK
}
startActivity(intent)
```

进入 Home 时使用相同规则，确保系统返回键不会回到认证页面。

### 6.3 一级页面切换

一级页面切换使用统一的 `ActivityNavigator`，不得在五个 Activity 中复制不同实现：

```kotlin
object ActivityNavigator {
    fun openMainSection(
        activity: Activity,
        target: Class<out Activity>
    ) {
        if (activity::class.java == target) return

        val intent = Intent(activity, target).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(intent)
        activity.finish()
    }
}
```

这套规则优先保证：

- 同一一级页面不会被连续重复创建。
- 返回键不会穿过多个一级页面形成混乱路径。
- 二级页面仍可正常返回来源页面。

### 6.4 Intent Extra

统一 Key：

```kotlin
object IntentKeys {
    const val EXTRA_MEAL_ID = "extra_meal_id"
    const val EXTRA_DRINK_ID = "extra_drink_id"
    const val EXTRA_GROUP_ID = "extra_group_id"
    const val EXTRA_FORM_MODE = "extra_form_mode"
    const val EXTRA_SOURCE = "extra_source"
    const val EXTRA_MEAL_TYPE = "extra_meal_type"
}
```

发送：

```kotlin
val intent = Intent(this, MealDetailActivity::class.java).apply {
    putExtra(IntentKeys.EXTRA_MEAL_ID, mealId)
}
startActivity(intent)
```

接收：

```kotlin
val mealId = intent.getLongExtra(IntentKeys.EXTRA_MEAL_ID, -1L)
if (mealId <= 0L) {
    showMessage(getString(R.string.error_invalid_meal))
    finish()
    return
}
```

规则：

- Intent 只传 ID、模式、来源等轻量值。
- 不传完整 DTO、Bitmap、Repository 或 ViewModel。
- 必填参数必须提供无效默认值并立即校验。
- Key 不允许散落为字符串字面量。

## 7. Activity View Binding 模式

```kotlin
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }
}
```

注意：

- Activity Binding 在 `onCreate()` 初始化。
- 不需要使用 Fragment 的 nullable `_binding` 模式。
- Binding 不得存入单例、Repository 或 ViewModel。
- Activity 销毁后不得由后台回调继续直接更新 View。

## 8. ViewModel 与状态观察

观察 StateFlow 时使用 Activity 生命周期：

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
            viewModel.uiState.collect(::render)
        }
        launch {
            viewModel.events.collect(::handleEvent)
        }
    }
}
```

UiState 用于可重复渲染：

- 表单值。
- Loading/Submitting。
- Content/Empty/Error。
- Field errors。

UiEvent 用于只消费一次：

- 打开目标 Activity。
- 显示 Snackbar/Toast。
- 结束当前 Activity。
- 复制邀请码。

不要把一次性 Intent 跳转直接存为长期 State，否则旋转屏幕后可能重复打开页面。

## 9. 网络层设计

### 9.1 通用响应

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
```

### 9.2 统一结果

```kotlin
sealed interface NetworkResult<out T> {
    data class Success<T>(
        val data: T,
        val message: String
    ) : NetworkResult<T>

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

Repository 应调用统一的 `safeApiCall`，避免每个 Repository 重复解析错误。

### 9.3 Base URL

```kotlin
buildConfigField(
    "String",
    "BASE_URL",
    "\"http://10.0.2.2:8080/\""
)
```

- Android Emulator → `10.0.2.2`
- 真机 → 电脑局域网 IP
- 手机中的 `localhost` 不是开发电脑
- Retrofit Base URL 必须以 `/` 结尾

## 10. Session 和统一 401

`AuthInterceptor` 只负责为受保护请求添加：

```text
Authorization: Bearer <token>
```

统一流程：

```text
Protected API returns 401
→ NetworkErrorHandler emits SessionExpired
→ SessionManager clears token
→ current BaseActivity receives event
→ explicit Intent opens LoginActivity
→ NEW_TASK + CLEAR_TASK clears protected Activities
```

规则：

- Interceptor 不直接启动 Activity。
- Login/Register 请求不添加空 Token。
- Login 接口自身返回 401 时显示账号密码错误，不触发 Session expired。
- 多个并发请求同时返回 401 时，只执行一次全局跳转。

## 11. 表单验证

验证分两层：

### Android 本地验证

- 必填。
- 格式。
- 数值范围。
- 字段间关系。
- Confirm Password 一致性。

### 后端验证

- Email 唯一性。
- Group 权限。
- 记录所有权。
- Invite Code 有效性。
- Token 和业务冲突。

后端返回 `details` 时，ViewModel 将字段错误映射到对应 TextInputLayout。

提交期间：

- 禁用提交按钮。
- 必要时禁用输入框。
- 显示 ProgressIndicator。
- 忽略重复点击。
- 请求失败后保留用户输入。

## 12. RecyclerView

- 使用 `ListAdapter` 和 DiffUtil。
- Adapter 不持有 Activity。
- Adapter 不执行 Intent 或 API。
- 点击事件通过 Lambda 传回 Activity：

```kotlin
MealAdapter(
    onItemClick = { mealId ->
        openMealDetail(mealId)
    }
)
```

## 13. Manifest 规范

每个 Activity 必须在 Manifest 注册：

```xml
<application
    android:name=".FoodMindApplication"
    android:networkSecurityConfig="@xml/network_security_config"
    ...>

    <activity
        android:name=".feature.auth.ui.RegisterActivity"
        android:exported="false" />

    <activity
        android:name=".feature.auth.ui.LoginActivity"
        android:exported="false" />

    <activity
        android:name=".MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

规则：

- 只有 Launcher Activity 默认 `exported=true`。
- 内部页面默认 `exported=false`。
- Manifest 声明 `android.permission.INTERNET`。
- 本地 HTTP 只通过开发用 network security config 放行。
- 正式环境不得全局允许明文流量。

## 14. XML 资源规范

- Activity 布局命名为 `activity_xxx.xml`。
- 列表项命名为 `item_xxx.xml`。
- Dialog 布局命名为 `dialog_xxx.xml`。
- 所有用户可见文本放入 `strings.xml`。
- 间距放入 `dimens.xml`。
- 颜色放入 `colors.xml`。
- ID 使用语义名称，例如 `buttonRegister`，不使用 `button1`。
- 长表单使用 ScrollView/NestedScrollView，避免小屏无法提交。

## 15. 依赖创建

课程项目使用简单依赖创建方式：

```text
FoodMindApplication
├── TokenManager
├── Retrofit APIs
└── Repositories

Activity
└── ViewModelFactory
    └── ViewModel
```

在 `FoodMindApplication` 中创建 API、Repository 和 TokenManager，再通过 ViewModelFactory 注入 ViewModel。

本课程版本不要求 Hilt，避免增加与核心业务无关的配置成本。

## 16. 命名规范

| 类型 | 示例 |
|---|---|
| Activity | `HistoryActivity` |
| ViewModel | `HistoryViewModel` |
| State | `HistoryUiState` |
| Event | `HistoryUiEvent` |
| API | `MealApi` |
| Repository | `MealRepository` |
| Request | `CreateMealRequest` |
| DTO | `MealRecordDto` |
| Activity Layout | `activity_history.xml` |
| List item | `item_meal.xml` |
| Intent Key | `EXTRA_MEAL_ID` |

## 17. Feature 到代码映射

| Feature | Activity | ViewModel | Repository | API |
|---|---|---|---|---|
| App launch | MainActivity | LaunchViewModel | AuthRepository | AuthApi |
| Login/Register | Login/RegisterActivity | AuthViewModel | AuthRepository | AuthApi |
| Profile | ProfileSetup/ProfileActivity | ProfileViewModel | ProfileRepository | ProfileApi |
| Home | HomeActivity | HomeViewModel | 多 Repository 聚合 | Auth/Analytics/Meal/Drink |
| Meal form | AddMealActivity | LogViewModel | MealRepository | MealApi |
| Drink form | AddDrinkActivity | LogViewModel | DrinkRepository | DrinkApi |
| History | HistoryActivity | HistoryViewModel | Meal/Drink Repository | Meal/Drink API |
| Record detail | Meal/DrinkDetailActivity | Detail ViewModel | 对应 Repository | 对应 API |
| Groups | Groups/Detail/FeedActivity | GroupViewModel | GroupRepository | GroupApi |
| Recommendation | RecommendationActivity | RecommendationViewModel | RecommendationRepository | RecommendationApi |
| Analytics | AnalyticsActivity | AnalyticsViewModel | AnalyticsRepository | AnalyticsApi |

## 18. 不建议采用的实现

- Activity 直接调用 Retrofit。
- 在 Activity 点击事件中堆积网络解析和业务规则。
- 使用隐式 Intent 打开应用内部页面。
- 通过 Intent 传递完整 DTO 或大型 Serializable 对象。
- 每个页面自行定义不同的 Extra Key。
- 登录成功后只调用 `finish()`，导致更早的认证页面仍留在返回栈。
- 在 Adapter 内启动 Activity 或调用 API。
- 一个巨大 ViewModel 管理整个 App。
- 一个巨大 ApiService 包含所有领域接口。
- 使用 `Map<String, Any>` 代替明确 DTO。
- 在 XML 或 Kotlin 中硬编码 Token、用户 ID、Group ID。
- 引入 Fragment、NavHost 或 Navigation Component 绕过课程约束。

## 19. 架构完成标准

- 所有页面由 Activity + XML Layout 实现。
- 项目不存在 Fragment、NavHost、NavController 和导航图依赖。
- 所有 Activity 已正确注册到 Manifest。
- Activity 不直接引用 Retrofit API。
- 所有远程页面由 UiState 驱动。
- Intent 只传轻量参数，且目标页面验证必填 Extra。
- 登录、注册、Profile Setup、Logout 和 401 的返回栈符合规格。
- 所有受保护请求自动携带 Token。
- Login/Register 不携带无效 Token。
- RecyclerView Adapter 使用 DiffUtil，且不执行导航或 API。
- View Binding 只在所属 Activity 内使用。
- 每个 Feature 都能独立找到 Activity、ViewModel、Repository、API 和 DTO。
