package com.oop.web_project.persistence;

import com.oop.web_project.Entities.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
}
