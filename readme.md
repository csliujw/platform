# 改造计划
## 单体系统改造
- √ 修改轮询规则，可根据情况注入不同的轮询规则
- √ 使用策略模式修改匹配规则
- x 抽取 Redis 工具类，完成缓存穿透，缓存击穿的通用方法
- x 使用 mq 完成输赢信息的异步写入，避免大量游戏对局压垮数据库
- x 使用 feign 进行远程调用
## 分布式系统改造
- x 使用 SpringCloud 改造服务。