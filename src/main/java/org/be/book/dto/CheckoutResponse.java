package org.be.book.dto;
import lombok.AllArgsConstructor; import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckoutResponse {
    private Long orderId;
    private String payUrl; // 프론트 결제 페이지 경로(or PG URL)
}