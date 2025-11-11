// org/be/bookshelf/controller/BookshelfController.java
package org.be.bookshelf.controller;

import lombok.RequiredArgsConstructor;
import org.be.bookshelf.dto.BookshelfResponse;
import org.be.bookshelf.dto.BookshelfSaveRequest;
import org.be.bookshelf.service.BookshelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;

    @GetMapping
    public ResponseEntity<BookshelfResponse> get(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(bookshelfService.get(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<BookshelfResponse> create(@AuthenticationPrincipal UserDetails user,
                                                    @RequestBody BookshelfSaveRequest req) {
        return ResponseEntity.status(201).body(bookshelfService.create(user.getUsername(), req));
    }

    @PutMapping
    public ResponseEntity<BookshelfResponse> update(@AuthenticationPrincipal UserDetails user,
                                                    @RequestBody BookshelfSaveRequest req) {
        return ResponseEntity.ok(bookshelfService.update(user.getUsername(), req));
    }
}