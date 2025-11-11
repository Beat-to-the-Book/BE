package org.be.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BooksRankResponse {
    private int rank;
    private String userId;
    private String username;
    private long totalCount;
    private long purchaseCount;
    private long rentalCount;
}