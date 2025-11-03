package org.be.book.dto;
import lombok.Getter; import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private Long bookId;
    private int quantity = 1;
}