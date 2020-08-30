package com.dgs.v1.service;

import com.dgs.v1.model.Supplier;
import org.springframework.stereotype.Service;

@Service
public class SupplierService extends AbstractService {
    public SupplierService() {
        super(Supplier.class);
    }
}
