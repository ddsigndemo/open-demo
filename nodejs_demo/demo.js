const crypto = require("crypto");

class OpenApiClient {
  constructor(baseUrl, accessKey, secretKey) {
    this.baseUrl = baseUrl.replace(/\/+$/, "");
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  sign(payload) {
    const source = Object.keys(payload)
      .filter((k) => k !== "Sign" && payload[k] !== null && String(payload[k]) !== "")
      .sort()
      .map((k) => `${k}=${String(payload[k]).trim()}`)
      .join("&") + `&SecretKey=${this.secretKey}`;
    return crypto.createHash("md5").update(source, "utf8").digest("hex");
  }

  buildCommon() {
    return {
      Timestamp: Math.floor(Date.now() / 1000),
      AccessKey: this.accessKey,
    };
  }

  async post(path, payload, absolute = false) {
    const url = absolute ? path : `${this.baseUrl}${path}`;
    console.log(`\nPOST ${url}`);
    console.log("payload:", JSON.stringify(payload));
    const resp = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const text = await resp.text();
    console.log("status:", resp.status);
    console.log("response:", text);
    return { status: resp.status, text };
  }

  async paySubmit(orderNo) {
    const payload = {
      ...this.buildCommon(),
      PayChannelId: "3081",
      Payee: "张三",
      PayeeNo: "test@example.com",
      PayeeAddress: "支付宝",
      OrderNo: orderNo,
      Amount: "100.00",
      CallbackUrl: "https://your-domain.com/pay/callback",
      Ext: "demo-ext",
    };
    payload.Sign = this.sign(payload);
    return this.post("/apiv1/open/pay/submit", payload);
  }

  async payQueryOrder(orderNo) {
    const payload = { ...this.buildCommon(), OrderNo: orderNo };
    payload.Sign = this.sign(payload);
    return this.post("/apiv1/open/pay/queryorder", payload);
  }

  async payQueryBalance() {
    const payload = this.buildCommon();
    payload.Sign = this.sign(payload);
    return this.post("/apiv1/open/pay/querybalance", payload);
  }

  async withdrawalSubmit(orderNo) {
    const payload = {
      ...this.buildCommon(),
      PayChannelId: "822",
      Payee: "李四",
      PayeeNo: "6222020000000000000",
      PayeeAddress: "招商银行",
      OrderNo: orderNo,
      Amount: "88.66",
      CallbackUrl: "https://your-domain.com/withdraw/callback",
      Ext: "demo-ext",
    };
    payload.Sign = this.sign(payload);
    return this.post("/apiv1/open/withdrawal/submit", payload);
  }

  async withdrawalQueryOrder(orderNo) {
    const payload = { ...this.buildCommon(), OrderNo: orderNo };
    payload.Sign = this.sign(payload);
    return this.post("/apiv1/open/withdrawal/queryorder", payload);
  }

  async withdrawalQueryBalance() {
    const payload = this.buildCommon();
    payload.Sign = this.sign(payload);
    return this.post("/apiv1/open/withdrawal/querybalance", payload);
  }

}

async function main() {
  const baseUrl = process.env.BASE_URL || "https://your-gateway.example.com";
  const accessKey = process.env.ACCESS_KEY || "YOUR_ACCESS_KEY";
  const secretKey = process.env.SECRET_KEY || "YOUR_SECRET_KEY";

  const client = new OpenApiClient(
    baseUrl,
    accessKey,
    secretKey
  );

  const ts = Math.floor(Date.now() / 1000);
  const payOrderNo = `NODE_PAY_${ts}`;
  const wdOrderNo = `NODE_WD_${ts}`;

  await client.paySubmit(payOrderNo);
  await client.payQueryOrder(payOrderNo);
  await client.payQueryBalance();

  await client.withdrawalSubmit(wdOrderNo);
  await client.withdrawalQueryOrder(wdOrderNo);
  await client.withdrawalQueryBalance();

}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
