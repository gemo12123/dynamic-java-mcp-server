# dynamic-java-mcp-server

`dynamic-java-mcp-server` 是一个把传统 HTTP 接口动态接入 MCP 体系的 Java 项目。

它的核心目标不是要求业务系统重写为 MCP Native 服务，而是尽量复用已有的 Spring MVC 接口能力，让老项目只通过少量注解和少量配置，就能把现有 HTTP 接口暴露为 MCP 工具。

这套方案特别适合这样的场景：

- 业务系统已经有大量稳定的 HTTP 接口
- 项目仍运行在 JDK 8
- 不希望为了接入 MCP 大规模改造服务结构
- 希望先低成本把一部分能力开放给大模型或 Agent 使用

一句话概括：

> 这是一个面向基于 HTTP 接口的存量 Java 项目的 MCP 适配方案，重点价值是让 JDK 8 的 Spring MVC 老项目通过 SDK 低成本接入 MCP。

## 项目解决什么问题

很多老项目已经沉淀了大量可复用的业务接口，但这些接口通常只是提供给前端、内部服务或网关使用，并不能直接被 MCP Client 识别为工具。

这个项目做的事情是：

1. 在业务项目内扫描已有 Spring MVC 接口
2. 把标注过的接口提取为工具定义
3. 将工具定义和服务地址上报到动态 MCP Server
4. 由动态 MCP Server 为每个业务模块动态生成对应的 MCP Server
5. 当 MCP Client 调用工具时，再由动态 MCP Server 反向调用原始 HTTP 接口

这样一来，原本“只能通过 HTTP 访问”的老接口，就变成了“可以通过 MCP 工具调用”的能力。

## 核心思路

项目采用“业务模块 + 注册 SDK + 动态 MCP Server”的模式：

- 业务模块继续提供原有 HTTP 接口，不要求改写业务逻辑
- `mcp-register-sdk` 在业务项目启动时扫描带注解的方法，自动提取工具信息
- 业务模块把自己的模块信息、工具信息、服务地址定时上报给动态 MCP Server
- 动态 MCP Server 根据上报内容动态创建 MCP Server，并把工具调用转发为 HTTP 请求

最终效果是：

- 一个动态 MCP Server 进程可以承载多个业务模块的 MCP 能力
- 业务项目保留原有 HTTP 接口形态
- MCP 侧看到的是标准工具

## 适用对象

这个仓库里最值得关注的是 `mcp-register-sdk`。

它是给 JDK 8 版本的 Spring MVC Web 项目使用的接入 SDK，目标用户通常是：

- 已经有 Spring MVC / Spring Boot Web 老项目
- 接口已经存在，不希望重复开发 MCP 工具层
- 希望用最小改动完成 MCP 接入

从当前代码实现看：

- `common`、`mcp-register-sdk`、`example-project` 使用 Java 8 编译
- `mcp-register-sdk` 基于 Spring Boot 2.5.x 的自动配置方式实现接入
- 示例业务项目 `example-project` 展示了一个 JDK 8 服务如何上报工具
- 动态 MCP Server 模块 `webmvc-dynamic-mcp-server` 是独立的服务端实现，用来接收注册并对外提供 MCP 能力

因此，更准确地说：

> SDK 面向 JDK 8 的 Spring MVC 业务项目；动态 MCP Server 作为独立中间层，负责把这些项目注册进 MCP 体系。

## 仓库结构

```text
dynamic-java-mcp-server
├─ common
│  └─ 公共定义，包含模块、服务实例、工具包装等基础模型
├─ mcp-register-sdk
│  └─ 业务项目接入 SDK，负责扫描注解、生成工具定义、定时注册
├─ webmvc-dynamic-mcp-server
│  └─ 动态 MCP Server，负责接收注册、生成 MCP Server、转发 HTTP 调用
└─ example-project
   └─ 示例业务项目，演示如何把现有 HTTP 接口注册为 MCP 工具
```

## 工作流程

整体链路如下：

1. 业务项目引入 `mcp-register-sdk`
2. 在启动类上增加 `@EnableDynamicMcpRegister`
3. 在已有 Spring MVC 接口方法上增加 `@Tool`
4. SDK 启动后扫描工具定义，并生成输入参数 Schema
5. SDK 将模块信息、工具信息、服务实例信息上报到动态 MCP Server
6. 动态 MCP Server 为每个模块动态构建 MCP Server 和对应路由
7. MCP Client 调用工具时，动态 MCP Server 再通过 HTTP 调用原始业务接口

这意味着：

- 业务系统仍然只维护一套 HTTP 业务实现
- MCP 工具层由框架自动生成
- 老项目接入成本主要集中在“增加注解 + 增加配置”

## 当前实现的关键能力

基于当前代码，已经具备这些能力：

- 基于注解扫描 Spring MVC 接口并提取工具定义
- 自动生成工具输入 Schema
- 支持按模块组织工具
- 支持将工具参数映射到路径参数
- 支持结构化响应拆包，返回真正业务数据
- 支持业务模块启动时注册、之后按定时任务持续上报
- 支持动态注册多个模块
- 支持基于版本号的模块升级判断
- 支持将 MCP 工具调用转发为 GET、POST、PUT、DELETE HTTP 请求
- 支持在单个动态服务进程中承载多个 MCP Server

## 为什么它适合老项目

对于存量系统，最大的成本通常不是“实现能力”，而是“改造成本”。

这个项目的价值就在于尽量不碰已有业务逻辑：

- 原有 Controller 方法可以继续保留
- 原有 URL 可以继续保留
- 原有返回结构基本可以保留
- 只需要在需要暴露的接口上增加少量注解
- 由 SDK 自动完成工具描述、参数定义和注册上报

因此它不是“重新开发一套 MCP 服务”，而是“给既有 HTTP 系统补上一层 MCP 接入能力”。

## 最小接入方式

下面是当前代码支持的最小接入思路。

### 1. 在业务项目中启用 SDK

在启动类上增加：

```java
@EnableDynamicMcpRegister
@SpringBootApplication
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
```

### 2. 在已有 HTTP 接口上增加 `@Tool`

```java
@RestController
public class ExampleController {

    @GetMapping("/hello-world")
    @Tool(name = "helloWorld")
    public String helloWorld() {
        return "hello world!";
    }
}
```

这样，这个接口除了继续作为普通 HTTP 接口使用之外，也会被 SDK 识别为一个 MCP 工具。

### 3. 配置模块信息和注册中心地址

示例项目中的配置如下：

```properties
server.port=8888

dynamic.mcp.register.enabled=true
dynamic.mcp.register.module-id=example-project
dynamic.mcp.register.module-name=example-project
dynamic.mcp.register.module-description=this is a example project
dynamic.mcp.register.module-version=0.0.1
dynamic.mcp.register.report-configuration.address-source=fixed
dynamic.mcp.register.report-configuration.fixed-url-prefix=http://127.0.0.1:8080
```

其中：

- `module-id` 是模块唯一标识
- `module-name` 是模块名称
- `module-version` 用于动态服务端识别版本变化
- `fixed-url-prefix` 是动态 MCP Server 的注册入口地址前缀

业务项目启动后，会向：

```text
{fixed-url-prefix}/dynamic-mcp-server/mcp/register
```

发起注册请求。

## 动态 MCP Server 做了什么

`webmvc-dynamic-mcp-server` 是整个方案中的“动态桥接层”。

它主要负责：

- 接收业务模块上报的工具定义和服务地址
- 为每个模块动态创建 MCP Server
- 将多个模块的路由统一挂载到一个服务进程中
- 在工具被调用时，把请求转发到对应业务模块的 HTTP 接口
- 根据模块版本信息处理升级和覆盖

从职责上看，它更像是一个：

- MCP 网关
- 动态注册中心
- HTTP 到 MCP 的转换层

对于 MCP Client 来说，连接方式也比较直接。

当前实现基于 SSE 形式暴露每个模块的 MCP Server 端点，路由格式由动态服务端按模块 ID 动态生成：

```text
SSE endpoint:        /dynamic-mcp/sse/{moduleId}
Message endpoint:    /dynamic-mcp/mcp/message/{moduleId}
```

如果 `webmvc-dynamic-mcp-server` 部署在 `http://127.0.0.1:8080`，某个模块的 `moduleId` 为 `example-project`，那么 MCP Client 连接时可使用：

```text
http://127.0.0.1:8080/dynamic-mcp/sse/example-project
```

也就是说，动态 MCP Server 并不是只提供一个统一的总入口，而是会为每个已注册模块暴露一个独立的 MCP 连接地址。MCP Client 只需要按目标模块选择对应的 SSE 地址连接，即可看到该模块当前注册的工具集合。

## 注解说明

### `@EnableDynamicMcpRegister`

启用 SDK 自动配置，开启工具扫描与定时上报。

### `@Tool`

把一个 Spring MVC 方法声明为 MCP 工具。

当前支持的核心属性包括：

- `name`：工具名
- `description`：工具描述
- `module`：工具所属模块
- `removeStructResponse`：是否拆掉外层响应结构，仅保留业务数据

### `@ToolParam`

用于描述参数元信息，例如：

- 参数说明
- 是否必填
- 是否忽略
- 是否映射为路径参数

### `@PathParam`

用于把方法参数映射到 URL 路径占位符。

### `@StructResponse` / `@StatusField` / `@DataField`

用于描述结构化返回值，便于在 MCP 返回时去掉外层包装。

典型场景是老项目返回统一结构：

```json
{
  "code": "200",
  "data": {
    "name": "test"
  }
}
```

通过这些注解，可以让 MCP 最终只返回 `data` 部分，减少模型侧处理负担。

## 示例项目说明

`example-project` 是一个最小示例。

它演示了：

- 一个 JDK 8 的 Spring Web 服务如何启用注册 SDK
- 一个普通 `GET /hello-world` 接口如何通过 `@Tool` 暴露为工具
- 如何通过配置把自己注册到动态 MCP Server

如果你想理解这套方案最基本的接入方式，建议先从这个模块开始看。

## 适合的落地方式

比较推荐的落地路径是：

1. 先部署 `webmvc-dynamic-mcp-server`
2. 选择一个已有 JDK 8 Spring MVC 业务项目引入 `mcp-register-sdk`
3. 只挑选少量稳定接口增加 `@Tool`
4. 验证注册是否成功
5. 再逐步扩大可暴露的工具范围

这样做的好处是：

- 试点成本低
- 对老系统侵入小
- 出问题时回退简单
- 能快速验证哪些接口适合暴露为 MCP 工具

## 当前实现边界

为了避免误解，下面这些边界最好提前知道：

- 当前注册上报地址源只实现了 `fixed` 模式
- 当前动态服务端默认实现是 CLI / 内存管理方式
- 注册接口的请求格式校验还比较基础
- 工具调用本质上仍依赖后端 HTTP 接口的可用性
- 更复杂的服务发现、持久化管理、多实例调度能力当前仓库里还没有完整展开

所以这个项目当前更适合作为：

- 老项目接入 MCP 的基础能力层
- 一个可验证的动态注册方案
- 后续继续扩展注册中心、路由策略和治理能力的起点

## 总结

`dynamic-java-mcp-server` 的核心贡献，不是重新定义业务系统，而是让已有基于 HTTP 接口的老项目能够以较低改造成本进入 MCP 体系。

如果你的项目已经有成熟的 Spring MVC 接口，尤其仍运行在 JDK 8 上，那么这套方案的价值就在于：

- 不要求重写业务
- 不要求把接口改造成全新的工具实现
- 只需要增加少量注解和配置
- 就可以把现有能力逐步转化为 MCP 工具

对于存量系统来说，这是一条更现实、也更容易落地的接入路径。