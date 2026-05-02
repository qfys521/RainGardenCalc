<!-- markdownlint-disable MD033 MD041 -->
<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" height="120" alt="Milthm Garden Calc">
</p>

<div align="center">

# Milthm Garden Calc

<!-- prettier-ignore-start -->
<!-- markdownlint-disable-next-line MD036 -->
_✨ 音游 Milthm 花园系统辅助规划工具 ✨_
<!-- prettier-ignore-end -->

</div>

<p align="center">
  <img src="https://img.shields.io/badge/Android-9+-3DDC84?logo=android&logoColor=white" alt="android">
  <img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white" alt="kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=jetpackcompose&logoColor=white" alt="compose">
</p>

## 简介

Milthm Garden Calc 是一款专为音游 [Milthm](https://store.steampowered.com/app/2351260/Milthm/) 设计的花园系统辅助规划工具。帮助玩家合理安排种植计划，高效获取材料以解锁歌曲和提升等级。

## 功能

- **种植规划器** — 根据登录时间和花盆数量，自动生成最优种植/收获计划
- **浇水模拟** — 模拟浇水冷却机制，计算最优浇水策略和期望产量
- **等级规划** — 计算从当前等级升级到目标等级所需的全部材料
- **歌曲解锁** — 计算解锁每首歌曲所需的材料清单
- **作物图鉴** — 查看所有作物的生长周期、产量、解锁条件
- **截图分享** — 一键生成规划方案图片，方便分享

## 截图

<img src=".github/images/5C15319FD3831D2E604A23F2D14AFA18.jpg" alt="Image">
<img src=".github/images/93BA4EBD699D4D415ACA0407DF8A0E49.jpg" alt="Image">
<img src=".github/images/515A58119725946E87FB2A133B13704F.jpg" alt="Image">
<img src=".github/images/286133720568034A98F582ED7A3EBD64.jpg" alt="Image">
<img src=".github/images/AB1FCF9E5672B694ACC713766FC6A135.jpg" alt="Image">
<img src=".github/images/C8C237CE2C25EC6049DC95BD078E9F20.jpg" alt="Image">

## 构建

```bash
./gradlew assemble
```

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + [Miuix](https://github.com/miuix-kotlin-multiplatform/miuix)
- **架构**: 单 Activity + Compose Navigation
- **最低版本**: Android 9 (API 28)
