package org.be.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.be.book.model.Book;

// 추천 도서, 유저가 읽은 도서 모두 공통적으로 사용
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private String title;
    private String author;
    private String genre;

    public static BookDto from(Book book) {
        return new BookDto(book.getTitle(), book.getAuthor(), book.getGenre());
    }
}
