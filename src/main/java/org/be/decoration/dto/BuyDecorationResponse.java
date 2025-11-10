// org/be/decoration/dto/BuyDecorationResponse.java
package org.be.decoration.dto;

public record BuyDecorationResponse(
        int decorationType,
        int purchasedCount,   // 항상 1
        int totalCount,       // 구매 후 총 보유 개수
        int remainingPoints   // 구매 후 남은 포인트
) {}