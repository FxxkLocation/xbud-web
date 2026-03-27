# 小步点 Web 模拟运行与抓包工具 (Xbud Web App)

## 📖 项目简介
本项目是一个针对于“小步点”App的协议级模拟与逆向研究工具。由于小步点对请求参数进行了本地 `.so` 库层面的加密签名（如 `libsport.so` 和 `libNetSecKit.so`），传统的直接请求无法通过服务器校验。

本项目通过提取原包库文件，借助 **Unidbg (Java)** 框架在后端直接创建一个虚拟的 Android 环境，模拟手机调用底层加密算法。再结合基于 **Python Flask + Leaflet.js** 构建的 Web 可视化面板，让用户可以通过网页端直接实现登录、地图规划选点与路线提交等相关操作。

## ⚙️ 核心功能
1. **本地 SO 库模拟**：内置 Java Unidbg 服务（端口 `5001`），自动加载并桥接原版 64 位 `libsport.so`，提取真实的 `SecretKey` 并处理未来的 AES/MD5 签名请求。
2. **可视化地图轨迹交互**：网页端内嵌高德地图资源（基于 Leaflet），可在网页端直接点击并可视化路径。
3. **Web 端接口接管**：Flask 后端（端口 `5000`）实现参数组装，底层通过 RPC 向 Unidbg 获取加密结果并最终发送至官方服务器。

## 📁 目录结构
```text
xbud_web_app/
├── crypto_unidbg/      # 基于 Java + Unidbg 的加密服务容器
│   ├── pom.xml         # Maven 依赖配置文件
│   └── src/main/java/run/xbud/XbudCryptoServer.java # SO模拟与 Spark HTTP API 服务
├── lib/                # 来源于应用提取的原始安卓 64 位加密相关动态库 (.so)
├── templates/          
│   └── index.html      # 网页前端控制面板 (Bootstrap 5 + Leaflet 地图)
├── server.py           # 基于 Flask 构建的 Web 业务中台
└── README.md           # 本说明文件
```

## 🚀 部署与启动指南

项目采用了 **Java 加密服务 + Python Web 服务** 分离的微服务架构，请分别在两个终端中启动。

### 1. 启动 Java Unidbg 加密服务 (基于 Maven)
打开终端，进入 `crypto_unidbg` 目录，编译并启动 Java 环境：
```bash
cd xbud_web_app/crypto_unidbg
# 编译并执行包含内部容器的 Java 服务器
mvn clean compile exec:java -Dexec.mainClass="run.xbud.XbudCryptoServer"
```
*启动成功后将在 `5001` 端口开启用于加密签名的 HTTP API 监听。*

### 2. 启动 Python Flask Web 可视化服务
在另一个独立终端中，进入 `xbud_web_app` 根目录，启动 Web 服务：
```bash
cd xbud_web_app
# 建议使用虚拟环境，并确保已安装 flask 和 requests，如果没有请执行:
# pip install flask requests

python3 server.py
```
*启动成功后将在 `http://127.0.0.1:5000` 提供 Web 界面支持。*

## 🎮 使用说明
1. 确保两个服务均处于运行状态。
2. 打开浏览器访问：`http://127.0.0.1:5000`
3. 进入网页后，将看到：
   - **登录面板**：可在上方切换手机号、学号或微信登录。
   - **交互地图**：左侧面板为当前定位模拟地图，可以点击定位按钮直接缩放获取高德路线信息。
   - 调试时，您可以开启 F12 浏览器控制台查看网络请求逻辑。网页对 Flask 接口请求会由 Flask 转发至 Java 的 5001 取得签名再原样打向后端 API。

## ⚠️ 免责声明
本仓库内容仅供网络协议与信息安全学习、研究使用。请勿用于未授权的数据伪造或商业用途，项目所用到的算法资料等最终解释权归原公司所有。
