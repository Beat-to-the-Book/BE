package org.be.bookshelf.dto;
import lombok.Getter;
import java.util.Map;
import java.util.List;

@Getter
public class BookshelfSaveRequest {
    private Map<String, List<DecorationDto>> decorations;

    @Getter
    public static class DecorationDto {
        private long id;
        private int type;
        private String color;
        private double[] position;
        private double rotationX;
        private double rotationY;
        private double rotationZ;
    }
}