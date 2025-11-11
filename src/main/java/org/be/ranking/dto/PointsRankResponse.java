package org.be.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointsRankResponse {
    private int rank;
    private String userId;
    private String username;
    private long points;
}