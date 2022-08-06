INSERT INTO CATEGORY (id, description) VALUES (1001, 'Comic books');
INSERT INTO CATEGORY (id, description) VALUES (1002, 'Movies');
INSERT INTO CATEGORY (id, description) VALUES (1003, 'Books');


INSERT INTO SUPPLIER (id, name) VALUES (1001, 'Panini comics');
INSERT INTO SUPPLIER (id, name) VALUES (1002, 'Amazon');


INSERT INTO PRODUCT (id, name, fk_supplier, fk_category, quantity_available, created_at) VALUES (1001, 'Crise nas infinitas terras', 1001, 1001, 10, CURRENT_TIMESTAMP);

INSERT INTO PRODUCT (id, name, fk_supplier, fk_category, quantity_available, created_at) VALUES (1002, 'Interestelar', 1002, 1002, 5, CURRENT_TIMESTAMP);

INSERT INTO PRODUCT (id, name, fk_supplier, fk_category, quantity_available, created_at) VALUES (1003, 'Harry Potter', 1002, 1003, 3, CURRENT_TIMESTAMP);