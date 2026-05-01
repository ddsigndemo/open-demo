import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // 使用您提供的参数进行测试
        Map<String, Object> params = new HashMap<>();
        params.put("AccessKey", "FmzLGtDVsiJbvNSU0qa6c6J8");
        params.put("Amount", "100.21");
        params.put("CallbackUrl", "https://xxxxx.com/xxxx/callback");
        params.put("Ext", "扩展数据");
        params.put("OrderNo", "OrderNo1753286865112");
        params.put("PayChannelId", "820");
        params.put("Payee", "张三");
        params.put("PayeeAddress", "支付宝");
        params.put("PayeeNo", "123456789@qq.com");
        params.put("Timestamp", "1753286865");
        params.put("Sign", "8e2eb3216de0882af0a08ae5ec850ae6"); // 这个会被移除
        
        String secretKey = "1W9CZQgbWfl6dr4n2sDZyGEkshDS0zI3D8RB89F9Pmwb0KSf";
        
        // 生成签名
        String generatedSign = generateSign(params, secretKey);
        String originalSign = "8e2eb3216de0882af0a08ae5ec850ae6";
        
        System.out.println("生成的签名: " + generatedSign);
        System.out.println("原始签名: " + originalSign);
        System.out.println("签名验证: " + (generatedSign.equals(originalSign) ? "成功" : "失败"));
        
        // 打印参与签名的字符串，方便调试
        System.out.println("\n参与签名的参数字符串:");
        printSignString(params, secretKey);
    }
    
    /**
     * 生成签名
     */
    public static String generateSign(Map<String, Object> params, String secretKey) {
        // 复制参数对象，移除sign字段
        Map<String, Object> signParams = new HashMap<>(params);
        signParams.remove("Sign");
        
        // 清理参数（移除null值和空字符串）
        Map<String, Object> cleanedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : signParams.entrySet()) {
            if (entry.getValue() != null) {
                String valueStr = String.valueOf(entry.getValue()).trim();
                if (!valueStr.isEmpty()) {
                    cleanedParams.put(entry.getKey(), valueStr);
                }
            }
        }
        
        // 1. 获取所有参数并按键名排序
        Map<String, Object> sortedParams = new TreeMap<>(cleanedParams);
        
        // 2. 拼接参数字符串
        StringBuilder preString1 = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            if (preString1.length() > 0) {
                preString1.append("&");
            }
            preString1.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // 3. 拼接密钥
        String preString2 = preString1 + "&SecretKey=" + secretKey;
        
        // 4. 计算MD5
        return getMD5(preString2);
    }
    
    /**
     * 计算MD5
     */
    private static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * 打印参与签名的字符串，用于调试
     */
    private static void printSignString(Map<String, Object> params, String secretKey) {
        // 复制参数对象，移除sign字段
        Map<String, Object> signParams = new HashMap<>(params);
        signParams.remove("Sign");
        
        // 清理参数
        Map<String, Object> cleanedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : signParams.entrySet()) {
            if (entry.getValue() != null) {
                String valueStr = String.valueOf(entry.getValue()).trim();
                if (!valueStr.isEmpty()) {
                    cleanedParams.put(entry.getKey(), valueStr);
                }
            }
        }
        
        // 排序
        Map<String, Object> sortedParams = new TreeMap<>(cleanedParams);
        
        
        // 拼接参数字符串
        StringBuilder preString1 = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            if (preString1.length() > 0) {
                preString1.append("&");
            }
            preString1.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // 拼接密钥
        String preString2 = preString1 + "&SecretKey=" + secretKey;
        
        System.out.println("排序后的参数: " + preString1.toString());
        System.out.println("最终签名字符串: " + preString2);
    }
}