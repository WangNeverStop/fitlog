# 给协作 AI 的共享上下文 / Shared Context for Collaborating Agents

> 本文件给所有参与本项目的 AI(Claude、Codex 等)阅读。开工前请先读这份 + `PROJECT_LOG.md`。
> Read this file + `PROJECT_LOG.md` before doing any work.

---

## 项目是什么 / What this is
- 一个**自用的 Android App**,个人使用、不上架应用商店。
- A personal-use **Android app**, not for the app store.
- 与任何论文/学术项目**无关**,是完全独立的项目。
- **Unrelated** to any thesis; a fully standalone project.

## 工作目录 / Working directory
- 根目录 / Root: `D:\MyAndroidApp` —— 所有 agent 都在此目录下工作。
- `docs\` 文档与记录 / docs & logs
- `app\`  源代码 / source code
- 启动方式 / How to start: 在 `D:\MyAndroidApp` 下启动你的 CLI(`cd D:\MyAndroidApp` 再运行)。

## 协作规则 / Collaboration rules
1. **每次做完一项工作,把决策和进度追加到 `docs\PROJECT_LOG.md`**,让另一个 agent 知道发生了什么。
   After each task, append decisions/progress to `docs\PROJECT_LOG.md`.
2. 改动较大的文件前,先看 PROJECT_LOG 最近条目,避免和对方撞车。
   Check the latest PROJECT_LOG entries before big edits to avoid conflicts.
3. 不确定的设计决策,写进 PROJECT_LOG 的「待定 Open Questions」让用户拍板,不要自作主张。
   Put uncertain design decisions under "Open Questions" for the user to decide.
4. 平台:**只做 Android**。环境:Windows 11,PowerShell。
   Platform: Android only. Env: Windows 11, PowerShell.

## 当前状态 / Current status (2026-06-07)
- 工作区已建好。/ Workspace created.
- **功能与技术路线尚未确定** —— 见 PROJECT_LOG「待定」。
  App features & tech route NOT yet decided — see PROJECT_LOG "Open Questions".

## 待用户确认的关键问题 / Pending user decisions
1. App 具体功能 / What the app does.
2. 技术路线:PWA 网页 App / Flutter / 原生 Android / 无代码。
   Tech route: PWA / Flutter / native Android / no-code.
3. 用户自学 vs. 直接生成可用代码 / Learn vs. generated code.
