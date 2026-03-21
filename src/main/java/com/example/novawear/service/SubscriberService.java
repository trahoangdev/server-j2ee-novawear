package com.example.novawear.service;

import com.example.novawear.dto.SubscriberDto;
import com.example.novawear.entity.Subscriber;
import com.example.novawear.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;

    @Transactional
    public SubscriberDto subscribe(String email) {
        var existing = subscriberRepository.findByEmail(email);
        if (existing.isPresent()) {
            Subscriber s = existing.get();
            if (!s.getActive()) {
                s.setActive(true);
                s = subscriberRepository.save(s);
            }
            return SubscriberDto.from(s);
        }
        Subscriber s = Subscriber.builder().email(email).build();
        s = subscriberRepository.save(s);
        return SubscriberDto.from(s);
    }

    @Transactional
    public void unsubscribe(String email) {
        subscriberRepository.findByEmail(email).ifPresent(s -> {
            s.setActive(false);
            subscriberRepository.save(s);
        });
    }

    @Transactional(readOnly = true)
    public Page<SubscriberDto> findAll(Pageable pageable) {
        return subscriberRepository.findAllByOrderBySubscribedAtDesc(pageable)
                .map(SubscriberDto::from);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return subscriberRepository.countByActiveTrue();
    }

    @Transactional
    public void delete(Long id) {
        subscriberRepository.deleteById(id);
    }
}
