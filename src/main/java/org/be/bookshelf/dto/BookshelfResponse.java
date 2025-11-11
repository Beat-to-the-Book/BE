package org.be.bookshelf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookshelfResponse {
    private Long userId;
    private Map<String, List<BookshelfSaveRequest.DecorationDto>> decorations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}