package org.be.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookDetailData {
    private final String editorsPick;
    private final String introduction;
    private final String toc;
    private final String basicInfo;
}