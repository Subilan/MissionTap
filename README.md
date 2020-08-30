# 📕 MissionTap

![Java CI with Gradle](https://github.com/sotapmc/MissionTap/workflows/Java%20CI%20with%20Gradle/badge.svg)
![](https://img.shields.io/badge/11-brown?logo=java)
![](https://img.shields.io/badge/poweredby-sotapmc-blue)
![](https://img.shields.io/badge/API-1.16.1--R0.1-orange)

**MissionTap** 是一个简单的 Minecraft 游戏内任务系统。该系统共有五种任务类型。借助本插件，你可以实现

- 每隔*一定*的时间从事先写好的任务列表中随机抽取*一定数量*的任务，随机分配到每个玩家的任务列表中，玩家可从任务列表中选取任务
- 配置每个任务所需要收集的物品数量、触发的事件数量。当前仅支持
  - ~~物品收集 `collecting`~~ 暂不支持，可用 `blockbreak` 粗略代替
  - 物品合成 `crafting`
  - 动物繁殖 `breeding`
  - 方块破坏 `blockbreak`
  - 生物击杀 `combat`
- 配置每个任务完成之后所要执行的指令，支持占位符；
- 配置每个任务奖励的 [Ageing](//github.com/sotapmc/Ageing) 经验值。
- ...

同时，你还可以通过改变配置文件中不同的项目，来搭配出与众不同的任务模式。关于这一点，可查看我们的[开发文档](//docs.sotap.dev/#/missiontap/index)。

## 关于和下载

#### 版本适配

推荐 1.16+，理论 1.13+（未测试）

#### 下载

请注意，本项目的 Release 仅用作里程碑，只有下载 Actions 中的最新构建，才能确保最少 Bug、最新内容体验。

- [GitHub Actions](https://github.com/sotapmc/MissionTap/actions?query=workflow%3A%22Java+CI+with+Gradle%22)
- 或下载源代码后自行编译，直接 `gradle build` 即可。

#### 鸣谢

- Sapherise，提供主要创意
- 内群里的伙伴们

## 协议

MIT
