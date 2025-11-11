// org/be/decoration/service/DecorationService.java
package org.be.decoration.service;

import lombok.RequiredArgsConstructor;
import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.decoration.dto.BuyDecorationRequest;
import org.be.decoration.dto.BuyDecorationResponse;
import org.be.decoration.dto.MyDecorationCountsResponse;
import org.be.decoration.model.DecorationInventory;
import org.be.decoration.model.DecorationType;
import org.be.decoration.repository.DecorationInventoryRepository;
import org.be.point.model.PointEvent;
import org.be.point.repository.PointEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DecorationService {

    private final UserRepository userRepository;
    private final DecorationInventoryRepository inventoryRepository;
    private final PointEventRepository pointEventRepository; // 사용 내역 기록용 (선택)

    @Transactional
    public BuyDecorationResponse buy(String userId, BuyDecorationRequest req) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패"));

        var type = DecorationType.fromCode(req.decorationType());
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 장식품 타입입니다.");
        }

        int price = type.getPrice();
        if (user.getTotalPoints() < price) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "포인트가 부족합니다. 필요한 포인트: " + price + "P, 현재 포인트: " + user.getTotalPoints() + "P"
            );
        }

        // 포인트 차감
        user.addPoints(-price);

        // 인벤토리 upsert(+1)
        var inv = inventoryRepository.findByUserAndType(user, type)
                .orElseGet(() -> inventoryRepository.save(new DecorationInventory(user, type)));
        inv.increase(1);

        // 포인트 사용 이벤트(음수) 기록 - 선택
        pointEventRepository.save(new PointEvent(user, -price, PointEvent.Reason.DECORATION_PURCHASE));

        return new BuyDecorationResponse(type.getCode(), 1, inv.getCount(), user.getTotalPoints());
    }

    @Transactional(readOnly = true)
    public MyDecorationCountsResponse getMyCounts(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패"));

        // 기본 0 세팅
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 0); map.put(2, 0); map.put(3, 0);

        inventoryRepository.findAll().stream()
                .filter(inv -> inv.getUser().getId().equals(user.getId()))
                .forEach(inv -> map.put(inv.getType().getCode(), inv.getCount()));

        return new MyDecorationCountsResponse(map);
    }
}