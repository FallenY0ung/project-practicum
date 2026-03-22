package ru.tbank.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.Blinds;

public interface BlindsRepository extends JpaRepository<Blinds, Long> {
}
