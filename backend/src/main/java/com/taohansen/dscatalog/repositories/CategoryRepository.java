package com.taohansen.dscatalog.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.taohansen.dscatalog.entities.Category;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, Long> {
}
