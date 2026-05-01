# Open API Demo (代收 / 代付)

基于 `API支付文档.json` 整理的可对接接口如下（核心业务）：

- 代收下单：`POST /apiv1/open/pay/submit`
- 代收订单查询：`POST /apiv1/open/pay/queryorder`
- 代收余额查询：`POST /apiv1/open/pay/querybalance`
- 代付下单：`POST /apiv1/open/withdrawal/submit`
- 代付订单查询：`POST /apiv1/open/withdrawal/queryorder`
- 代付余额查询：`POST /apiv1/open/withdrawal/querybalance`

另有回调类接口：
- 订单回调（代收/代付）：回调地址由商户提供

## 目录

- `java_demo` Java Demo
- `golang_demo` Golang Demo
- `php_demo` PHP Demo
- `python_demo` Python Demo
- `nodejs_demo` Node.js Demo

每个 demo 都包含：
- 签名函数（默认：参数排序后拼接 + `SecretKey` 做 MD5，按你的平台规则可调整）
- 代收接口调用示例
- 代付接口调用示例

## 公开仓库使用

运行前请通过环境变量注入配置，不要在代码中写真实密钥：

- `BASE_URL` 例如 `http://127.0.0.1:9999`
- `ACCESS_KEY` 你的商户 AccessKey
- `SECRET_KEY` 你的商户 SecretKey

示例：

```bash
BASE_URL=http://127.0.0.1:9999 ACCESS_KEY=xxx SECRET_KEY=yyy node nodejs_demo/demo.js
```

## 各 Demo 执行方式

- Node.js
```bash
BASE_URL=http://127.0.0.1:9999 ACCESS_KEY=xxx SECRET_KEY=yyy node nodejs_demo/demo.js
```

- Golang
```bash
BASE_URL=http://127.0.0.1:9999 ACCESS_KEY=xxx SECRET_KEY=yyy go run golang_demo/main.go
```

- Python
```bash
BASE_URL=http://127.0.0.1:9999 ACCESS_KEY=xxx SECRET_KEY=yyy python3 python_demo/demo.py
```

- PHP
```bash
BASE_URL=http://127.0.0.1:9999 ACCESS_KEY=xxx SECRET_KEY=yyy php php_demo/demo.php
```

- Java
```bash
cd java_demo
BASE_URL=http://127.0.0.1:9999 ACCESS_KEY=xxx SECRET_KEY=yyy javac OpenApiDemo.java
BASE_URL=http://127.0.0.1:9999 ACCESS_KEY=xxx SECRET_KEY=yyy java OpenApiDemo
```

## 注意

- 文档中的 `AccessKey` / `SecretKey` 示例值请勿用于生产。
- 少数文档条目是占位 URL，建议以后台真实环境地址为准。
