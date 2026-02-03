package com.example.novawear.service;

import com.example.novawear.dto.CategoryDto;
import com.example.novawear.entity.Category;
import com.example.novawear.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(CategoryDto::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        return CategoryDto.from(c);
    }

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        Category c = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        c = categoryRepository.save(c);
        return CategoryDto.from(c);
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        c = categoryRepository.save(c);
        return CategoryDto.from(c);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
