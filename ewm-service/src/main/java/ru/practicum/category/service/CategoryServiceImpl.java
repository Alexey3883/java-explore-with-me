package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.IllegalArgumentException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Получение запроса на добавление новой категории");
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new IllegalArgumentException("Категория с таким именем уже существует");
        }
        log.info("Категория добавлена");
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));

    }

    @Override
    @Transactional
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository
                .findAll(PageRequest.of(from, size))
                .getContent().stream()
                .map(categoryMapper::toCategoryDto).toList();
    }

    @Override
    @Transactional
    public CategoryDto getCategory(Long catId) {
        return categoryMapper.toCategoryDto(
                categoryRepository.findById(catId)
                        .orElseThrow(() -> new NotFoundException("Category not found"))
        );
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Category not found"));
        if (eventRepository.findByCategoryId(catId) != null) {
            throw new IllegalArgumentException("Нельзя удалить категорию, которая уже используется в событиях");
        }
        categoryRepository.deleteById(catId);
        log.info("Category was deleted");
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {

        categoryDto.setId(catId);
        Category existCategory = categoryRepository.findById(categoryDto.getId()).orElseThrow(
                () -> new NotFoundException("Category not found"));

        if (!existCategory.getName().equals(categoryDto.getName()) && categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Категория с таким именем уже существует");
        }

        existCategory.setName(categoryDto.getName());

        Category savedCategory = categoryRepository.save(existCategory);
        return categoryMapper.toCategoryDto(savedCategory);
    }

}
