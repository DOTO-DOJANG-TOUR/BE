package com.doto.domain.festival.repository;

import com.doto.domain.festival.entity.Festival;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    Optional<Festival> findByContentId(Long contentId);
}
