> [!IMPORTANT]
> # ⚠️ 本项目完全由 AI 生成
>
> **本仓库中的全部内容——包括源代码、注释、文档、设计与架构——均由人工智能（AI）自动生成，没有人工逐行编写或审计。**
>
> - 代码由 AI 依据需求直接产出，未经完整的人工安全审查与真机验证；
> - 可能存在逻辑缺陷、兼容问题、安全与隐私风险，**请勿直接用于生产环境或日常主力设备**；
> - 本项目涉及对系统进程（蓝牙、系统 UI、第三方 App）的 Hook 与注入，使用风险由使用者自行承担；
> - 作者不对因使用本项目导致的任何损失、设备故障或数据问题负责。
>
> 请将其视为一份**由 AI 撰写的技术演示 / 学习参考**，而非可靠的正式软件。

---

# flyme_tws

一个基于 **LSPosed 现代 Xposed API（libxposed）** 的 Android 模块（applicationId `github.forest.tool.tws.io`），用于让第三方品牌的 TWS 真无线耳机（vivo / iQOO、原道 NiceHCK、华为 FreeBuds / FreeClip / FreeLace）在 **魅族（Flyme）系统**上获得接近"官方耳机"的体验：控制中心卡片、快连弹窗、左右耳与充电仓电量、降噪模式切换，以及点击"更多设置"跳转到对应品牌的官方 App。

> **实现说明**：模块入口是 `github.forest.tool.tws.io.ModuleMain`，继承真实的
> `io.github.libxposed.api.XposedModule`，由 `META-INF/xposed/java_init.list` +
> `module.prop` + `scope.list` 声明，LSPosed 在作用域进程内实例化并回调
> `onPackageReady` 完成 hook 注册。编译期以官方 `io.github.libxposed:api:102.0.0`
> 的 `classes.jar` 作 `compileOnly`（运行时由框架提供，**不打进 APK**）；launcher
> 通过官方 `service` 库绑定 `XposedService` 读写 remote preferences，使启用开关端到端可用。
> 工作流内设有断言，校验入口元数据、包名以及"真实 API 未被打包"。

## 它做了什么

魅族系统只对自家"官方耳机"提供完整的配对弹窗、电量展示和降噪控制。本模块通过跨进程 Hook + 系统广播，把这套体验"移植"到第三方耳机上：

- **蓝牙进程**：识别设备品牌、选择对应私有协议，通过经典蓝牙 RFCOMM 常驻读取左右耳 / 充电仓电量，并对外广播；同时把 App 发来的降噪请求翻译成品牌指令写回耳机。
- **魅族耳机 App / 魅族智能家居 App**：消费广播，向系统注入"伪装的官方设备"与电量数据，放行扫描与型号白名单，并在用户切换降噪时回发指令。
- **系统 UI / 控制中心**：把卡片"更多设置"指向对应品牌官方 App（未装则兜底系统蓝牙详情页），微调设备中心间距，并支持替换弹窗 / 卡片中的耳机图标。

## 支持的品牌与协议

| 品牌 | 私有协议 | 传输 |
| --- | --- | --- |
| vivo / iQOO | GAIA-shaped 帧（按机型细分 v3 / v4 字节差异） | RFCOMM |
| 原道 NiceHCK / OriG in | `0x4E` 二进制帧 | RFCOMM SPP |
| 华为 FreeBuds / FreeClip / FreeLace | `0x5A` + CRC16/XMODEM 帧 | 标准 SPP |

新增品牌只需在 `EarbudCatalog.kt` 登记一条 `CatalogEntry`（官方 App 包名 + 设备名匹配规则），并在 `BluetoothHook` 中补一条协议路由。

## 架构：四个进程，靠广播串起来

三个品牌无关的广播动作定义在 `TwsActions.kt`，作为进程间契约：

| 动作 | 方向 | 数据 | 含义 |
| --- | --- | --- | --- |
| `EARBUD_BATTERY` | 蓝牙 → App | `left` / `right` / `box` | 电量上报（%） |
| `EARBUD_DEVICE_INFO` | 蓝牙 → App / UI | `name` / `mac` | 真实设备名与 MAC |
| `EARBUD_SET_ANC` | App → 蓝牙 | `mode`（1 关 / 2 降噪 / 3 通透） | 降噪切换请求 |

## 目录结构

```
app/src/main/
├── java/github/forest/tool/tws/io/
│   ├── ModuleMain.kt          ← 真实 XposedModule 入口（onPackageReady 分发）
│   ├── ModuleSettings.kt      remote preferences 开关（默认启用）
│   ├── App.kt / MainActivity.kt  launcher：通过 XposedService 写开关
│   ├── utils/XLog.kt          走真实 module.log
│   └── hook/TWS/              10 个原始 TWS 源码（协议 / 广播 / hook）
│       ├── TwsActions.kt / EarbudCatalog.kt / BluetoothHook.kt
│       ├── GaiaProtocol.kt / NiceHckProtocol.kt / HuaweiProtocol.kt
│       ├── MeizuTWS.kt / MeizuiotTWS.kt / EarbudHook.kt / MeizuFastPairPop.kt
├── resources/META-INF/xposed/
│   ├── java_init.list         ← LSPosed 识别并加载 ModuleMain
│   ├── module.prop            (minApiVersion=101, target=102, staticScope)
│   └── scope.list             4 个目标进程
├── AndroidManifest.xml        App + MainActivity + XposedProvider
└── libs/                     官方 libxposed api/annotation/service/interface classes.jar
```

## 使用前提（简要）

- 已 Root 并刷入支持 **libxposed 现代 API（API 101+）** 的 LSPosed 框架；
- 在 LSPosed 中启用本模块并勾选作用域（`scope.list` 已声明 4 个目标进程）；
- launcher 内的「启用 / 关闭 伪装」开关通过 `XposedService` 写入 remote preferences
  （group `frost_tool_prefs`，key `tws_device_spoof_enabled`，**默认启用**）；
- 需对蓝牙进程、魅族耳机 App、魅族智能家居 App、系统 UI 等目标启用本模块；
- 依赖各厂商私有蓝牙协议，不同机型 / 系统版本可能需要适配，**不保证通用**。

> 本项目仅供学习与技术研究使用，请勿用于违反设备厂商条款或侵犯他人权益的场景。

## 构建与 V3 签名（GitHub Actions）

本项目附带一个可复现的 Android Gradle 工程与 CI 工作流，用于**编译出带 APK 签名方案 v3 的 APK 产物**。

- 工作流文件：[.github/workflows/build-apk.yml](./.github/workflows/build-apk.yml)
- 触发方式：推送到 `main`（或 Actions 页手动 `workflow_dispatch`）即自动激活编译。
- 工具链：JDK 17 · Gradle 8.2 · AGP 8.1.4 · Kotlin 1.9.22 · compileSdk 34 / build-tools 34.0.0。

流程：`:app:assembleRelease` 生成未签名包 → `zipalign -p 4` → 用 `apksigner` 以
`--v3-signing-enabled true --min-sdk-version 28` 签名 → `apksigner verify` 断言
**v3 签名块确实存在** → 上传 `flyme_tws-v3-signed.apk` 为构建产物。

> 说明：APK 签名方案 v3 原生适用于 Android 9（API 28）及以上，因此工程把 `minSdk` 设为 28。
> 此时 `apksigner` 会生成**纯 v3 签名**（验证结果 `v3: true`，`v1/v2: false`）——因为 v3 已覆盖
> 全部受支持版本，v1/v2 被自动判定为冗余而省略。

> 签名密钥：若配置了仓库 Secrets（`SIGNING_KEYSTORE_BASE64` 等）则用固定密钥；否则每次运行生成一份临时 CI 密钥，
> 保证任何情况下都能得到可安装、且经过 v3 签名的产物。**私钥永不入库**。
>
> 编译期依赖的是**官方发布的** `io.github.libxposed:api:102.0.0` / `annotation:1.0.0`
> classes.jar（`compileOnly`，运行时由 LSPosed 提供，**不打进 APK**），以及
> `service:102.0.0` / `interface:102.0.0`（launcher 侧开关，`implementation`）。
> 工作流会断言：入口元数据存在、`applicationId` 正确、且真实 API 类**未**被打包进 APK。

## 开源协议

本项目采用 **GNU Affero General Public License v3.0（AGPL-3.0）** 开源，详见 [LICENSE](./LICENSE)。

依据 AGPL-3.0：你可以自由地运行、复制、修改和分发本软件；若你修改后通过网络向用户提供服务，必须以同等协议公开对应源代码。本软件**不提供任何担保**。

---

> 再次提醒：**本项目完全由 AI 生成**，详见本文件顶部的重要声明。
