package com.br.comunicacao.productapi.modules.category.controller;

import com.br.comunicacao.productapi.config.exception.SucessReponse;
import com.br.comunicacao.productapi.modules.category.dto.CategoryRequest;
import com.br.comunicacao.productapi.modules.category.dto.CategoryResponse;
import com.br.comunicacao.productapi.modules.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public CategoryResponse save(@RequestBody CategoryRequest request) {
        return categoryService.save(request);
    }

    @GetMapping
    public List<CategoryResponse> findAll(){
        return categoryService.findAll();
    }

    @GetMapping("{id}")
    public CategoryResponse findByIdResponse(@PathVariable Integer id){
        return categoryService.findByIdResponse(id);
    }

    @GetMapping("description/{description}")
    public List<CategoryResponse> findByName(@PathVariable String description){
        return categoryService.findByDescription(description);
    }

    @DeleteMapping("{id}")
    public SucessReponse delete(@PathVariable Integer id){
        return categoryService.delete(id);
    }

    @PutMapping("{id}")
    public CategoryResponse update(@RequestBody CategoryRequest request, @PathVariable Integer id){
        return categoryService.update(request, id);
    }

}
