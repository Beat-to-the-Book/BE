package org.be.recommend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RecommendationResultMessage {
    private String userId;
    private List<BookDto> books;
}
