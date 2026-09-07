# GenId 迭代计划

> 目标：把 GenId 建设成一个开箱即用、可插拔、高可用的分布式 ID 生成组件。
> 参考对标：美团 Leaf、百度 UidGenerator、滴滴 Tinyid、MyBatis-Plus IdentifierGenerator。

---

## 一、现状盘点（2025 当前）

### 已完成
| 模块 | 内容 | 状态 |
|---|---|---|
| 接口层 | `StringIdGenerator` / `IdGenerator` / `IdConverter` | ✅ 可用 |
| JVM 生成器 | `AtomicLongGenerator`、`UuidGenerator`(v4)、`UuidV7Generator` | ✅ 可用（仅本机语义） |
| 工具类 | `UuidV7`（时间戳前置 + 版本位/变体位） | ✅ 可用 |
| 转换器 | `SimpleIdConverter`（long ↔ String） | ✅ 可用 |
| 注册枚举 | `IdGeneratorEnum` / `IdConverterEnum` | ⚠️ 只注册了 UUID，其余未接入 |

### 未完成 / 问题清单
| # | 问题 | 严重度 |
|---|---|---|
| 1 | `SnowflakeGenerator.generateId()` 返回 0，**核心分布式算法未实现**（无位分配、无 workerId、无时钟回拨处理） | 🔴 阻塞 |
| 2 | 零测试：`UuidV7` 用 `main` 方法当测试，正式 JUnit 测试全部被注释 | 🔴 |
| 3 | UUID 类生成器对 `generateId()` 直接抛异常，接口契约没有清晰表达"是否支持 long" | 🟡 设计 |
| 4 | 无 Spring Boot Starter、无 ORM（MyBatis-Plus 等）适配，作为"组件"不可开箱即用 | 🟡 |
| 5 | 缺少分布式主流模式：号段模式（DB）、Redis 自增 | 🟡 |
| 6 | workerId 无法自动分配（当前连手动配置都没有） | 🟡 |
| 7 | `AtomicLongGenerator` 重启归零，未持久化，仅能用于单机测试 | 🟢 文档说明即可 |
| 8 | 依赖治理：`hutool-all`（全量包）不适合做库依赖，应换 `hutool-core` 或自实现；lombok 缺 `provided` scope | 🟢 |
| 9 | `.gitignore` 不完整（IDE 文件、`.pi/`、`.classpath` 等被提交/待提交） | 🟢 |
| 10 | `UuidV7` 同毫秒内随机部分无单调性（可能加大 B+ 树页分裂）；`Holder` 死代码 | 🟢 增强 |
| 11 | 无 CI、无基准测试、无可观测性（指标/健康检查）、README 只有一行 | 🟢 |

---

## 二、目标形态（模块规划）

```
GenId (pom)
├── genid-core                  # SPI 接口 + 纯 JVM 实现：Snowflake / UUIDv7 / AtomicLong
├── genid-segment               # DB 号段模式（双 Buffer）          [迭代二]
├── genid-redis                 # Redis INCRBY 模式               [迭代二]
├── genid-zookeeper             # 可选：workerId 自动分配          [迭代二]
├── genid-spring-boot-starter   # 自动装配 + MyBatis-Plus 适配     [迭代三]
└── genid-example               # 示例工程                        [迭代三]
```

核心接口规划（迭代一重构）：

```java
public interface StringIdGenerator { String generateStringId(); }

public interface LongIdGenerator extends StringIdGenerator { long generateId(); }

public interface IdGeneratorFactory {          // SPI，ServiceLoader 发现
    StringIdGenerator getStringIdGenerator(IdType type);
    Optional<LongIdGenerator> getLongIdGenerator(IdType type);
}
```

---

## 三、迭代计划

### 迭代一：核心能力闭环（Snowflake + 测试基线）
**周期建议：1~1.5 周**

| 任务 | 说明 |
|---|---|
| 1. SnowflakeGenerator 完整实现 | 标准位分配：1 符号位 + 41 时间戳（支持自定义 epoch，默认如 2024-01-01）+ 10 workerId（5 机房 + 5 机器）+ 12 序列号；毫秒内序列溢出自旋等待下一毫秒 |
| 2. 时钟回拨处理 | 小回拨（≤ 阈值如 5ms）自旋等待；大回拨抛异常并暴露钩子，预留扩展位方案（参考 UidGenerator） |
| 3. WorkerId 手动配置 | 构造参数 / 环境变量注入，启动时范围校验（0~1023），冲突检测提示 |
| 4. 接口重构 | 拆分 `LongIdGenerator`，UUID 系列只实现 String 语义，消除抛异常契约 |
| 5. 补齐枚举注册 | `IdGeneratorEnum` 接入 Snowflake / UUIDv7 / AtomicLong |
| 6. 测试基线 | 引入 JUnit5 + AssertJ；迁移 `UuidV7` 的 main/注释测试；补：雪花并发唯一性（多线程百万级无重复）、单调性、回拨用例、转换器用例 |
| 7. 工程卫生 | `.gitignore` 补全（IDE/.pi/.classpath）；`hutool-all` → `hutool-core`；lombok `provided`；清理死代码 |

**验收标准**
- `mvn test` 全绿；单机雪花多线程压测 400w ID 无重复
- 同一毫秒回拨场景不产生重复 ID

---

### 迭代二：分布式扩展模式（号段 + Redis + workerId 自动分配）
**周期建议：2 周**

| 任务 | 说明 |
|---|---|
| 1. 号段模式 `genid-segment` | DB 表 `id_segment(biz_tag, max_id, step, version)`；乐观锁取号；**双 Buffer 异步预加载**（使用超 90% 触发下一号段加载，切换无锁） |
| 2. Redis 模式 `genid-redis` | `INCRBY genid:{bizTag} step` 批量取号 + 本地缓冲，降低网络往返 |
| 3. workerId 自动分配 | 首选 Redis 租约（SETNX + TTL 续约）；预留 ZooKeeper 临时顺序节点实现位 |
| 4. 集成测试 | Testcontainers（MySQL + Redis）跑号段/Redis 模式；模拟 DB 抖动下的 buffer 降级 |
| 5. 业务隔离 | `bizTag` 维度的号段隔离，不同业务互不影响 |

**验收标准**
- 双 buffer 下生成路径无 DB/Redis 网络调用，命中率指标可观测
- 单实例 kill -9 重启后 ID 不回退、不重复

---

### 迭代三：生态集成（Spring Boot Starter + ORM 适配）
**周期建议：1.5 周**

| 任务 | 说明 |
|---|---|
| 1. `genid-spring-boot-starter` | `@ConfigurationProperties`（`genid.type` / `genid.worker-id` / `genid.data-center-id` / db、redis 连接等）+ 自动装配 + 条件化装配（有 Redis 依赖才暴露 Redis 生成器） |
| 2. 健康检查 | `HealthIndicator`：雪花时钟偏移、号段 buffer 状态 |
| 3. MyBatis-Plus 适配 | 实现 `IdentifierGenerator`，注解 `@TableId` 零改造接入 |
| 4. SPI 工厂 | `IdGeneratorFactory` + `ServiceLoader` 注册，非 Spring 环境可用 |
| 5. 示例工程 `genid-example` | 三种模式的最小可运行示例 |

**验收标准**
- 用户引入一个 starter 依赖 + 3 行配置即可切换生成模式
- MyBatis-Plus 实体主键自动填充示例可运行

---

### 迭代四：质量、可观测性与 1.0 发布
**周期建议：1~2 周**

| 任务 | 说明 |
|---|---|
| 1. JMH 基准 | Snowflake / UUIDv7 / 号段(本地命中) / Redis 的吞吐对比报告，写入文档 |
| 2. Micrometer 指标 | 生成速率、时钟回拨次数、buffer 预加载命中率、Redis/DB 耗时 |
| 3. CI/CD | GitHub Actions：JDK21 构建 + 单测 + Jacoco 覆盖率报告 |
| 4. 文档 | README 重写（选型指南：雪花 vs 号段 vs UUIDv7）、各模式配置文档、性能数据 |
| 5. 发布 | 打 tag `v1.0.0`；可选：发布到 Maven Central（Sonatype） |

---

## 四、立刻可以做的 Quick Wins（半天内）
1. 补全 `.gitignore`（`.idea/`、`*.iml`、`.classpath`、`.project`、`.settings/`、`.factorypath`、`.pi/`）
2. `hutool-all` → `hutool-core`，lombok 加 `<scope>provided</scope>`
3. 把 `UuidV7.main` 及注释里的测试迁移成 `src/test/java` 下的正式 JUnit 测试
4. 删除 `UuidV7.Holder` 死代码

## 五、风险与决策点
- **时钟回拨策略**（等待 / 抛异常 / 扩展位）需要在迭代一确定，影响位分配设计
- **workerId 自动分配**优先做 Redis 租约还是 ZK，取决于目标用户的部署形态，建议迭代二开始前确认
- **是否自研 hutool 依赖**：库项目长期看建议去掉 hutool（UUIDv7 已自实现，`Assert`/`Random` JDK 均可替代），迭代一顺手评估
