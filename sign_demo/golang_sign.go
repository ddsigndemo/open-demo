package main

import (
	"crypto/md5"
	"fmt"
	"sort"
	"strings"
)

func main() {
	// 使用您提供的参数进行测试
	params := map[string]interface{}{
		"AccessKey":      "FmzLGtDVsiJbvNSU0qa6c6J8",
		"Amount":         "100.21",
		"CallbackUrl":    "https://xxxxx.com/xxxx/callback",
		"Ext":            "扩展数据",
		"OrderNo":        "OrderNo1753286865112",
		"PayChannelId":   "820",
		"Payee":          "张三",
		"PayeeAddress":   "支付宝",
		"PayeeNo":        "123456789@qq.com",
		"Timestamp":      "1753286865",
		"Sign":           "8e2eb3216de0882af0a08ae5ec850ae6", // 这个会被移除
	}

	secretKey := "1W9CZQgbWfl6dr4n2sDZyGEkshDS0zI3D8RB89F9Pmwb0KSf"

	// 生成签名
	generatedSign := GenerateSign(params, secretKey)
	originalSign := "8e2eb3216de0882af0a08ae5ec850ae6"

	fmt.Printf("生成的签名: %s\n", generatedSign)
	fmt.Printf("原始签名: %s\n", originalSign)
	
	if generatedSign == originalSign {
		fmt.Println("签名验证: 成功")
	} else {
		fmt.Println("签名验证: 失败")
	}

	// 打印参与签名的字符串，方便调试
	fmt.Println("\n参与签名的参数字符串:")
	PrintSignString(params, secretKey)

	// 其他测试用例
	fmt.Println("\n=== 其他测试用例 ===")

	// 测试用例2：包含空值和nil
	params2 := map[string]interface{}{
		"name":  "john",
		"email": "",
		"phone": nil,
		"city":  "beijing",
	}

	signature2 := GenerateSign(params2, secretKey)
	fmt.Printf("测试用例2 - 签名: %s\n", signature2)

	// 测试用例3：空参数
	params3 := map[string]interface{}{}
	signature3 := GenerateSign(params3, secretKey)
	fmt.Printf("测试用例3 - 签名: %s\n", signature3)
}

/**
 * 生成签名
 * @param params 请求参数
 * @param secretKey 密钥
 * @return MD5签名
 */
func GenerateSign(params map[string]interface{}, secretKey string) string {
	// 复制参数对象，移除sign字段
	signParams := make(map[string]interface{})
	for k, v := range params {
		if k != "Sign" && k != "sign" {
			signParams[k] = v
		}
	}

	// 清理参数（移除nil值和空字符串）
	cleanedParams := make(map[string]string)
	for k, v := range signParams {
		if v != nil {
			valueStr := fmt.Sprintf("%v", v)
			if strings.TrimSpace(valueStr) != "" {
				cleanedParams[k] = valueStr
			}
		}
	}

	// 如果清理后没有参数，直接使用空字符串进行签名
	if len(cleanedParams) == 0 {
		preString2 := "SecretKey=" + secretKey
		return GetMD5(preString2)
	}

	// 1. 获取所有参数并按键名排序
	keys := make([]string, 0, len(cleanedParams))
	for k := range cleanedParams {
		keys = append(keys, k)
	}
	sort.Strings(keys)

	// 2. 拼接参数字符串
	var preString1Builder strings.Builder
	for i, key := range keys {
		if i > 0 {
			preString1Builder.WriteString("&")
		}
		preString1Builder.WriteString(fmt.Sprintf("%s=%s", key, cleanedParams[key]))
	}
	preString1 := preString1Builder.String()

	// 3. 拼接密钥
	preString2 := preString1 + "&SecretKey=" + secretKey

	// 4. 计算MD5
	return GetMD5(preString2)
}

/**
 * 计算MD5
 */
func GetMD5(input string) string {
	hash := md5.Sum([]byte(input))
	return fmt.Sprintf("%x", hash)
}

/**
 * 打印参与签名的字符串，用于调试
 */
func PrintSignString(params map[string]interface{}, secretKey string) {
	// 复制参数对象，移除sign字段
	signParams := make(map[string]interface{})
	for k, v := range params {
		if k != "Sign" && k != "sign" {
			signParams[k] = v
		}
	}

	// 清理参数
	cleanedParams := make(map[string]string)
	for k, v := range signParams {
		if v != nil {
			valueStr := fmt.Sprintf("%v", v)
			if strings.TrimSpace(valueStr) != "" {
				cleanedParams[k] = valueStr
			}
		}
	}

	// 排序
	keys := make([]string, 0, len(cleanedParams))
	for k := range cleanedParams {
		keys = append(keys, k)
	}
	sort.Strings(keys)

	// 拼接参数字符串
	var preString1Builder strings.Builder
	for i, key := range keys {
		if i > 0 {
			preString1Builder.WriteString("&")
		}
		preString1Builder.WriteString(fmt.Sprintf("%s=%s", key, cleanedParams[key]))
	}
	preString1 := preString1Builder.String()

	// 拼接密钥
	preString2 := preString1 + "&SecretKey=" + secretKey

	fmt.Printf("排序后的参数: %s\n", preString1)
	fmt.Printf("最终签名字符串: %s\n", preString2)
}