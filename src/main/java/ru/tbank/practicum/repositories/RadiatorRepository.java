package ru.tbank.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.practicum.entity.Radiator;

public interface RadiatorRepository extends JpaRepository<Radiator, Long> {
}
