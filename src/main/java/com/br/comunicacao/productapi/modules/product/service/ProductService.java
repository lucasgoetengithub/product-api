package com.br.comunicacao.productapi.modules.product.service;

import com.br.comunicacao.productapi.config.exception.SucessReponse;
import com.br.comunicacao.productapi.config.exception.ValidationException;
import com.br.comunicacao.productapi.modules.category.service.CategoryService;
import com.br.comunicacao.productapi.modules.product.dto.*;
import com.br.comunicacao.productapi.modules.product.model.Product;
import com.br.comunicacao.productapi.modules.product.repository.ProductRepository;
import com.br.comunicacao.productapi.modules.sales.client.SalesClient;
import com.br.comunicacao.productapi.modules.sales.dto.SalesConfirmationDTO;
import com.br.comunicacao.productapi.modules.sales.dto.SalesProductsResponse;
import com.br.comunicacao.productapi.modules.sales.enums.SalesStatus;
import com.br.comunicacao.productapi.modules.sales.rabbitmq.SalesConfirmationSender;
import com.br.comunicacao.productapi.modules.supplier.service.SupplierService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.br.comunicacao.productapi.config.RequestUtil.getCurrentRequest;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class ProductService {

    private static final Integer ZERO = 0;
    private static final String TRANSACTION_ID = "transactionid";
    private static final String SERVICE_ID = "serviceid";
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    SalesConfirmationSender salesConfirmationSender;

    @Autowired
    SalesClient salesClient;

    public ProductResponse findByIdResponse(Integer id){
        return ProductResponse.of(findById(id));
    }

    public List<ProductResponse> findAll(){
        return productRepository
                .findAll()
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public Product findById(Integer id){
        validateInformedId(id);

        return productRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no product for the given ID."));
    }

    public List<ProductResponse> findByName(String name){
        if (isEmpty(name)){
            throw new ValidationException("The product description must be informed.");
        }
        return productRepository
                .findByNameIgnoreCaseContaining(name)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findBySupplierId(Integer supplierId){
        if (isEmpty(supplierId)){
            throw new ValidationException("The product supplier must be informed.");
        }
        return productRepository
                .findBySupplierId(supplierId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByCategoryId(Integer categoryId){
        if (isEmpty(categoryId)){
            throw new ValidationException("The product category must be informed.");
        }
        return productRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public ProductResponse save(ProductRequest request){
        validateProductDataInformed(request);
        validateCategoryAndSupplierIdInformed(request);
        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());
        var product = productRepository.save(Product.of(request, category, supplier));
        return ProductResponse.of(product);
    }

    public ProductResponse update(ProductRequest request,
                                  Integer id){
        validateProductDataInformed(request);
        validateCategoryAndSupplierIdInformed(request);
        validateInformedId(id);
        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());
        var product = Product.of(request, category, supplier);
        product.setId(id);
        productRepository.save(product);
        return ProductResponse.of(product);
    }

    private void validateProductDataInformed(ProductRequest request){
        if (isEmpty(request.getName())) {
            throw new ValidationException("The Product's description was not informed.");
        }

        if (isEmpty(request.getQuantityAvailable())) {
            throw new ValidationException("The Product's QuantityAvailable was not informed.");
        }

        if (request.getQuantityAvailable() <= ZERO) {
            throw new ValidationException("The quantity should not be less or equal to zero.");
        }
    }

    private void validateCategoryAndSupplierIdInformed(ProductRequest request){
        if (isEmpty(request.getCategoryId())) {
            throw new ValidationException("The Category Id was not informed.");
        }

        if (isEmpty(request.getSupplierId())) {
            throw new ValidationException("The Supplier Id was not informed.");
        }
    }

    public Boolean existsByCategoryId(Integer categoryId){
        return productRepository.existsByCategoryId(categoryId);
    }

    public Boolean existsBySupplierId(Integer supplierId){
        return productRepository.existsBySupplierId(supplierId);
    }

    public SucessReponse delete(Integer id){
        validateInformedId(id);
        if (!productRepository.existsById(id)) {
            throw new ValidationException("The product does not exists.");
        }
        var sales = getSalesByProductId(id);
        if (!isEmpty(sales.getSalesIds())) {
            throw new ValidationException("The product cannot be deleted. There are sales for it.");
        }

        productRepository.deleteById(id);
        return SucessReponse.create("The product was deleted.");
    }

    private void validateInformedId(Integer id){
        if (isEmpty(id)) {
            throw new ValidationException("The product id must be informed.");
        }
    }

    public void updateProductStock(ProductStockDTO productStockDTO){
        try {
            validateStockUpdateData(productStockDTO);
            updateStock(productStockDTO);
        }catch (Exception ex){
            log.error("Error while trying to update stock for message with error {}", ex.getMessage(), ex);
            var rejectMessage = new SalesConfirmationDTO(productStockDTO.getSalesId(), SalesStatus.REJECTED, productStockDTO.getTransactionid());
            salesConfirmationSender.sendSalesConfirmationMessage(rejectMessage);
        }
    }

    @Transactional
    private void updateStock(ProductStockDTO productStockDTO) {
        var productsForUpadates = new ArrayList<Product>();
        productStockDTO
                .getProducts()
                .forEach(salesProduct -> {
                    var existingProduct = findById(salesProduct.getProductId());
                    validateQuantityInStock(salesProduct,existingProduct);
                    existingProduct.updateStock(salesProduct.getQuantity());
                    productsForUpadates.add(existingProduct);

                });
        if (!isEmpty(productsForUpadates)) {
            productRepository.saveAll(productsForUpadates);
            var approvedMessage = new SalesConfirmationDTO(productStockDTO.getSalesId(),
                    SalesStatus.APPROVED, productStockDTO.getTransactionid());
            salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
        }
    }

    private void validateQuantityInStock(ProductQuantityDTO salesProduct, Product existingProduct){
        if (salesProduct.getQuantity() > existingProduct.getQuantityAvailable()) {
            throw new ValidationException(
                    String.format("The product %s is out of stock", existingProduct.getId())
            );
        }
    }

    @Transactional
    private void validateStockUpdateData(ProductStockDTO productStockDTO){
        if (isEmpty(productStockDTO) || isEmpty(productStockDTO.getSalesId())) {
            throw new ValidationException("The product dara and sales ID must be informed.");
        }

        if (isEmpty(productStockDTO.getProducts())) {
            throw new ValidationException("The sales products must be informed.");
        }

        productStockDTO
                .getProducts()
                .forEach(salesProduct -> {
                    if (isEmpty(salesProduct.getQuantity())
                            || isEmpty(salesProduct.getProductId())) {
                        throw new ValidationException("The product ID and the quantity must be informed.");
                    }
                });
    }

    public ProductSalesResponse findProductSales(Integer id){
        var product = findById(id);
        var sales = getSalesByProductId(product.getId());
        return ProductSalesResponse.of(product, sales.getSalesIds());
    }

    public SalesProductsResponse getSalesByProductId(Integer productId){
        try {
            var currentRequest = getCurrentRequest();
            var transactionid = currentRequest.getHeader(TRANSACTION_ID);
            var serviceid = currentRequest.getAttribute(SERVICE_ID);
            log.info("Sending GET request to orders by productID with data {} | transactionID: {} | serviceID {}",
                    productId, transactionid, serviceid);


            var response = salesClient.findSalesByProductId(productId)
                    .orElseThrow(() -> new ValidationException("The sales was not found by this product."));

            log.info("Response GET request to orders by productID with data {} | transactionID: {} | serviceID {}",
                    new ObjectMapper().writeValueAsString(response), transactionid, serviceid);

            return response;
        }catch (Exception ex){
            ex.printStackTrace();
            throw  new ValidationException("Sales could not be found.");
        }
    }

    public SucessReponse checkProductsStock(ProductCheckStockRequest productCheckStockRequest) {
        try {
            var currentRequest = getCurrentRequest();
            var transactionid = currentRequest.getHeader(TRANSACTION_ID);
            var serviceid = currentRequest.getAttribute(SERVICE_ID);
            log.info("Request to POST product stock with data {} | transactionID: {} | serviceID {}",
                    new ObjectMapper().writeValueAsString(productCheckStockRequest), transactionid, serviceid);

            if (isEmpty(productCheckStockRequest) || isEmpty(productCheckStockRequest.getProducts())) {
                throw new ValidationException("The request data must be informed.");
            }
            productCheckStockRequest
                    .getProducts()
                    .forEach(this::validateStock);

            var response = SucessReponse.create("The stock is ok!");
            log.info("Response to POST product stock with data {} | transactionID: {} | serviceID {}",
                    new ObjectMapper().writeValueAsString(productCheckStockRequest), transactionid, serviceid);

            return response;
        }catch (Exception ex){
            throw new ValidationException(ex.getMessage());
        }
    }

    private void validateStock(ProductQuantityDTO productQuantityDTO){
        if (isEmpty(productQuantityDTO.getProductId()) || isEmpty(productQuantityDTO.getQuantity())) {
            throw new ValidationException("Product ID and quantity must be informed.");
        }
        var product = findById(productQuantityDTO.getProductId());
        if (productQuantityDTO.getQuantity() > product.getQuantityAvailable() ) {
            throw new ValidationException(String.format("The product %s is out of stock.", product.getId()));
        }
    }

}
