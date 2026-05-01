const crypto = require('crypto');

/**
 * 生成签名
 * @param {Object} params 请求参数
 * @param {string} secretKey 密钥
 * @returns {string} MD5签名
 */
function generateSign(params, secretKey) {
    // 复制参数对象，移除sign字段
    const signParams = { ...params };
    delete signParams.Sign;
    delete signParams.sign; // 同时处理小写
    
    // 清理参数（移除null值和空字符串）
    const cleanedParams = {};
    for (const [key, value] of Object.entries(signParams)) {
        if (value !== null && value !== undefined) {
            const valueStr = String(value).trim();
            if (valueStr !== '') {
                cleanedParams[key] = valueStr;
            }
        }
    }
    
    // 如果清理后没有参数，直接使用空字符串进行签名
    if (Object.keys(cleanedParams).length === 0) {
        const preString2 = `SecretKey=${secretKey}`;
        return getMD5(preString2);
    }
    
    // 1. 获取所有参数并按键名排序
    const sortedKeys = Object.keys(cleanedParams).sort();
    
    // 2. 拼接参数字符串
    const preString1Parts = [];
    for (const key of sortedKeys) {
        preString1Parts.push(`${key}=${cleanedParams[key]}`);
    }
    const preString1 = preString1Parts.join('&');
    
    // 3. 拼接密钥
    const preString2 = `${preString1}&SecretKey=${secretKey}`;
    
    // 4. 计算MD5
    return getMD5(preString2);
}

/**
 * 计算MD5
 * @param {string} input 输入字符串
 * @returns {string} MD5哈希值
 */
function getMD5(input) {
    return crypto.createHash('md5').update(input).digest('hex');
}

/**
 * 打印参与签名的字符串，用于调试
 * @param {Object} params 请求参数
 * @param {string} secretKey 密钥
 */
function printSignString(params, secretKey) {
    // 复制参数对象，移除sign字段
    const signParams = { ...params };
    delete signParams.Sign;
    delete signParams.sign;
    
    // 清理参数
    const cleanedParams = {};
    for (const [key, value] of Object.entries(signParams)) {
        if (value !== null && value !== undefined) {
            const valueStr = String(value).trim();
            if (valueStr !== '') {
                cleanedParams[key] = valueStr;
            }
        }
    }
    
    // 排序
    const sortedKeys = Object.keys(cleanedParams).sort();
    
    // 拼接参数字符串
    const preString1Parts = [];
    for (const key of sortedKeys) {
        preString1Parts.push(`${key}=${cleanedParams[key]}`);
    }
    const preString1 = preString1Parts.join('&');
    
    // 拼接密钥
    const preString2 = `${preString1}&SecretKey=${secretKey}`;
    
    console.log("排序后的参数:", preString1);
    console.log("最终签名字符串:", preString2);
}

// 测试代码
function main() {
    // 使用您提供的参数进行测试
    const params = {
        "AccessKey": "FmzLGtDVsiJbvNSU0qa6c6J8",
        "Amount": "100.21",
        "CallbackUrl": "https://xxxxx.com/xxxx/callback",
        "Ext": "扩展数据",
        "OrderNo": "OrderNo1753286865112",
        "PayChannelId": "820",
        "Payee": "张三",
        "PayeeAddress": "支付宝",
        "PayeeNo": "123456789@qq.com",
        "Timestamp": "1753286865",
        "Sign": "8e2eb3216de0882af0a08ae5ec850ae6" // 这个会被移除
    };
    
    const secretKey = "1W9CZQgbWfl6dr4n2sDZyGEkshDS0zI3D8RB89F9Pmwb0KSf";
    
    // 生成签名
    const generatedSign = generateSign(params, secretKey);
    const originalSign = "8e2eb3216de0882af0a08ae5ec850ae6";
    
    console.log("生成的签名:", generatedSign);
    console.log("原始签名:", originalSign);
    
    if (generatedSign === originalSign) {
        console.log("签名验证: 成功");
    } else {
        console.log("签名验证: 失败");
    }
    
    // 打印参与签名的字符串，方便调试
    console.log("\n参与签名的参数字符串:");
    printSignString(params, secretKey);
    
    // 其他测试用例
    console.log("\n=== 其他测试用例 ===");
    
    // 测试用例2：包含空值和null
    const params2 = {
        "name": "john",
        "email": "",
        "phone": null,
        "city": "beijing"
    };
    
    const signature2 = generateSign(params2, secretKey);
    console.log("测试用例2 - 签名:", signature2);
    
    // 测试用例3：空参数
    const params3 = {};
    const signature3 = generateSign(params3, secretKey);
    console.log("测试用例3 - 签名:", signature3);
}

// 运行测试
main();