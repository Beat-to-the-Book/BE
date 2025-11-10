package org.be.calendar.controller;

import lombok.RequiredArgsConstructor;
import org.be.calendar.dto.CalendarCreateRequest;
import org.be.calendar.dto.CalendarMonthlyResponse;
import org.be.calendar.dto.CalendarResponse;
import org.be.calendar.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    // 생성
    @PostMapping
    public ResponseEntity<CalendarResponse> create(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CalendarCreateRequest req
    ) {
        return ResponseEntity.ok(calendarService.create(user.getUsername(), req));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<CalendarResponse> getOne(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(calendarService.getOne(user.getUsername(), id));
    }

    // 전체 조회(해당 사용자)
    @GetMapping
    public ResponseEntity<List<CalendarResponse>> getAll(
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(calendarService.getAll(user.getUsername()));
    }

    // 월별 조회 ?year=2025&month=11
    @GetMapping("/month")
    public ResponseEntity<CalendarMonthlyResponse> getByMonth(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(calendarService.getByMonth(user.getUsername(), year, month));
    }

    // (선택) 수정/삭제
    // @PutMapping("/{id}")
    // @DeleteMapping("/{id}")
}