package com.br.comunicacao.productapi.modules.supplier.service;

import com.br.comunicacao.productapi.config.exception.SucessReponse;
import com.br.comunicacao.productapi.config.exception.ValidationException;
import com.br.comunicacao.productapi.modules.product.service.ProductService;
import com.br.comunicacao.productapi.modules.supplier.dto.SupplierRequest;
import com.br.comunicacao.productapi.modules.supplier.dto.SupplierResponse;
import com.br.comunicacao.productapi.modules.supplier.model.Supplier;
import com.br.comunicacao.productapi.modules.supplier.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductService productService;

    public SupplierResponse findByIdResponse(Integer id){
        return SupplierResponse.of(findById(id));
    }

    public List<SupplierResponse> findAll(){
        return supplierRepository
                .findAll()
                .stream()
                .map(SupplierResponse::of)
                .collect(Collectors.toList());
    }

    public Supplier findById(Integer id){
        validateInformedId(id);

        return supplierRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no supplier for the given ID."));
    }

    public List<SupplierResponse> findByName(String name){
        if (ObjectUtils.isEmpty(name)){
            throw new ValidationException("The supplier description must be informed.");
        }
        return supplierRepository
                .findByNameIgnoreCaseContaining(name)
                .stream()
                .map(SupplierResponse::of)
                .collect(Collectors.toList());
    }

    public SupplierResponse save(SupplierRequest request){
        validateSupplierNameInformed(request);
        var supplier = supplierRepository.save(Supplier.of(request));
        return SupplierResponse.of(supplier);
    }

    public SupplierResponse update(SupplierRequest request,
                                   Integer id){
        validateInformedId(id);
        validateSupplierNameInformed(request);
        var supplier = Supplier.of(request);
        supplier.setId(id);
        supplierRepository.save(supplier);
        return SupplierResponse.of(supplier);
    }

    private void validateSupplierNameInformed(SupplierRequest request){
        if (ObjectUtils.isEmpty(request.getName())) {
            throw new ValidationException("The Supplier description was not informed.");
        }
    }

    public SucessReponse delete(Integer id){
        validateInformedId(id);
        if (productService.existsBySupplierId(id)) {
            throw new ValidationException("You cannot delete this supplier because it's already defined by a product.");
        }

        supplierRepository.deleteById(id);
        return SucessReponse.create("The supplier was deleted.");
    }

    private void validateInformedId(Integer id){
        if (ObjectUtils.isEmpty(id)) {
            throw new ValidationException("The supplier id must be informed.");
        }
    }


}
