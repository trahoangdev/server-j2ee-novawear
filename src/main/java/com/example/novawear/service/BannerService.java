package com.example.novawear.service;

import com.example.novawear.dto.BannerDto;
import com.example.novawear.entity.Banner;
import com.example.novawear.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<BannerDto> findAll() {
        return bannerRepository.findAll().stream()
                .sorted(Comparator.comparing(Banner::getSortOrder).thenComparing(Banner::getId))
                .map(BannerDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BannerDto> findAllActive() {
        return bannerRepository.findAllByActiveTrueOrderBySortOrderAsc().stream()
                .map(BannerDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BannerDto getById(Long id) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found: " + id));
        return BannerDto.from(b);
    }

    @Transactional
    public BannerDto create(BannerDto dto) {
        Banner b = Banner.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : "")
                .linkUrl(dto.getLinkUrl())
                .ctaText(dto.getCtaText())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        b = bannerRepository.save(b);
        return BannerDto.from(b);
    }

    @Transactional
    public BannerDto update(Long id, BannerDto dto) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found: " + id));
        if (dto.getTitle() != null) b.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) b.setSubtitle(dto.getSubtitle());
        if (dto.getImageUrl() != null) b.setImageUrl(dto.getImageUrl());
        if (dto.getLinkUrl() != null) b.setLinkUrl(dto.getLinkUrl());
        if (dto.getCtaText() != null) b.setCtaText(dto.getCtaText());
        if (dto.getSortOrder() != null) b.setSortOrder(dto.getSortOrder());
        if (dto.getActive() != null) b.setActive(dto.getActive());
        b = bannerRepository.save(b);
        return BannerDto.from(b);
    }

    @Transactional
    public void delete(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new IllegalArgumentException("Banner not found: " + id);
        }
        bannerRepository.deleteById(id);
    }
}
