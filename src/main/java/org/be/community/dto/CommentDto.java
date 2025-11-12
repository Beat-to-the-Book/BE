package org.be.community.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {

    @Getter @Setter
    public static class CreateRequest {
        private String content;
    }

    @Getter @Setter
    public static class ReplyRequest {
        private Long parentId;
        private String content;
    }

    @Getter @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long parentId;
        private Long rootId;
        private byte depth;
        private Long userId;
        private String username;
        private String content;
        private boolean deleted;
        private boolean edited;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Setter
        private List<Response> children;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String content;
    }
}