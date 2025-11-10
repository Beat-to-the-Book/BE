package org.be.calendar.repository;

import org.be.auth.model.User;
import org.be.calendar.model.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    List<Calendar> findByUserOrderByStartDateDesc(User user);

    Optional<Calendar> findByIdAndUser(Long id, User user);

    // 월 범위와 겹치는 일정 조회
    @Query("""
           select c from Calendar c
           where c.user = :user
             and c.startDate <= :monthEnd
             and c.endDate >= :monthStart
           order by c.startDate desc
           """)
    List<Calendar> findMonthEntries(User user, LocalDate monthStart, LocalDate monthEnd);
}