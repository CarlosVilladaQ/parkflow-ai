package com.parkflow.backend.repository;

import com.parkflow.backend.model.ConfiguracionParqueadero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionRepository extends JpaRepository<ConfiguracionParqueadero, Integer> {
}
