<div align="center">

# 🏋️ FitLog

**一款简洁、纯本地、注重隐私的个人健身记录 App**
*A clean, fully-offline, privacy-first personal workout tracker for Android*

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)](#-下载与安装)
[![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)](#-技术栈)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](#-技术栈)
[![Data](https://img.shields.io/badge/Data-Local%20only-success)](#-隐私)
[![Version](https://img.shields.io/badge/Version-0.1.0-blue)](#)

</div>

---

## 📖 简介

**FitLog** 是一款给自己用的健身记录 App：**所有数据只存在你的手机里**，不联网、不上传、不需要注册账号。
打开就能开始训练、记录每一组、回看趋势——像一本随身的训练日记。

> **Why another fitness app?** No account, no ads, no cloud, no tracking. Your data never leaves your phone.

<div align="center">

<!-- 截图占位：在仓库根目录新建 screenshots/ 文件夹放入图片后，下面会自动显示 -->
<!-- Add PNGs under screenshots/ and they will render here. -->

| 首页 | 训练中 | 日历 | 肌肉图 |
|:---:|:---:|:---:|:---:|
| ![home](screenshots/home.png) | ![session](screenshots/session.png) | ![calendar](screenshots/calendar.png) | ![muscle](screenshots/muscle.png) |

*（截图待补充 / screenshots coming soon）*

</div>

---

## ✨ 功能 Features

### 👤 多用户 · 零密码
- Netflix 式头像滑动切换用户，**每个用户的数据完全隔离**
- 本地用户，无需密码、无需联网

### 🏠 首页
- 按时间问候 + 每日轮换激励语
- 一键「开始今天的训练」
- 本周训练圆点概览（已训练次数 / 总时长）
- 最近一次训练卡片 + 体重小折线

### 💪 完整训练闭环
> 选目标 → 复用上次（可选）→ 选动作 → 正式训练 → 完成总结
- **8 大训练目标**：胸 / 背 / 肩 / 手臂 / 腹 / 腿 / 臀 / 有氧，外加自由训练
- **一键复用上次同部位训练**，省去重复编排
- 动作卡片**长按拖拽排序**
- 训练中**实时计时**（开始时刻 + 已用时），逐组打勾，误触返回有二次确认
- **4 种记录类型**自动适配字段：
  | 类型 | 字段示例 | 展示 |
  |---|---|---|
  | 力量 | 重量 + 组数 + 次数 | `60kg × 10次` |
  | 自重 | 组数 + 次数 | `自重 × 15次` |
  | 计时 | 组数 + 秒数 | `45 秒` |
  | 有氧 | 时长 + 配速 + 距离 | `20 分钟 · 8km/h · 3km` |

### 📅 日历
- 月视图：**今天高亮**、训练日按部位**彩色圆点**、备忘录 ★、**2026 中国法定节假日**（含调休）
- 点任意一天查看：节假日 → 个人备忘录 → 当天训练
- 内嵌**体重趋势图**

### 📒 训练记录
- 全部历史训练列表，点开看每个动作每一组
- **补录历史训练**：把以前练过的也记进来，自动汇入日历与统计

### ⚖️ 体重 / BMI
- 记录体重、**自动计算 BMI** 与分类（偏瘦 / 正常 / 超重 / 肥胖）
- 带坐标轴的趋势折线图，支持补录历史日期

### 🗺️ 动作库 + 人体肌肉图
- **82 个内置动作**，含器械、动作要领与注意事项
- **真实人体肌肉响应图**：按动作高亮主 / 次发力肌群，并**根据性别**自动切换男 / 女图
- 纯本地 Compose 绘制，**无 WebView、无联网图片**

### 🔧 个性化设置
- 重量单位 kg / lb、组间休息时长、身体资料（身高 / 年龄 / 性别）
- 全部按用户隔离，重启后保留

---

## 📦 下载与安装

> FitLog 通过 **APK 侧载** 安装（不上架应用商店）。

1. 到本仓库的 **[Releases](../../releases)** 页面，下载最新的 `FitLog-vX.X.X.apk`
2. 把 APK 传到手机（微信 / QQ / 数据线 / 网盘均可）
3. 在手机上点开 APK 安装：首次会提示**「允许安装未知来源应用」**，按提示去设置里打开对应来源的权限即可
4. 安装完成，打开 App，新建一个用户，开练 💪

**系统要求**：Android 8.0（API 26）及以上。

> ℹ️ 第一次安装系统会警告"未知开发者"，这是侧载安装的正常提示——因为这是自签名的个人 App，并非恶意软件。

---

## 🛠 从源码构建 Build from source

```bash
git clone https://github.com/WangNeverStop/fitlog.git
```
1. 用 **Android Studio** 打开工程
2. 等待 Gradle Sync 完成（首次会下载依赖）
3. `Run ▶` 安装到设备，或 `Build → Generate Signed App Bundle / APK` 产出可分发的 APK

环境：AGP 8.7 · Gradle 8.9 · JDK 17 · minSdk 26 / targetSdk 35

---

## 🧱 技术栈

| 层 | 选型 |
|---|---|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | 单 Activity + Navigation Compose · MVVM · 手动依赖注入 |
| 本地数据库 | Room |
| 轻量设置 | DataStore Preferences |
| 图表 / 肌肉图 | 纯 Compose Canvas 绘制 |

**完全离线，无任何网络权限与第三方追踪。**

---

## 🔒 隐私

- 📵 **零联网**：App 不请求网络权限，数据不出本机
- 🙈 **无账号、无收集**：不需要注册，不采集任何个人信息
- 🗑️ **可一键删除**：在「我的」里可删除当前用户及其全部数据

---

## 🗺️ 路线图 Roadmap

- [ ] 每个部位「我的默认动作」一键加载
- [ ] 组间休息倒计时
- [ ] 本地数据备份 / 恢复（导出导入）
- [ ] 训练容量、PR（个人最佳）等进阶统计
- [ ] 关于页与开源声明入口

---

## 🙏 致谢 Acknowledgements

- 人体肌肉图改编自 [**react-muscle-highlighter**](https://github.com/soroojshehryar/react-muscle-highlighter)（MIT），详见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)

---

## 📄 License

个人项目，目前未指定开源许可（保留所有权利）。如需复用请先联系作者。
Personal project — currently all rights reserved; please contact the author before reuse.
