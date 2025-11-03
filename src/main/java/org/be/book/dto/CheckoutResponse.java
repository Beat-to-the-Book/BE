package org.be.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.be.point.dto.MilestoneAwardResponse;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {
    private Long orderId;
    private String payUrl;
    private MilestoneAwardResponse milestone;
}