package org.be.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookDetailResponse {
    private final Long id;
    private final Long bookId;
    private final String bookTitle;   // 선택: 제목도 함께
    private final String editorPickHtml;
    private final String introductionHtml;
    private final String tocHtml;
    private final String sourceUrl;
}