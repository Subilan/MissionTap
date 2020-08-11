# 📕 MissionTap

![Java CI with Gradle](https://github.com/sotapmc/MissionTap/workflows/Java%20CI%20with%20Gradle/badge.svg)
![](https://img.shields.io/badge/11-brown?logo=java)
![](https://img.shields.io/badge/poweredby-sotapmc-blue)
![](https://img.shields.io/badge/API-1.16.1--R0.1-orange)

**MissionTap** 是一个简单的 Minecraft 游戏内任务系统。目前，该系统一共有三种任务类型。借助本插件，你可以实现

- 每隔*一定*的时间从事先写好的任务列表中随机抽取*一定数量*的任务，作为玩家在游戏中可接受的任务；
- 配置每个任务所需要收集的物品数量、触发的事件数量。当前仅支持
  - 物品收集 `collecting`
  - 物品合成 `crafting`
  - 动物繁殖 `breeding`
  - 方块破坏 `blockbreak`
  - 生物击杀 `combat`
- 配置每个任务完成之后所要执行的指令，支持占位符；
- 配置每个任务奖励的 [Ageing](//github.com/sotapmc/Ageing) 经验值。
- ...

同时，你还可以通过改变配置文件中不同的项目，来搭配出与众不同的任务模式。关于这一点，可查看我们的[开发文档](//book.sotap.org/#/missiontap/index)。

## 版本适配

采用 `Spigot-API 1.16.1-R0.1-SNAPSHOT` 开发。由于没有动用特殊的 API，理论上支持所有 `1.13` 以上的服务器运行（未测试）。推荐使用 `1.16.1` 运行。

## 鸣谢

- Sapherise，提供主要创意
- 内群里的伙伴们

## 协议

MIT
