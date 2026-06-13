# 开发与构建说明 / Dev Setup & Build

> 本文件说明 FitLog 工程结构、如何构建出可安装的 `.apk`，以及"数据不混杂"的实现位置。
> Structure of the FitLog project, how to build an installable `.apk`, and where the
> per-user data isolation lives.

---

## 1. 构建前置条件 / Prerequisites
本机目前 **未安装 Java / Android Studio**，因此只能写代码、暂不能编译。要构建/运行需先安装：

- **Android Studio**（推荐，自带 JDK + Android SDK + 模拟器，并会自动补全 Gradle Wrapper）。
  下载后用它「Open」`D:\MyAndroidApp` 文件夹即可，首次会自动同步并下载 Gradle 8.9 与依赖。

> 不需要单独装 Gradle：Android Studio 会根据 `gradle/wrapper/gradle-wrapper.properties`
> 自动生成 `gradlew` 与 wrapper jar。

## 2. 技术栈 / Tech Stack
- Kotlin 2.0 + Jetpack Compose（Material 3）
- Room（结构化数据） + DataStore Preferences（当前用户等少量设置）
- 单 Activity + Navigation Compose；UI 用 ViewModel，数据访问走 Repository
- 手动依赖容器 `AppContainer`（小型自用 App 不引入 Hilt，减少构建复杂度）
- 完全离线运行；`INTERNET` 权限仅为将来动作/肌群联网搜索预留
- `applicationId` / `namespace`：`com.fitlog`；minSdk 26，target/compile 35

## 3. 目录结构 / Project Layout
```
D:\MyAndroidApp\
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle\libs.versions.toml          # 版本目录
├── app\
│   ├── build.gradle.kts
│   └── src\main\
│       ├── AndroidManifest.xml
│       ├── res\                        # 图标、字符串、主题
│       └── java\com\fitlog\
│           ├── FitLogApplication.kt    # 入口 + 容器 + 首次播种
│           ├── MainActivity.kt
│           ├── data\
│           │   ├── AppContainer.kt     # 手动 DI
│           │   ├── local\
│           │   │   ├── AppDatabase.kt
│           │   │   ├── Converters.kt   # LocalDate <-> epochDay
│           │   │   ├── SeedData.kt     # 内置肌群/动作种子
│           │   │   ├── entity\         # 7 张表（见下）
│           │   │   └── dao\            # 每个面向用户的查询都按 userId 过滤
│           │   ├── prefs\              # DataStore：当前用户
│           │   └── repository\         # UserRepository / TrainingRepository
│           └── ui\
│               ├── navigation\         # Routes + FitLogNavHost
│               ├── theme\
│               ├── FitLogViewModelFactory.kt
│               └── screens\            # ProfileSelect(可用) + 其余占位
├── docs\                               # 记录与说明
└── skills-library\                     # 参考用 Android skill（不参与构建，已 gitignore）
```

## 4. 数据模型与"不混杂"的实现 / Data model & isolation
七张表：`user_profile`、`muscle_group`、`exercise`、`workout`、`workout_exercise`、
`workout_set`、`body_weight`。

**隔离关键**：`workout` 和 `body_weight` 都带 `userId` 外键；自定义动作带 `ownerUserId`。
所有面向用户的 DAO 查询都 **`WHERE userId = :userId`**（见
`WorkoutDao` / `BodyWeightDao` / `ExerciseDao`）。切换用户只改 DataStore 里的
`current_user_id`，因此一个 profile 永远查不到另一个 profile 的数据。
"复用上次训练"用 `WorkoutDao.findLastCompleted(userId, muscleGroupId)`，同样受 `userId` 约束。

## 5. 现在能跑到哪一步 / Current state
- ✅ 工程骨架、统一数据模型、导航骨架已就绪。
- ✅ **用户选择页可用**：创建用户、点选切换当前用户（验证隔离链路）。
- ⏳ 首页 / 训练流程 / 日历 / 体重 页面为占位，等与 Codex 敲定 UI 后实现。

## 6. 怎么得到 .apk / Producing the APK
装好 Android Studio 后：
- 菜单 **Build → Build Bundle(s)/APK(s) → Build APK(s)**，或命令行 `.\gradlew assembleDebug`。
- 产物在 `app\build\outputs\apk\debug\app-debug.apk`，传到安卓手机即可安装
  （手机需允许"安装未知来源应用"）。
- 正式分发版需配置签名（release keystore），后续再做。
