# 光阴如梭 — 个人效率工具

专为 **小米15 / 澎湃OS** 深度适配的本地效率APP。

## 功能
- 📋 **0-24h 时间轴** — 按小时排列每日任务，左右滑动切换日期
- 🍅 **番茄钟** — 前台Service保活，专注/休息切换，关联目标任务
- 🎯 **目标树** — 无限级子母任务，次数/日期双打卡，进度百分比
- 🏷️ **标签系统** — 独立标签管理，彩色标识，专注时长统计
- 📊 **统计** — 日/月/年专注趋势，APP使用分布，标签时长分析
- 📱 **娱乐监控** — UsageStatsManager 检测 APP超时，全屏不可跳过弹窗
- 🪟 **桌面小部件** — 时间线待办 + 番茄钟一键启动

## 隐私安全 🔒
**本APP完全本地运行，零联网权限，不收集任何个人信息！**
- ✅ 无网络请求权限
- ✅ 所有数据存储在本地 Room 数据库
- ✅ 不包含任何分析SDK
- ✅ 不需要注册账号

## 从 GitHub 编译 APK

### 方式一：GitHub Actions（推荐）
1. 将本项目 **Fork** 或上传到你的 GitHub 仓库
2. 进入仓库页面 → 点击 **Actions** 标签
3. 在左侧选择 **Build APK** → 点击 **Run workflow**
4. 等待几分钟，编译完成后在 `Actions` 页面点击运行记录
5. 在 **Artifacts** 处下载 `GuangYinRuSuo-APK.zip`
6. 解压即可得到 `.apk` 安装包

### 方式二：本地编译
```bash
# 确保已安装 JDK 17 和 Android SDK
./gradlew assembleDebug
# 生成的 APK 在 app/build/outputs/apk/debug/
```

## 下载预览
项目包含一个 `preview/app_preview.html` 文件，双击即可在浏览器中预览 APP 界面和交互效果。

## 技术栈
| 组件 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI | XML + Fragment + Material 3 |
| 架构 | MVVM (UI/逻辑/数据三层分离) |
| 数据库 | Room |
| 后台 | Foreground Service + WorkManager |
| 图表 | MPAndroidChart |
| 构建 | Gradle KTS + AGP 8.5 |

## 许可证
MIT — 自由使用、修改、分享。
