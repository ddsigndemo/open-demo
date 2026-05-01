<?php

class OpenApiClient
{
    private string $baseUrl;
    private string $accessKey;
    private string $secretKey;

    public function __construct(string $baseUrl, string $accessKey, string $secretKey)
    {
        $this->baseUrl = rtrim($baseUrl, '/');
        $this->accessKey = $accessKey;
        $this->secretKey = $secretKey;
    }

    private function sign(array $payload): string
    {
        $filtered = [];
        foreach ($payload as $k => $v) {
            if ($k === 'Sign' || $v === null || trim((string)$v) === '') {
                continue;
            }
            $filtered[$k] = trim((string)$v);
        }
        ksort($filtered);
        $pairs = [];
        foreach ($filtered as $k => $v) {
            $pairs[] = $k . '=' . $v;
        }
        $source = implode('&', $pairs) . '&SecretKey=' . $this->secretKey;
        return md5($source);
    }

    private function common(): array
    {
        return [
            'Timestamp' => time(),
            'AccessKey' => $this->accessKey,
        ];
    }

    private function post(string $path, array $payload, bool $absolute = false): void
    {
        $url = $absolute ? $path : $this->baseUrl . $path;
        echo PHP_EOL . "POST {$url}" . PHP_EOL;
        echo "payload: " . json_encode($payload, JSON_UNESCAPED_UNICODE) . PHP_EOL;

        $ch = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_POST => true,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
            CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_UNICODE),
            CURLOPT_TIMEOUT => 20,
        ]);
        $response = curl_exec($ch);
        $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        if ($response === false) {
            echo "curl error: " . curl_error($ch) . PHP_EOL;
        } else {
            echo "status: {$status}" . PHP_EOL;
            echo "response: {$response}" . PHP_EOL;
        }
        curl_close($ch);
    }

    public function paySubmit(string $orderNo): void
    {
        $payload = array_merge($this->common(), [
            'PayChannelId' => '3081',
            'Payee' => '张三',
            'PayeeNo' => 'test@example.com',
            'PayeeAddress' => '支付宝',
            'OrderNo' => $orderNo,
            'Amount' => '100.00',
            'CallbackUrl' => 'https://your-domain.com/pay/callback',
            'Ext' => 'demo-ext',
        ]);
        $payload['Sign'] = $this->sign($payload);
        $this->post('/apiv1/open/pay/submit', $payload);
    }

    public function payQueryOrder(string $orderNo): void
    {
        $payload = array_merge($this->common(), ['OrderNo' => $orderNo]);
        $payload['Sign'] = $this->sign($payload);
        $this->post('/apiv1/open/pay/queryorder', $payload);
    }

    public function payQueryBalance(): void
    {
        $payload = $this->common();
        $payload['Sign'] = $this->sign($payload);
        $this->post('/apiv1/open/pay/querybalance', $payload);
    }

    public function withdrawalSubmit(string $orderNo): void
    {
        $payload = array_merge($this->common(), [
            'PayChannelId' => '822',
            'Payee' => '李四',
            'PayeeNo' => '6222020000000000000',
            'PayeeAddress' => '招商银行',
            'OrderNo' => $orderNo,
            'Amount' => '88.66',
            'CallbackUrl' => 'https://your-domain.com/withdraw/callback',
            'Ext' => 'demo-ext',
        ]);
        $payload['Sign'] = $this->sign($payload);
        $this->post('/apiv1/open/withdrawal/submit', $payload);
    }

    public function withdrawalQueryOrder(string $orderNo): void
    {
        $payload = array_merge($this->common(), ['OrderNo' => $orderNo]);
        $payload['Sign'] = $this->sign($payload);
        $this->post('/apiv1/open/withdrawal/queryorder', $payload);
    }

    public function withdrawalQueryBalance(): void
    {
        $payload = $this->common();
        $payload['Sign'] = $this->sign($payload);
        $this->post('/apiv1/open/withdrawal/querybalance', $payload);
    }

}

$accessKey = getenv('ACCESS_KEY') ?: 'YOUR_ACCESS_KEY';
$secretKey = getenv('SECRET_KEY') ?: 'YOUR_SECRET_KEY';
$baseUrl = getenv('BASE_URL') ?: 'https://your-gateway.example.com';

$client = new OpenApiClient(
    $baseUrl,
    $accessKey,
    $secretKey
);

$ts = time();
$payOrderNo = 'PHP_PAY_' . $ts;
$wdOrderNo = 'PHP_WD_' . $ts;

$client->paySubmit($payOrderNo);
$client->payQueryOrder($payOrderNo);
$client->payQueryBalance();

$client->withdrawalSubmit($wdOrderNo);
$client->withdrawalQueryOrder($wdOrderNo);
$client->withdrawalQueryBalance();

