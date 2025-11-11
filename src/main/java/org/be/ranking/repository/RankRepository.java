package org.be.ranking.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RankRepository extends Repository<org.be.auth.model.User, Long> {

    // Projection Interfaces
    interface PointsAgg {
        String getUserId();
        String getUsername();
        Long getPoints();
    }

    interface BooksAgg {
        String getUserId();
        String getUsername();
        Long getTotalCount();
        Long getPurchaseCount();
        Long getRentalCount();
    }

    // ✅ 월간 포인트 합산 (PointEvent 기준, JPQL)
    @Query("""
        select u.userId as userId,
               u.username as username,
               coalesce(sum(e.delta), 0) as points
        from PointEvent e
            join e.user u
        where e.createdAt >= :start and e.createdAt < :end
        group by u.id, u.userId, u.username
        order by points desc
        """)
    List<PointsAgg> findMonthlyPointsAgg(@Param("start") LocalDateTime start,
                                         @Param("end")   LocalDateTime end);

    // ✅ 월간 구매/대여 합산 (native 쿼리)
    @Query(value = """
        select
            u.user_id   as userId,
            u.username  as username,
            (coalesce(p.cnt,0) + coalesce(r.cnt,0)) as totalCount,
            coalesce(p.cnt,0) as purchaseCount,
            coalesce(r.cnt,0) as rentalCount
        from users u
        left join (
            select user_id, count(*) as cnt
            from purchase
            where purchase_date >= :start and purchase_date < :end
            group by user_id
        ) p on p.user_id = u.id
        left join (
            select user_id, count(*) as cnt
            from rental
            where rental_date >= :start and rental_date < :end
            group by user_id
        ) r on r.user_id = u.id
        where (coalesce(p.cnt,0) + coalesce(r.cnt,0)) > 0
        order by totalCount desc, username asc
        """,
            nativeQuery = true)
    List<BooksAgg> findMonthlyBooksAgg(@Param("start") LocalDateTime start,
                                       @Param("end")   LocalDateTime end);
}