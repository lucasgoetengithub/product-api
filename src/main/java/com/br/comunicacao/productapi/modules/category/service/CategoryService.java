package com.br.comunicacao.productapi.modules.category.service;

import com.br.comunicacao.productapi.config.exception.SucessReponse;
import com.br.comunicacao.productapi.config.exception.ValidationException;
import com.br.comunicacao.productapi.modules.category.dto.CategoryRequest;
import com.br.comunicacao.productapi.modules.category.dto.CategoryResponse;
import com.br.comunicacao.productapi.modules.category.model.Category;
import com.br.comunicacao.productapi.modules.category.repository.CategoryRepository;
import com.br.comunicacao.productapi.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    public CategoryResponse findByIdResponse(Integer id){
        return CategoryResponse.of(findById(id));
    }
    public List<CategoryResponse> findAll(){
        return categoryRepository
                .findAll()
                .stream()
                .map(CategoryResponse::of)
                .collect(Collectors.toList());
    }

    public Category findById(Integer id){
        validateInformedId(id);
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no Category for the given ID."));
    }

    public List<CategoryResponse> findByDescription(String description){
        if (ObjectUtils.isEmpty(description)){
            throw new ValidationException("The Category description must be informed.");
        }
        return categoryRepository
                .findByDescriptionIgnoreCaseContaining(description)
                .stream()
                .map(CategoryResponse::of)
                .collect(Collectors.toList());
    }

    public CategoryResponse save(CategoryRequest request){
        validateCategoryNameInformed(request);
        var category = categoryRepository.save(Category.of(request));
        return CategoryResponse.of(category);
    }

    public CategoryResponse update(CategoryRequest request,
                                   Integer id){
        validateInformedId(id);
        validateCategoryNameInformed(request);
        var category = Category.of(request);
        category.setId(id);
        categoryRepository.save(category);
        return CategoryResponse.of(category);
    }

    private void validateCategoryNameInformed(CategoryRequest request){
        if (ObjectUtils.isEmpty(request.getDescription())) {
            throw new ValidationException("The category description was not informed.");
        }
    }

    public SucessReponse delete(Integer id){
        validateInformedId(id);
        if (productService.existsByCategoryId(id)) {
            throw new ValidationException("You cannot delete this category because it's already defined by a product.");
        }

        categoryRepository.deleteById(id);
        return SucessReponse.create("The category was deleted.");
    }

    private void validateInformedId(Integer id){
        if (ObjectUtils.isEmpty(id)) {
            throw new ValidationException("The category id must be informed.");
        }
    }

}
