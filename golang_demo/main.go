package main

import (
	"bytes"
	"crypto/md5"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"sort"
	"strings"
	"time"
)

type OpenAPIClient struct {
	BaseURL   string
	AccessKey string
	SecretKey string
	Client    *http.Client
}

func NewOpenAPIClient(baseURL, accessKey, secretKey string) *OpenAPIClient {
	return &OpenAPIClient{
		BaseURL:   strings.TrimRight(baseURL, "/"),
		AccessKey: accessKey,
		SecretKey: secretKey,
		Client:    &http.Client{Timeout: 20 * time.Second},
	}
}

func (c *OpenAPIClient) sign(payload map[string]any) string {
	keys := make([]string, 0, len(payload))
	for k, v := range payload {
		if k == "Sign" || v == nil || fmt.Sprintf("%v", v) == "" {
			continue
		}
		keys = append(keys, k)
	}
	sort.Strings(keys)

	parts := make([]string, 0, len(keys)+1)
	for _, k := range keys {
		parts = append(parts, fmt.Sprintf("%s=%v", k, strings.TrimSpace(fmt.Sprintf("%v", payload[k]))))
	}
	parts = append(parts, "SecretKey="+c.SecretKey)
	source := strings.Join(parts, "&")

	sum := md5.Sum([]byte(source))
	return hex.EncodeToString(sum[:])
}

func (c *OpenAPIClient) common() map[string]any {
	return map[string]any{
		"Timestamp": time.Now().Unix(),
		"AccessKey": c.AccessKey,
	}
}

func (c *OpenAPIClient) post(path string, payload map[string]any, absolute bool) error {
	url := path
	if !absolute {
		url = c.BaseURL + path
	}

	bs, _ := json.Marshal(payload)
	fmt.Printf("\nPOST %s\n", url)
	fmt.Printf("payload: %s\n", string(bs))

	req, err := http.NewRequest(http.MethodPost, url, bytes.NewBuffer(bs))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := c.Client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)
	fmt.Printf("status: %d\n", resp.StatusCode)
	fmt.Printf("response: %s\n", string(body))
	return nil
}

func (c *OpenAPIClient) PaySubmit(orderNo string) error {
	payload := c.common()
	payload["PayChannelId"] = "3081"
	payload["Payee"] = "张三"
	payload["PayeeNo"] = "test@example.com"
	payload["PayeeAddress"] = "支付宝"
	payload["OrderNo"] = orderNo
	payload["Amount"] = "100.00"
	payload["CallbackUrl"] = "https://your-domain.com/pay/callback"
	payload["Ext"] = "demo-ext"
	payload["Sign"] = c.sign(payload)
	return c.post("/apiv1/open/pay/submit", payload, false)
}

func (c *OpenAPIClient) PayQueryOrder(orderNo string) error {
	payload := c.common()
	payload["OrderNo"] = orderNo
	payload["Sign"] = c.sign(payload)
	return c.post("/apiv1/open/pay/queryorder", payload, false)
}

func (c *OpenAPIClient) PayQueryBalance() error {
	payload := c.common()
	payload["Sign"] = c.sign(payload)
	return c.post("/apiv1/open/pay/querybalance", payload, false)
}

func (c *OpenAPIClient) WithdrawalSubmit(orderNo string) error {
	payload := c.common()
	payload["PayChannelId"] = "822"
	payload["Payee"] = "李四"
	payload["PayeeNo"] = "6222020000000000000"
	payload["PayeeAddress"] = "招商银行"
	payload["OrderNo"] = orderNo
	payload["Amount"] = "88.66"
	payload["CallbackUrl"] = "https://your-domain.com/withdraw/callback"
	payload["Ext"] = "demo-ext"
	payload["Sign"] = c.sign(payload)
	return c.post("/apiv1/open/withdrawal/submit", payload, false)
}

func (c *OpenAPIClient) WithdrawalQueryOrder(orderNo string) error {
	payload := c.common()
	payload["OrderNo"] = orderNo
	payload["Sign"] = c.sign(payload)
	return c.post("/apiv1/open/withdrawal/queryorder", payload, false)
}

func (c *OpenAPIClient) WithdrawalQueryBalance() error {
	payload := c.common()
	payload["Sign"] = c.sign(payload)
	return c.post("/apiv1/open/withdrawal/querybalance", payload, false)
}

func main() {
	baseURL := os.Getenv("BASE_URL")
	if baseURL == "" {
		baseURL = "https://your-gateway.example.com"
	}
	accessKey := os.Getenv("ACCESS_KEY")
	if accessKey == "" {
		accessKey = "YOUR_ACCESS_KEY"
	}
	secretKey := os.Getenv("SECRET_KEY")
	if secretKey == "" {
		secretKey = "YOUR_SECRET_KEY"
	}

	client := NewOpenAPIClient(
		baseURL,
		accessKey,
		secretKey,
	)

	ts := time.Now().Unix()
	payOrderNo := fmt.Sprintf("GO_PAY_%d", ts)
	wdOrderNo := fmt.Sprintf("GO_WD_%d", ts)

	_ = client.PaySubmit(payOrderNo)
	_ = client.PayQueryOrder(payOrderNo)
	_ = client.PayQueryBalance()

	_ = client.WithdrawalSubmit(wdOrderNo)
	_ = client.WithdrawalQueryOrder(wdOrderNo)
	_ = client.WithdrawalQueryBalance()

}
