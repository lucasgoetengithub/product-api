package com.br.comunicacao.productapi.modules.supplier.controller;

import com.br.comunicacao.productapi.config.exception.SucessReponse;
import com.br.comunicacao.productapi.modules.supplier.dto.SupplierRequest;
import com.br.comunicacao.productapi.modules.supplier.dto.SupplierResponse;
import com.br.comunicacao.productapi.modules.supplier.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public SupplierResponse save(@RequestBody SupplierRequest request) {
        return supplierService.save(request);
    }

    @GetMapping
    public List<SupplierResponse> findAll(){
        return supplierService.findAll();
    }

    @GetMapping("{id}")
    public SupplierResponse findByIdResponse(@PathVariable Integer id){
        return supplierService.findByIdResponse(id);
    }

    @GetMapping("name/{name}")
    public List<SupplierResponse> findByName(@PathVariable String name){
        return supplierService.findByName(name);
    }

    @DeleteMapping("{id}")
    public SucessReponse delete(@PathVariable Integer id){
        return supplierService.delete(id);
    }

    @PutMapping("{id}")
    public SupplierResponse update(@RequestBody SupplierRequest request, @PathVariable Integer id){
        return supplierService.update(request, id);
    }
}
