import hashlib
import urllib.parse
from typing import Dict, Any

def main():
    # 使用您提供的参数进行测试
    params = {
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
        "Sign": "8e2eb3216de0882af0a08ae5ec850ae6"  # 这个会被移除
    }
    
    secret_key = "1W9CZQgbWfl6dr4n2sDZyGEkshDS0zI3D8RB89F9Pmwb0KSf"
    
    # 生成签名
    generated_sign = generate_sign(params, secret_key)
    original_sign = "8e2eb3216de0882af0a08ae5ec850ae6"
    
    print(f"生成的签名: {generated_sign}")
    print(f"原始签名: {original_sign}")
    
    if generated_sign == original_sign:
        print("签名验证: 成功")
    else:
        print("签名验证: 失败")
    
    # 打印参与签名的字符串，方便调试
    print("\n参与签名的参数字符串:")
    print_sign_string(params, secret_key)
    
    # 其他测试用例
    print("\n=== 其他测试用例 ===")
    
    # 测试用例2：包含空值和None
    params2 = {
        "name": "john",
        "email": "",
        "phone": None,
        "city": "beijing"
    }
    
    signature2 = generate_sign(params2, secret_key)
    print(f"测试用例2 - 签名: {signature2}")
    
    # 测试用例3：空参数
    params3 = {}
    signature3 = generate_sign(params3, secret_key)
    print(f"测试用例3 - 签名: {signature3}")

def generate_sign(params: Dict[str, Any], secret_key: str) -> str:
    """
    生成签名
    
    Args:
        params: 请求参数
        secret_key: 密钥
        
    Returns:
        MD5签名
    """
    # 复制参数对象，移除sign字段
    sign_params = params.copy()
    sign_params.pop("Sign", None)
    sign_params.pop("sign", None)  # 同时处理小写
    
    # 清理参数（移除None值和空字符串）
    cleaned_params = {}
    for key, value in sign_params.items():
        if value is not None:
            value_str = str(value).strip()
            if value_str != "":
                cleaned_params[key] = value_str
    
    # 如果清理后没有参数，直接使用空字符串进行签名
    if not cleaned_params:
        pre_string2 = f"SecretKey={secret_key}"
        return get_md5(pre_string2)
    
    # 1. 获取所有参数并按键名排序
    sorted_keys = sorted(cleaned_params.keys())
    
    # 2. 拼接参数字符串
    pre_string1_parts = []
    for key in sorted_keys:
        pre_string1_parts.append(f"{key}={cleaned_params[key]}")
    pre_string1 = "&".join(pre_string1_parts)
    
    # 3. 拼接密钥
    pre_string2 = f"{pre_string1}&SecretKey={secret_key}"
    
    # 4. 计算MD5
    return get_md5(pre_string2)

def get_md5(input_str: str) -> str:
    """
    计算MD5
    
    Args:
        input_str: 输入字符串
        
    Returns:
        MD5哈希值
    """
    md5_hash = hashlib.md5()
    md5_hash.update(input_str.encode('utf-8'))
    return md5_hash.hexdigest()

def print_sign_string(params: Dict[str, Any], secret_key: str):
    """
    打印参与签名的字符串，用于调试
    
    Args:
        params: 请求参数
        secret_key: 密钥
    """
    # 复制参数对象，移除sign字段
    sign_params = params.copy()
    sign_params.pop("Sign", None)
    sign_params.pop("sign", None)
    
    # 清理参数
    cleaned_params = {}
    for key, value in sign_params.items():
        if value is not None:
            value_str = str(value).strip()
            if value_str != "":
                cleaned_params[key] = value_str
    
    # 排序
    sorted_keys = sorted(cleaned_params.keys())
    
    # 拼接参数字符串
    pre_string1_parts = []
    for key in sorted_keys:
        pre_string1_parts.append(f"{key}={cleaned_params[key]}")
    pre_string1 = "&".join(pre_string1_parts)
    
    # 拼接密钥
    pre_string2 = f"{pre_string1}&SecretKey={secret_key}"
    
    print(f"排序后的参数: {pre_string1}")
    print(f"最终签名字符串: {pre_string2}")

if __name__ == "__main__":
    main()