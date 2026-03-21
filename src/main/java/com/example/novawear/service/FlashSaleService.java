package com.example.novawear.service;

import com.example.novawear.dto.FlashSaleDto;
import com.example.novawear.entity.FlashSale;
import com.example.novawear.entity.FlashSaleProduct;
import com.example.novawear.entity.Product;
import com.example.novawear.repository.FlashSaleRepository;
import com.example.novawear.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<FlashSaleDto> findActiveNow() {
        return flashSaleRepository.findActiveNow(Instant.now()).stream()
                .map(FlashSaleDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FlashSaleDto> findAll(Pageable pageable) {
        return flashSaleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(FlashSaleDto::from);
    }

    @Transactional(readOnly = true)
    public FlashSaleDto getById(Long id) {
        FlashSale fs = flashSaleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flash sale not found: " + id));
        return FlashSaleDto.from(fs);
    }

    @Transactional
    public FlashSaleDto create(FlashSaleDto dto) {
        FlashSale fs = FlashSale.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .discountPercent(dto.getDiscountPercent())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        fs = flashSaleRepository.save(fs);
        return FlashSaleDto.from(fs);
    }

    @Transactional
    public FlashSaleDto update(Long id, FlashSaleDto dto) {
        FlashSale fs = flashSaleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flash sale not found: " + id));
        if (dto.getName() != null) fs.setName(dto.getName());
        if (dto.getStartTime() != null) fs.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) fs.setEndTime(dto.getEndTime());
        if (dto.getDiscountPercent() != null) {
            fs.setDiscountPercent(dto.getDiscountPercent());
            for (FlashSaleProduct p : fs.getProducts()) {
                p.setSalePrice(
                        p.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(100 - dto.getDiscountPercent()))
                                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR)
                );
            }
        }
        if (dto.getActive() != null) fs.setActive(dto.getActive());
        fs = flashSaleRepository.save(fs);
        return FlashSaleDto.from(fs);
    }

    @Transactional
    public FlashSaleDto addProduct(Long flashSaleId, Long productId, Integer quantity) {
        FlashSale fs = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new IllegalArgumentException("Flash sale not found: " + flashSaleId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        BigDecimal salePrice = product.getPrice()
                .multiply(BigDecimal.valueOf(100 - fs.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        if (fs.getProducts().stream().anyMatch(p -> p.getProduct().getId().equals(productId))) {
            throw new IllegalArgumentException("Sản phẩm này đã có trong Flash Sale.");
        }

        FlashSaleProduct fsp = FlashSaleProduct.builder()
                .flashSale(fs)
                .product(product)
                .salePrice(salePrice)
                .quantity(quantity != null ? quantity : 50)
                .build();
        fs.getProducts().add(fsp);
        fs = flashSaleRepository.save(fs);
        return FlashSaleDto.from(fs);
    }

    @Transactional
    public void removeProduct(Long flashSaleId, Long productItemId) {
        FlashSale fs = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new IllegalArgumentException("Flash sale not found: " + flashSaleId));
        fs.getProducts().removeIf(p -> p.getId().equals(productItemId));
        flashSaleRepository.save(fs);
    }

    @Transactional
    public void delete(Long id) {
        flashSaleRepository.deleteById(id);
    }
}
