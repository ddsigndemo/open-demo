import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenApiDemo {

    static class OpenApiClient {
        private final String baseUrl;
        private final String accessKey;
        private final String secretKey;
        private final HttpClient httpClient;

        OpenApiClient(String baseUrl, String accessKey, String secretKey) {
            this.baseUrl = baseUrl.replaceAll("/+$", "");
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.httpClient = HttpClient.newHttpClient();
        }

        private String sign(Map<String, Object> payload) {
            List<Map.Entry<String, Object>> items = payload.entrySet().stream()
                    .filter(e -> !"Sign".equals(e.getKey()))
                    .filter(e -> e.getValue() != null && !String.valueOf(e.getValue()).trim().isEmpty())
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .collect(Collectors.toList());

            String source = items.stream()
                    .map(e -> e.getKey() + "=" + String.valueOf(e.getValue()).trim())
                    .collect(Collectors.joining("&")) + "&SecretKey=" + secretKey;
            return md5(source);
        }

        private String md5(String source) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(source.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private Map<String, Object> common() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("Timestamp", Instant.now().getEpochSecond());
            m.put("AccessKey", accessKey);
            return m;
        }

        private void post(String path, Map<String, Object> payload, boolean absolute) {
            String url = absolute ? path : baseUrl + path;
            String json = toJson(payload);
            System.out.println("\nPOST " + url);
            System.out.println("payload: " + json);

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("status: " + response.statusCode());
                System.out.println("response: " + response.body());
            } catch (IOException | InterruptedException e) {
                System.out.println("request failed: " + e.getMessage());
            }
        }

        // 轻量 JSON 序列化，仅用于 demo
        private String toJson(Map<String, Object> map) {
            List<String> pairs = new ArrayList<>();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                String key = "\"" + escape(e.getKey()) + "\"";
                Object v = e.getValue();
                String value;
                if (v == null) {
                    value = "null";
                } else if (v instanceof Number || v instanceof Boolean) {
                    value = String.valueOf(v);
                } else {
                    value = "\"" + escape(String.valueOf(v)) + "\"";
                }
                pairs.add(key + ":" + value);
            }
            return "{" + String.join(",", pairs) + "}";
        }

        private String escape(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        void paySubmit(String orderNo) {
            Map<String, Object> payload = common();
            payload.put("PayChannelId", "3081");
            payload.put("Payee", "张三");
            payload.put("PayeeNo", "test@example.com");
            payload.put("PayeeAddress", "支付宝");
            payload.put("OrderNo", orderNo);
            payload.put("Amount", "100.00");
            payload.put("CallbackUrl", "https://your-domain.com/pay/callback");
            payload.put("Ext", "demo-ext");
            payload.put("Sign", sign(payload));
            post("/apiv1/open/pay/submit", payload, false);
        }

        void payQueryOrder(String orderNo) {
            Map<String, Object> payload = common();
            payload.put("OrderNo", orderNo);
            payload.put("Sign", sign(payload));
            post("/apiv1/open/pay/queryorder", payload, false);
        }

        void payQueryBalance() {
            Map<String, Object> payload = common();
            payload.put("Sign", sign(payload));
            post("/apiv1/open/pay/querybalance", payload, false);
        }

        void withdrawalSubmit(String orderNo) {
            Map<String, Object> payload = common();
            payload.put("PayChannelId", "822");
            payload.put("Payee", "李四");
            payload.put("PayeeNo", "6222020000000000000");
            payload.put("PayeeAddress", "招商银行");
            payload.put("OrderNo", orderNo);
            payload.put("Amount", "88.66");
            payload.put("CallbackUrl", "https://your-domain.com/withdraw/callback");
            payload.put("Ext", "demo-ext");
            payload.put("Sign", sign(payload));
            post("/apiv1/open/withdrawal/submit", payload, false);
        }

        void withdrawalQueryOrder(String orderNo) {
            Map<String, Object> payload = common();
            payload.put("OrderNo", orderNo);
            payload.put("Sign", sign(payload));
            post("/apiv1/open/withdrawal/queryorder", payload, false);
        }

        void withdrawalQueryBalance() {
            Map<String, Object> payload = common();
            payload.put("Sign", sign(payload));
            post("/apiv1/open/withdrawal/querybalance", payload, false);
        }

    }

    public static void main(String[] args) {
        String baseUrl = System.getenv().getOrDefault("BASE_URL", "https://your-gateway.example.com");
        String accessKey = System.getenv().getOrDefault("ACCESS_KEY", "YOUR_ACCESS_KEY");
        String secretKey = System.getenv().getOrDefault("SECRET_KEY", "YOUR_SECRET_KEY");

        OpenApiClient client = new OpenApiClient(
                baseUrl,
                accessKey,
                secretKey
        );

        long ts = Instant.now().getEpochSecond();
        String payOrderNo = "JAVA_PAY_" + ts;
        String wdOrderNo = "JAVA_WD_" + ts;

        client.paySubmit(payOrderNo);
        client.payQueryOrder(payOrderNo);
        client.payQueryBalance();

        client.withdrawalSubmit(wdOrderNo);
        client.withdrawalQueryOrder(wdOrderNo);
        client.withdrawalQueryBalance();

    }
}
