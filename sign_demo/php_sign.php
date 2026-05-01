<?php
/**
 * 生成签名
 * @param array $params 请求参数
 * @param string $secretKey 密钥
 * @return string MD5签名
 */
function generateSign($params, $secretKey) {
    // 复制参数对象，移除sign字段
    $signParams = $params;
    unset($signParams['Sign']);
    
    // 清理参数（移除null值和空字符串）
    $cleanedParams = [];
    foreach ($signParams as $key => $value) {
        if ($value !== null && $value !== '') {
            $cleanedParams[$key] = $value;
        }
    }
    
    // 如果清理后没有参数，直接使用空字符串进行签名
    if (empty($cleanedParams)) {
        $preString2 = "SecretKey=" . $secretKey;
        return md5($preString2);
    }
    
    // 1. 获取所有参数并按键名排序
    ksort($cleanedParams);
    
    // 2. 拼接参数字符串
    $preString1 = '';
    foreach ($cleanedParams as $key => $value) {
        if ($preString1 !== '') {
            $preString1 .= '&';
        }
        $preString1 .= $key . '=' . $value;
    }
    
    // 3. 拼接密钥
    $preString2 = $preString1 . '&SecretKey=' . $secretKey;
    
    // 4. 计算MD5
    return md5($preString2);
}

/**
 * 打印参与签名的字符串，用于调试
 */
function printSignString($params, $secretKey) {
    // 复制参数对象，移除sign字段
    $signParams = $params;
    unset($signParams['Sign']);
    
    // 清理参数
    $cleanedParams = [];
    foreach ($signParams as $key => $value) {
        if ($value !== null && $value !== '') {
            $cleanedParams[$key] = $value;
        }
    }
    
    // 排序
    ksort($cleanedParams);
    
    // 拼接参数字符串
    $preString1 = '';
    foreach ($cleanedParams as $key => $value) {
        if ($preString1 !== '') {
            $preString1 .= '&';
        }
        $preString1 .= $key . '=' . $value;
    }
    
    // 拼接密钥
    $preString2 = $preString1 . '&SecretKey=' . $secretKey;
    
    echo "排序后的参数: " . $preString1 . "\n";
    echo "最终签名字符串: " . $preString2 . "\n";
}

// 测试代码
// 使用您提供的参数进行测试
$params = [
    "AccessKey" => "FmzLGtDVsiJbvNSU0qa6c6J8",
    "Amount" => "100.21",
    "CallbackUrl" => "https://xxxxx.com/xxxx/callback",
    "Ext" => "扩展数据",
    "OrderNo" => "OrderNo1753286865112",
    "PayChannelId" => "820",
    "Payee" => "张三",
    "PayeeAddress" => "支付宝",
    "PayeeNo" => "123456789@qq.com",
    "Timestamp" => "1753286865",
    "Sign" => "8e2eb3216de0882af0a08ae5ec850ae6" // 这个会被移除
];

$secretKey = "1W9CZQgbWfl6dr4n2sDZyGEkshDS0zI3D8RB89F9Pmwb0KSf";

// 生成签名
$generatedSign = generateSign($params, $secretKey);
$originalSign = "8e2eb3216de0882af0a08ae5ec850ae6";

echo "生成的签名: " . $generatedSign . "\n";
echo "原始签名: " . $originalSign . "\n";
echo "签名验证: " . ($generatedSign === $originalSign ? "成功" : "失败") . "\n";

// 打印参与签名的字符串，方便调试
echo "\n参与签名的参数字符串:\n";
printSignString($params, $secretKey);

// 其他测试用例
echo "\n=== 其他测试用例 ===\n";

// 测试用例2：包含空值和null
$params2 = [
    "name" => "john",
    "email" => "",
    "phone" => null,
    "city" => "beijing"
];

$signature2 = generateSign($params2, $secretKey);
echo "测试用例2 - 签名: " . $signature2 . "\n";

// 测试用例3：空参数
$params3 = [];
$signature3 = generateSign($params3, $secretKey);
echo "测试用例3 - 签名: " . $signature3 . "\n";
?>