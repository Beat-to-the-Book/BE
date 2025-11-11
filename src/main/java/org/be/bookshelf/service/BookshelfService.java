package org.be.bookshelf.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.bookshelf.dto.BookshelfResponse;
import org.be.bookshelf.dto.BookshelfSaveRequest;
import org.be.bookshelf.model.Bookshelf;
import org.be.bookshelf.repository.BookshelfRepository;
import org.be.decoration.repository.DecorationInventoryRepository; // 보유 수 검증용
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookshelfService {

    private final UserRepository userRepository;
    private final BookshelfRepository bookshelfRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 선택: 배치 검증 위해 보유 수 확인
    private final DecorationInventoryRepository decorationInventoryRepository;

    @Transactional(readOnly = true)
    public BookshelfResponse get(String userId) {
        User user = findUser(userId);
        Bookshelf bs = bookshelfRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BOOKSHELF_NOT_FOUND"));

        var map = readJson(bs.getDecorationsJson());
        return new BookshelfResponse(user.getId(), map, bs.getCreatedAt(), bs.getUpdatedAt());
    }

    @Transactional
    public BookshelfResponse create(String userId, BookshelfSaveRequest req) {
        User user = findUser(userId);
        bookshelfRepository.findByUser(user).ifPresent(b -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 책장이 존재합니다.");
        });

        // 최초 생성 시에도 보유 수 초과 배치 방지
        if (req.getDecorations() == null) {
            Map<String, List<BookshelfSaveRequest.DecorationDto>> empty = new LinkedHashMap<>();
            for (int i = 1; i <= 7; i++) empty.put(String.valueOf(i), List.of());
            // lombok @Setter 없을 수 있으므로, 요청 객체가 불변이라면 아래 검증 메서드에서 null 안전 처리함
        }
        validateAndLimitByInventory(user, req);

        Bookshelf bs = Bookshelf.builder()
                .user(user)
                .decorationsJson(writeJson(req.getDecorations()))
                .build();

        bs = bookshelfRepository.save(bs);
        var map = readJson(bs.getDecorationsJson());
        return new BookshelfResponse(user.getId(), map, bs.getCreatedAt(), bs.getUpdatedAt());
    }

    @Transactional
    public BookshelfResponse update(String userId, BookshelfSaveRequest req) {
        User user = findUser(userId);
        Bookshelf bs = bookshelfRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BOOKSHELF_NOT_FOUND"));

        // 1) DB의 현재 배치 읽기
        Map<String, List<BookshelfSaveRequest.DecorationDto>> current = readJson(bs.getDecorationsJson());
        Map<String, List<BookshelfSaveRequest.DecorationDto>> incoming =
                Optional.ofNullable(req.getDecorations()).orElseGet(() -> {
                    Map<String, List<BookshelfSaveRequest.DecorationDto>> m = new LinkedHashMap<>();
                    for (int i = 1; i <= 7; i++) m.put(String.valueOf(i), List.of());
                    return m;
                });

        // 2) 층별로 "합집합" 병합 (요청에 없는 기존 항목은 유지, 같은 id는 갱신)
        Map<String, List<BookshelfSaveRequest.DecorationDto>> merged = new LinkedHashMap<>();
        for (int floor = 1; floor <= 7; floor++) {
            String k = String.valueOf(floor);
            List<BookshelfSaveRequest.DecorationDto> cur = new ArrayList<>(current.getOrDefault(k, List.of()));
            List<BookshelfSaveRequest.DecorationDto> inc = incoming.getOrDefault(k, List.of());

            merged.put(k, mergeFloorById(cur, inc)); // ★ 핵심: 항목 단위 병합
        }

        // 3) 보유 수 초과 검증(선택)
        validateAndLimitByInventoryWithMap(user, merged);

        // 4) 저장
        bs.setDecorationsJson(writeJson(merged));
        var map = readJson(bs.getDecorationsJson());
        return new BookshelfResponse(user.getId(), map, bs.getCreatedAt(), bs.getUpdatedAt());
    }


    // =================== helpers ===================

    private List<BookshelfSaveRequest.DecorationDto> mergeFloorById(
            List<BookshelfSaveRequest.DecorationDto> current,
            List<BookshelfSaveRequest.DecorationDto> incoming
    ) {
        Map<Long, BookshelfSaveRequest.DecorationDto> byId = new LinkedHashMap<>();
        // 기존 유지
        for (var d : current) {
            byId.put(d.getId(), d);
        }
        // 들어온 건 upsert (같은 id면 덮어쓰기, 새로운 id면 추가)
        for (var d : incoming) {
            byId.put(d.getId(), d);
        }
        return new ArrayList<>(byId.values());
    }

    private User findUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));
    }

    private Map<String, List<BookshelfSaveRequest.DecorationDto>> readJson(String json) {
        try {
            if (json == null || json.isBlank()) {
                Map<String, List<BookshelfSaveRequest.DecorationDto>> empty = new LinkedHashMap<>();
                for (int i = 1; i <= 7; i++) empty.put(String.valueOf(i), List.of());
                return empty;
            }
            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, List<BookshelfSaveRequest.DecorationDto>>>() {}
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 실패");
        }
    }

    private String writeJson(Map<String, List<BookshelfSaveRequest.DecorationDto>> map) {
        try {
            // 1~7층 키가 모두 존재하도록 보정
            Map<String, List<BookshelfSaveRequest.DecorationDto>> fixed = new LinkedHashMap<>();
            for (int i = 1; i <= 7; i++) {
                fixed.put(String.valueOf(i), map.getOrDefault(String.valueOf(i), List.of()));
            }
            return objectMapper.writeValueAsString(fixed);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
    }

    /**
     * (기존) 요청 바디 기반 검증: create()에서 사용
     */
    private void validateAndLimitByInventory(User user, BookshelfSaveRequest req) {
        if (decorationInventoryRepository == null) return;

        // 타입별 배치 수 집계 (null-safe)
        Map<Integer, Integer> placed = new HashMap<>();
        Map<String, List<BookshelfSaveRequest.DecorationDto>> decos = Optional
                .ofNullable(req.getDecorations())
                .orElseGet(() -> {
                    Map<String, List<BookshelfSaveRequest.DecorationDto>> m = new LinkedHashMap<>();
                    for (int i = 1; i <= 7; i++) m.put(String.valueOf(i), List.of());
                    return m;
                });
        decos.values().forEach(list -> {
            for (var d : list) {
                placed.merge(d.getType(), 1, Integer::sum);
            }
        });

        // 보유 수 조회
        var invList = decorationInventoryRepository.findAllByUser(user);
        Map<Integer, Integer> ownMap = invList.stream()
                .collect(Collectors.toMap(
                        inv -> inv.getType().getCode(), // 1/2/3
                        inv -> inv.getCount(),
                        Integer::sum
                ));

        int own1 = ownMap.getOrDefault(1, 0);
        int own2 = ownMap.getOrDefault(2, 0);
        int own3 = ownMap.getOrDefault(3, 0);

        if (placed.getOrDefault(1, 0) > own1
                || placed.getOrDefault(2, 0) > own2
                || placed.getOrDefault(3, 0) > own3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 개수가 보유 개수를 초과했습니다.");
        }
    }

    /**
     * (신규) 병합된 Map 기반 검증: update()에서 사용
     */
    private void validateAndLimitByInventoryWithMap(
            User user,
            Map<String, List<BookshelfSaveRequest.DecorationDto>> merged
    ) {
        if (decorationInventoryRepository == null) return;

        // 타입별 배치 수 집계
        Map<Integer, Integer> placed = new HashMap<>();
        merged.values().forEach(list -> {
            for (var d : list) {
                placed.merge(d.getType(), 1, Integer::sum);
            }
        });

        // 보유 수 조회
        var invList = decorationInventoryRepository.findAllByUser(user);
        Map<Integer, Integer> ownMap = invList.stream()
                .collect(Collectors.toMap(
                        inv -> inv.getType().getCode(), // 1/2/3
                        inv -> inv.getCount(),
                        Integer::sum
                ));

        int own1 = ownMap.getOrDefault(1, 0);
        int own2 = ownMap.getOrDefault(2, 0);
        int own3 = ownMap.getOrDefault(3, 0);

        if (placed.getOrDefault(1, 0) > own1
                || placed.getOrDefault(2, 0) > own2
                || placed.getOrDefault(3, 0) > own3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배치 개수가 보유 개수를 초과했습니다.");
        }
    }
}