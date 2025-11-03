package org.be.book.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentWebhookRequest {
    private Long orderId;
    private String pgProvider;
    private String pgTransactionId;
    private long paidAmount;
    private String signature; // 실연동 시 서명검증에 사용
}