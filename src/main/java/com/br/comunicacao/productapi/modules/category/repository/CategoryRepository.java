package com.br.comunicacao.productapi.modules.category.repository;

import com.br.comunicacao.productapi.modules.category.dto.CategoryResponse;
import com.br.comunicacao.productapi.modules.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByDescriptionIgnoreCaseContaining(String description);
}
