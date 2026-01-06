package com.kuspid.beat.repository;

import com.kuspid.beat.model.Beat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeatRepository extends JpaRepository<Beat, Long> {
}
