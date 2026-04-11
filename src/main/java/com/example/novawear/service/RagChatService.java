package com.example.novawear.service;

import com.example.novawear.dto.ChatRequest;
import com.example.novawear.entity.Product;
import com.example.novawear.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.novawear.entity.ProductBundle;
import com.example.novawear.entity.Voucher;
import com.example.novawear.entity.FlashSale;
import com.example.novawear.repository.ProductBundleRepository;
import com.example.novawear.repository.VoucherRepository;
import com.example.novawear.repository.FlashSaleRepository;

import java.time.Instant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RagChatService {

    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final ProductBundleRepository productBundleRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${qwen.api-key}")
    private String apiKey;

    public RagChatService(ProductRepository productRepository, 
                          VoucherRepository voucherRepository,
                          FlashSaleRepository flashSaleRepository,
                          ProductBundleRepository productBundleRepository,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.voucherRepository = voucherRepository;
        this.flashSaleRepository = flashSaleRepository;
        this.productBundleRepository = productBundleRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void streamChat(ChatRequest request, SseEmitter emitter) {
        try {
            // 1. Lấy dỡ liệu từ Database (Sát dự án nhất)
            List<Product> products = productRepository.findAll();
            List<Voucher> vouchers = voucherRepository.findActiveVouchers(Instant.now());
            List<FlashSale> flashSales = flashSaleRepository.findActiveNow(Instant.now());
            List<ProductBundle> bundles = productBundleRepository.findByActiveTrueOrderByCreatedAtDesc();

            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("DANH SÁCH NHỮNG SẢN PHẨM HIỆN CÓ TẠI CỬA HÀNG:\n");
            for (Product p : products) {
                contextBuilder.append("- ").append(p.getName())
                        .append(" (Cú pháp hiển thị ảnh trong Markdown: ![").append(p.getName()).append("](").append(p.getImageUrl()).append(")): ")
                        .append("Giá: ").append(p.getSalePrice() != null ? p.getSalePrice() : p.getPrice())
                        .append("đ (Tồn kho: ").append(p.getStock()).append(")\n");
            }
            
            if (!vouchers.isEmpty()) {
                contextBuilder.append("\n MÃ GIẢM GIÁ (VOUCHER) ĐANG CÓ HIỆU LỰC CHO KHÁCH HÀNG:\n");
                for (Voucher v : vouchers) {
                    contextBuilder.append("- Báo khách nhập mã '").append(v.getCode()).append("': ");
                    contextBuilder.append(v.getDescription()).append(" (Điều kiện: Đơn tối thiểu ").append(v.getMinOrderValue()).append("đ)\n");
                }
            }

            if (!flashSales.isEmpty()) {
                contextBuilder.append("\n CHƯƠNG TRÌNH FLASH SALE ĐANG DIỄN RA (Báo khách hàng săn ngay!):\n");
                for (FlashSale fs : flashSales) {
                    contextBuilder.append("- ").append(fs.getName())
                            .append(" (Giảm tới ").append(fs.getDiscountPercent()).append("%)\n");
                }
            }

            if (!bundles.isEmpty()) {
                contextBuilder.append("\n CÁC COMBO/BUNDLE SIÊU TIẾT KIỆM (Gợi ý khách mua combo rẻ hơn):\n");
                for (ProductBundle pb : bundles) {
                    contextBuilder.append("- ").append(pb.getName())
                            .append(" (Cú pháp hiển thị ảnh Combo: ![").append(pb.getName()).append("](").append(pb.getImageUrl()).append(")): ")
                            .append("Giảm thêm ").append(pb.getDiscountPercent()).append("% khi mua chung. Mô tả: ").append(pb.getDescription()).append("\n");
                }
            }

            String systemContext = "Bạn là trợ lý ảo của cửa hàng thời trang NovaWear. Luôn trả lời khách hàng bằng tiếng Việt, thân thiện, lịch sự, ngắn gọn trúng đích. " +
                    "Ngôn từ tư vấn thời thượng. Đổi trả trong 7 ngày, vận chuyển 2-5 ngày (free ship đơn >200k), thanh toán COD/VNPay.\n\n" +
                    contextBuilder.toString() +
                    "\n\nTUYỆT ĐỐI CHÚ Ý CÁC NGUYÊN TẮC BẤT DI BẤT DỊCH: " +
                    "\n1. KHÔNG BỊA ĐẶT DỮ LIỆU: Mọi thông tin (Sản phẩm, Voucher, Flash Sale, Combo) đều PHẢI lấy từ danh sách trên. NẾU KHÔNG CÓ TRONG DANH SÁCH THÌ KHÔNG ĐƯỢC NHẮC ĐẾN. Tự ý bịa ra mã giảm giá, bịa combo hoặc bịa flash sale là vi phạm nghiêm trọng." +
                    "\n2. BẠN KHÔNG THỂ TỰ ĐẶT HÀNG: Tuyệt đối không giả vờ chốt đơn, tạo mã đơn giả hay nói 'Đã đặt hàng thành công'. Gặp khách muốn mua, hãy hướng dẫn họ BẤM VÀO NÚT 'THÊM VÀO GIỎ / MUA NGAY' ngay trên website này để tiến hành thanh toán một cách an toàn." +
                    "\n3. HIỂN THỊ ẢNH CHO KHÁCH HÀNG: Website KHÔNG CÓ các tính năng như Video 360 độ, Thực tế ảo (AR), Thử đồ ảo. Nếu khách hỏi xem ảnh sản phẩm hoặc combo, BẠN PHẢI SỬ DỤNG TRỰC TIẾP CÚ PHÁP MARKDOWN ẢNH được liệt kê bên trên (Ví dụ: ![Tên Sản Phẩm](url_ảnh) ) để cho khách xem ảnh, tuyệt đối không chèn thêm dấu nháy đơn hay ngoặc kép ` bao quanh cú pháp markdown ảnh này vì nó sẽ làm hỏng chức năng.";

            // 2. Tạo Request gửi Qwen
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "qwen-plus");
            rootNode.put("stream", true);
            rootNode.put("temperature", 0.7);
            rootNode.put("max_tokens", 2000);

            ArrayNode messagesArray = rootNode.putArray("messages");
            // Add system
            ObjectNode sysMsg = objectMapper.createObjectNode();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemContext);
            messagesArray.add(sysMsg);

            // Add history
            if (request.getHistory() != null) {
                for (ChatRequest.ChatMessage m : request.getHistory()) {
                    ObjectNode msg = objectMapper.createObjectNode();
                    msg.put("role", m.getRole());
                    msg.put("content", m.getContent() == null ? "" : m.getContent());
                    messagesArray.add(msg);
                }
            }

            // Add new user message
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", request.getMessage());
            messagesArray.add(userMsg);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://dashscope-intl.aliyuncs.com/compatible-mode/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(rootNode)))
                    .build();

            // 3. Xử lý Streaming Response từ Qwen và đẩy thẳng vào SseEmitter
            httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            String errorBody = response.body().collect(java.util.stream.Collectors.joining("\n"));
                            System.err.println("DashScope API Error: " + response.statusCode() + " " + errorBody);
                            try {
                                ObjectNode errNode = objectMapper.createObjectNode();
                                errNode.put("content", "\n\n[Lỗi kết nối API AI: " + response.statusCode() + ". Lý do: "
                                        + errorBody + "]");
                                emitter.send(objectMapper.writeValueAsString(errNode));
                                emitter.complete();
                            } catch (Exception e) {
                            }
                            return;
                        }
                        Stream<String> lines = response.body();
                        lines.forEach(line -> {
                            System.out.println("QWEN SSE LINE: " + line); // DEBUG
                            if (line.isEmpty() || line.equals("data: [DONE]")) {
                                return;
                            }
                            if (line.startsWith("data: ")) {
                                try {
                                    String json = line.substring(6);
                                    JsonNode node = objectMapper.readTree(json);
                                    JsonNode choices = node.get("choices");
                                    if (choices != null && choices.isArray() && choices.size() > 0) {
                                        JsonNode delta = choices.get(0).get("delta");
                                        if (delta != null && delta.has("content")) {
                                            String content = delta.get("content").asText();
                                            ObjectNode resNode = objectMapper.createObjectNode();
                                            resNode.put("content", content);
                                            try {
                                                emitter.send(objectMapper.writeValueAsString(resNode));
                                            } catch (Exception ex) {
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });
                        emitter.complete();
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        try {
                            ObjectNode errNode = objectMapper.createObjectNode();
                            errNode.put("content", "\n\n[Lỗi kết nối từ máy chủ AI: " + ex.getMessage() + "]");
                            emitter.send(objectMapper.writeValueAsString(errNode));
                            emitter.completeWithError(ex);
                        } catch (Exception ignored) {
                        }
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
            emitter.completeWithError(e);
        }
    }
}