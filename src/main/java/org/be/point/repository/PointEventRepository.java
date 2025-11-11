package org.be.point.repository;

import org.be.point.model.PointEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointEventRepository extends JpaRepository<PointEvent, Long> {}