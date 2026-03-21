package com.example.novawear.repository;

import com.example.novawear.entity.Subscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Subscriber> findAllByOrderBySubscribedAtDesc(Pageable pageable);

    long countByActiveTrue();
}
