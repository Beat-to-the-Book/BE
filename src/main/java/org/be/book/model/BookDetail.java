package org.be.book.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_detail")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "size_text", length = 50)
    private String sizeText;

    @Column(name = "weight_g")
    private Integer weightG;

    @Column(name = "isbn", length = 32)
    private String isbn;

    @Lob @Column(name = "editor_pick", columnDefinition = "TEXT")
    private String editorPickHtml;

    @Lob @Column(name = "introduction", columnDefinition = "TEXT")
    private String introductionHtml;

    @Lob @Column(name = "toc", columnDefinition = "TEXT")
    private String tocHtml;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;
}
