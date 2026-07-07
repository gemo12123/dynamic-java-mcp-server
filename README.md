# dynamic-java-mcp-server

`dynamic-java-mcp-server` 是一个把传统 Spring MVC HTTP 接口动态接入 MCP 体系的 Java 项目。

它的目标不是要求业务系统重写为 MCP Native 服务，而是尽量复用已有的 Controller 接口、URL 和业务逻辑，让存量 Java Web 项目通过少量注解和配置，把现有 HTTP 能力暴露为 MCP 工具。

一句话概括：

> 这是一个面向存量 Spring MVC / Spring Boot Web 项目的 MCP 适配方案，重点价值是让老系统低成本接入 MCP，而不是重写一套新的工具服务。

## 适合什么场景

这套方案尤其适合下面几类项目：

- 已经沉淀了大量稳定的 HTTP 接口
- 希望把一部分接口能力开放给大模型或 Agent 使用
- 不希望为了接入 MCP 大规模改造原有服务结构
- 业务项目仍停留在 JDK 8，但又希望逐步接入新的 MCP 生态

如果你的系统已经有成熟的 Spring MVC 接口，这个项目的价值主要在于：

- 原有业务逻辑不用重写
- 原有 HTTP 接口可以继续保留
- 只需要在目标接口上增加少量注解
- MCP 工具描述、参数 Schema、注册上报由框架自动处理

## 它解决的问题

很多老项目已经沉淀了大量可复用的业务接口，但这些接口通常只服务于前端、内部系统或网关，不能直接被 MCP Client 识别为工具。

这个项目做的事情是：

1. 在业务项目内扫描已有 Spring MVC 接口
2. 把标注过的接口提取为工具定义
3. 将工具定义和服务地址上报到动态 MCP Server
4. 由动态 MCP Server 为每个业务模块动态生成独立的 MCP Server 端点
5. 当 MCP Client 调用工具时，再由动态 MCP Server 反向调用原始 HTTP 接口

这样一来，原本“只能通过 HTTP 调用”的老接口，就能变成“可以通过 MCP 工具调用”的能力。

## 整体架构

项目采用“业务模块 + 注册 SDK + 动态 MCP Server”的模式：

- 业务模块继续提供原有 HTTP 接口，不要求改写业务逻辑
- `mcp-register-sdk` 在业务项目启动时扫描带注解的方法，自动提取工具信息并生成输入 Schema
- 业务模块把模块信息、工具信息、服务实例信息定时上报给动态 MCP Server
- `webmvc-dynamic-mcp-server` 根据上报内容动态创建模块级 MCP Server，并把工具调用转发为 HTTP 请求

整体调用链如下：

```text
业务项目 Controller
    │
    ├─ @Tool / @ToolParam / @StructResponse 等注解
    │
    ▼
mcp-register-sdk
    │ 扫描工具定义、生成参数 Schema、定时上报注册信息
    ▼
webmvc-dynamic-mcp-server
    │ 为每个 moduleId 动态创建独立 SSE MCP 端点
    ▼
MCP Client
    │ 调用工具
    ▼
动态服务端反向调用原始 HTTP 接口
```

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

## 运行环境要求

这一点非常关键：仓库内不同模块的运行时要求并不相同。

| 模块 | 作用 | Java 版本 | 说明 |
| --- | --- | --- | --- |
| `common` | 公共模型定义 | 8 | 被 SDK 和示例项目复用 |
| `mcp-register-sdk` | 业务项目接入 SDK | 8 | 面向存量 Spring MVC / Spring Boot Web 项目 |
| `example-project` | 最小示例业务项目 | 8 | 演示如何注册工具 |
| `webmvc-dynamic-mcp-server` | 动态 MCP 服务端 | 25 | 独立运行的桥接层，基于 Spring Boot 3.4 + Spring AI MCP |

也就是说：

- `SDK` 侧是为 JDK 8 业务项目准备的
- `动态 MCP Server` 本身不是 JDK 8 服务，它需要单独运行在更高版本的 Java 环境上

如果你第一次阅读这个仓库，建议先把它理解成一个“跨代际桥接方案”：

- 老业务系统继续运行在 JDK 8
- 动态 MCP 接入层运行在较新的 Java / Spring AI 环境上

## 当前实现的核心能力

基于当前源码，已经具备这些能力：

- 基于注解扫描 Spring MVC 接口并提取工具定义
- 自动生成工具输入参数的 JSON Schema
- 支持按模块组织工具
- 支持将参数映射到路径占位符
- 支持结构化响应拆包，仅返回真正业务数据
- 支持业务模块启动时注册，并按定时任务持续上报
- 支持在单个动态服务进程中承载多个模块的 MCP 能力
- 支持基于模块版本号的升级判断
- 支持将工具调用转发为 `GET`、`POST`、`PUT`、`DELETE` HTTP 请求
- 支持为每个模块暴露独立的 SSE MCP 端点

## 快速开始

下面用仓库里的 `webmvc-dynamic-mcp-server` 和 `example-project` 说明最小联通流程。

### 1. 准备运行环境

至少需要准备两套 Java 环境：

- Java 25：用于启动 `webmvc-dynamic-mcp-server`
- Java 8：用于启动 `example-project`

同时需要 Maven，并且本地 `toolchains` 配置能提供对应版本的 JDK，因为各模块 `pom.xml` 已显式声明了不同的 `jdkToolchain`。

### 2. 先启动动态 MCP Server

动态服务端模块：`webmvc-dynamic-mcp-server`

当前默认配置位于：

- [webmvc-dynamic-mcp-server/src/main/resources/application.properties](E:/my_project/dynamic-java-mcp-server/webmvc-dynamic-mcp-server/src/main/resources/application.properties)

默认内容如下：

```properties
spring.ai.mcp.server.enabled=false

dynamic-mcp-server.mode=cli
```

这表示：

- 当前使用的是 `cli` 模式
- 注册后的模块路由由当前进程在内存中维护
- 未看到额外端口配置时，Spring Boot 默认使用 `8080`

你可以按自己的习惯通过 IDE 或 Maven 启动 `org.mytest.test.WebmvcApplication`。

启动成功后，注册入口默认应为：

```text
http://127.0.0.1:8080/dynamic-mcp-server/mcp/register
```

### 3. 再启动示例业务项目

示例业务模块：`example-project`

它的启动类已经启用了 SDK：

```java
@EnableDynamicMcpRegister
@SpringBootApplication
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
```

当前示例配置位于：

- [example-project/src/main/resources/application.properties](E:/my_project/dynamic-java-mcp-server/example-project/src/main/resources/application.properties)

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

这意味着：

- 示例业务服务自身运行在 `8888`
- 它会把自己注册到 `http://127.0.0.1:8080`
- 注册请求地址最终为：

```text
http://127.0.0.1:8080/dynamic-mcp-server/mcp/register
```

### 4. 验证注册是否成功

`example-project` 当前按能力拆分为 4 组样例：

- `BasicHttpExampleController`
  - `GET /examples/basic/hello`
  - `POST /examples/basic/echo`
  - `PUT /examples/basic/resources`
  - `DELETE /examples/basic/resources?resourceId=...`
- `PathParamExampleController`
  - `GET /examples/path/resources/{resourceId}`
  - `GET /examples/path/teams/{teamId}/resources/{resourceId}`
- `StructResponseExampleController`
  - `GET /examples/struct/unwrapped`
  - `GET /examples/struct/wrapped`
- `ComprehensiveExampleController`
  - `POST /examples/comprehensive/projects/{projectId}/tasks/{taskId}/submit`

对应源码见：

- [example-project/src/main/java/org/mytest/test/controller/BasicHttpExampleController.java](E:/my_project/dynamic-java-mcp-server/example-project/src/main/java/org/mytest/test/controller/BasicHttpExampleController.java)
- [example-project/src/main/java/org/mytest/test/controller/PathParamExampleController.java](E:/my_project/dynamic-java-mcp-server/example-project/src/main/java/org/mytest/test/controller/PathParamExampleController.java)
- [example-project/src/main/java/org/mytest/test/controller/StructResponseExampleController.java](E:/my_project/dynamic-java-mcp-server/example-project/src/main/java/org/mytest/test/controller/StructResponseExampleController.java)
- [example-project/src/main/java/org/mytest/test/controller/ComprehensiveExampleController.java](E:/my_project/dynamic-java-mcp-server/example-project/src/main/java/org/mytest/test/controller/ComprehensiveExampleController.java)

如果示例项目启动后完成注册，动态服务端会按 `moduleId=example-project` 为它生成独立的 MCP SSE 端点：

```text
SSE endpoint:     http://127.0.0.1:8080/dynamic-mcp-server/sse/example-project
Message endpoint: http://127.0.0.1:8080/dynamic-mcp-server/mcp/message/example-project
```

到这一步，MCP Client 就可以连接该 SSE 地址，看到 `example-project` 当前注册出来的工具集合。

### 5. 验证原始 HTTP 服务是否可用

示例项目本身仍然保留原有 HTTP 接口能力，可以直接访问下面几个代表性地址：

```text
GET    http://127.0.0.1:8888/examples/basic/hello
GET    http://127.0.0.1:8888/examples/path/resources/alpha
GET    http://127.0.0.1:8888/examples/struct/wrapped
POST   http://127.0.0.1:8888/examples/comprehensive/projects/demo/tasks/T-100/submit
```

例如访问：

```text
GET http://127.0.0.1:8888/examples/basic/hello
```

预期返回：

```text
hello dynamic mcp
```

`example-project` 现在覆盖的能力包括：

- 基础工具暴露与 `GET` / `POST` / `PUT` / `DELETE` HTTP 方法转发
- `@ToolParam` + `@PathParam` 的路径占位符映射
- `@StructResponse`、`@StatusField`、`@DataField` 的结构化响应拆包与保留包装
- 路径参数、请求体、结构化响应组合在同一个工具中的综合样例

这正是这个方案的核心价值：

- 业务系统继续维护原有 HTTP 接口
- MCP 工具层由桥接方案自动生成
- 不需要重复开发一套新的工具服务

## 最小接入方式

如果你要把自己的 Spring MVC 项目接入这套方案，最小步骤如下。

### 1. 引入 `mcp-register-sdk`

在业务项目中引入 SDK 模块。

### 2. 在启动类上启用注册能力

```java
@EnableDynamicMcpRegister
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 在目标接口上增加 `@Tool`

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

### 4. 配置模块信息和注册地址

```properties
dynamic.mcp.register.enabled=true
dynamic.mcp.register.module-id=your-module
dynamic.mcp.register.module-name=your-module
dynamic.mcp.register.module-description=your module description
dynamic.mcp.register.module-version=1.0.0
dynamic.mcp.register.report-configuration.address-source=fixed
dynamic.mcp.register.report-configuration.fixed-url-prefix=http://127.0.0.1:8080
```

## 注解说明

下面只列出当前接入最关键的注解。

### `@EnableDynamicMcpRegister`

启用 SDK 自动配置，开启工具扫描与定时上报。

### `@Tool`

把一个 Spring MVC 方法声明为 MCP 工具。

当前支持的核心属性包括：

- `name`：工具名，不填写时默认使用方法名
- `description`：工具描述
- `module`：工具所属模块
- `removeStructResponse`：是否拆掉外层包装，仅返回真正业务数据

### `@ToolParam`

用于描述参数元信息，例如：

- 参数说明
- 是否必填
- 是否忽略
- 是否映射为路径参数

### `@PathParam`

用于把方法参数映射到 URL 路径占位符。

### `@StructResponse` / `@StatusField` / `@DataField`

用于描述统一响应包装，便于在 MCP 返回时去掉外层结构。

典型场景如下：

```json
{
  "code": "200",
  "data": {
    "name": "test"
  }
}
```

如果返回类型上标记了结构化响应注解，且状态字段满足预期，MCP 侧最终可以只返回 `data` 部分，减少模型侧处理负担。

## 动态 MCP Server 做了什么

`webmvc-dynamic-mcp-server` 是整个方案中的动态桥接层，主要负责：

- 接收业务模块上报的工具定义和服务地址
- 为每个模块动态创建 MCP Server
- 将多个模块的路由统一挂载到一个服务进程中
- 在工具被调用时，把请求转发到对应业务模块的 HTTP 接口
- 根据模块版本信息处理升级和覆盖

从职责上看，它更像是一个：

- MCP 网关
- 动态注册中心
- HTTP 到 MCP 的转换层

## 当前实现边界

为了避免对项目能力产生误解，下面这些限制建议提前知道：

- 当前注册上报地址源只实现了 `fixed` 模式
- 当前动态服务端默认实现是 `cli` / 内存管理方式
- 注册接口对请求内容的格式校验还比较基础
- 工具调用本质上仍然依赖后端 HTTP 服务的可用性
- 当前没有看到完整展开的持久化注册中心、多实例调度、复杂治理能力
- 动态服务端和业务 SDK 的 Java 运行时不同，部署时需要明确区分

因此，这个仓库当前更适合作为：

- 老项目接入 MCP 的基础能力层
- 一个可验证的动态注册方案
- 后续继续扩展服务发现、路由策略和治理能力的起点

## 推荐阅读顺序

如果你是第一次接触这个项目，建议按下面顺序阅读：

1. 先看本 README，理解项目定位与整体链路
2. 再看 [example-project](E:/my_project/dynamic-java-mcp-server/example-project) 理解最小接入方式
3. 再看 [mcp-register-sdk](E:/my_project/dynamic-java-mcp-server/mcp-register-sdk) 理解注解扫描和注册上报
4. 最后看 [webmvc-dynamic-mcp-server](E:/my_project/dynamic-java-mcp-server/webmvc-dynamic-mcp-server) 理解动态路由与 HTTP 转发

## 总结

`dynamic-java-mcp-server` 的核心贡献，不是重新定义业务系统，而是让已有基于 HTTP 接口的存量 Java 项目，能够以较低改造成本进入 MCP 体系。

如果你的项目已经有成熟的 Spring MVC 接口，尤其业务仍运行在 JDK 8 上，那么这套方案的现实价值就在于：

- 不要求重写业务
- 不要求额外开发一套 MCP Native 工具层
- 只需要增加少量注解和配置
- 就可以把现有能力逐步转换为 MCP 工具

对于存量系统来说，这是一条更现实、也更容易试点落地的接入路径。
