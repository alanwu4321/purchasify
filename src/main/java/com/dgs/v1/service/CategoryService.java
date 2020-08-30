package com.dgs.v1.service;

import com.dgs.v1.model.Category;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends AbstractService {
    public CategoryService() {
        super(Category.class);
    }
}
