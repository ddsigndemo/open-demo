import hashlib
import json
import os
import time
from typing import Dict, Any

import requests


class OpenApiClient:
    def __init__(self, base_url: str, access_key: str, secret_key: str):
        self.base_url = base_url.rstrip("/")
        self.access_key = access_key
        self.secret_key = secret_key
        self.headers = {"Content-Type": "application/json"}

    def sign(self, payload: Dict[str, Any]) -> str:
        # 默认签名规则: 排除 Sign 和空值，按 key 升序拼接，再拼 SecretKey 后做 MD5 小写
        items = []
        for k, v in payload.items():
            if k == "Sign" or v is None or str(v) == "":
                continue
            items.append((k, str(v).strip()))
        items.sort(key=lambda x: x[0])
        source = "&".join(f"{k}={v}" for k, v in items) + f"&SecretKey={self.secret_key}"
        return hashlib.md5(source.encode("utf-8")).hexdigest()

    def post(self, path: str, payload: Dict[str, Any], absolute: bool = False):
        url = path if absolute else f"{self.base_url}{path}"
        print(f"\nPOST {url}")
        print("payload:", json.dumps(payload, ensure_ascii=False))
        resp = requests.post(url, headers=self.headers, json=payload, timeout=20)
        print("status:", resp.status_code)
        print("response:", resp.text)
        return resp

    def build_common(self) -> Dict[str, Any]:
        return {
            "Timestamp": int(time.time()),
            "AccessKey": self.access_key,
        }

    # -------- 代收 --------
    def pay_submit(self, order_no: str):
        payload = {
            **self.build_common(),
            "PayChannelId": "3081",
            "Payee": "张三",
            "PayeeNo": "test@example.com",
            "PayeeAddress": "支付宝",
            "OrderNo": order_no,
            "Amount": "100.00",
            "CallbackUrl": "https://your-domain.com/pay/callback",
            "Ext": "demo-ext",
        }
        payload["Sign"] = self.sign(payload)
        return self.post("/apiv1/open/pay/submit", payload)

    def pay_query_order(self, order_no: str):
        payload = {**self.build_common(), "OrderNo": order_no}
        payload["Sign"] = self.sign(payload)
        return self.post("/apiv1/open/pay/queryorder", payload)

    def pay_query_balance(self):
        payload = self.build_common()
        payload["Sign"] = self.sign(payload)
        return self.post("/apiv1/open/pay/querybalance", payload)

    # -------- 代付 --------
    def withdrawal_submit(self, order_no: str):
        payload = {
            **self.build_common(),
            "PayChannelId": "822",
            "Payee": "李四",
            "PayeeNo": "6222020000000000000",
            "PayeeAddress": "招商银行",
            "OrderNo": order_no,
            "Amount": "88.66",
            "CallbackUrl": "https://your-domain.com/withdraw/callback",
            "Ext": "demo-ext",
        }
        payload["Sign"] = self.sign(payload)
        return self.post("/apiv1/open/withdrawal/submit", payload)

    def withdrawal_query_order(self, order_no: str):
        payload = {**self.build_common(), "OrderNo": order_no}
        payload["Sign"] = self.sign(payload)
        return self.post("/apiv1/open/withdrawal/queryorder", payload)

    def withdrawal_query_balance(self):
        payload = self.build_common()
        payload["Sign"] = self.sign(payload)
        return self.post("/apiv1/open/withdrawal/querybalance", payload)

if __name__ == "__main__":
    base_url = os.getenv("BASE_URL", "https://your-gateway.example.com")
    access_key = os.getenv("ACCESS_KEY", "YOUR_ACCESS_KEY")
    secret_key = os.getenv("SECRET_KEY", "YOUR_SECRET_KEY")

    client = OpenApiClient(
        base_url=base_url,
        access_key=access_key,
        secret_key=secret_key,
    )

    ts = int(time.time())
    pay_order_no = f"PY_PAY_{ts}"
    wd_order_no = f"PY_WD_{ts}"

    # 代收
    client.pay_submit(pay_order_no)
    client.pay_query_order(pay_order_no)
    client.pay_query_balance()

    # 代付
    client.withdrawal_submit(wd_order_no)
    client.withdrawal_query_order(wd_order_no)
    client.withdrawal_query_balance()

