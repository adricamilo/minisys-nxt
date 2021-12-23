//////////////////////////////////////////////////////////////////////////////
// Copyright 2020 Anurag Yadav (anurag.yadav@newtechways.com)               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//////////////////////////////////////////////////////////////////////////////

package com.ntw.oms.product.service;

import com.ntw.oms.product.dao.ProductDao;
import com.ntw.oms.product.dao.ProductDaoFactory;
import com.ntw.oms.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by anurag on 30/05/17.
 */
@Configuration
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
// https://www.baeldung.com/spring-boot-failed-to-configure-data-source
@Component
public class ProductServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private static final String REDIS_PRODUCTS_MAP_KEY = "products";

    @Autowired
    private ProductDaoFactory productDaoFactory;

    private ProductDao productDaoBean;

    @Value("${database.type}")
    private String productDBType;

    @Autowired(required = false)
    RedisTemplate<String, Product> redisTemplate;

    @PostConstruct
    public void postConstruct()
    {
        this.productDaoBean = productDaoFactory.getProductDao(productDBType);
    }

    public ProductDao getProductDaoBean() {
        return productDaoBean;
    }

    public List<Product> getProducts() {
        List<Product> products = null;
        Map<Object, Object> productMap = null;
        try {
            if (redisTemplate != null) {
                productMap = redisTemplate.opsForHash().entries(REDIS_PRODUCTS_MAP_KEY);
            }
        } catch(Exception e) {
            logger.error("Unable to access redis cache for getProducts: ", e);
        }
        if (productMap != null && productMap.values().size() > 0) {
            products = new ArrayList<>();
            for (Object productObj : productMap.values()) {
                products.add((Product)productObj);
            }
        } else {
            products = getProductDaoBean().getProducts();
            productMap = new HashMap<>();
            for (Product product : products) {
                productMap.put(product.getId(), product);
            }
            try {
                if (redisTemplate != null) {
                    redisTemplate.opsForHash().putAll(REDIS_PRODUCTS_MAP_KEY, productMap);
                }
            } catch (Exception e) {
                logger.error("Unable to access redis cache for setProducts: ", e);
            }
        }
        products.sort(new Comparator<Product>() {
            @Override
            public int compare(Product o1, Product o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return products;
    }

    public List<Product> getProducts(List<String> ids) {
        List<Product> products;
        List<Object> productObjects = null;
        List<Object> idObjects = new LinkedList<>();
        ids.forEach(id -> idObjects.add(id));
        // Get as many from cache
        try {
            if (redisTemplate != null) {
                productObjects = redisTemplate.opsForHash().multiGet(REDIS_PRODUCTS_MAP_KEY, idObjects);
            }
        } catch(Exception e) {
            logger.error("Unable to access redis cache for getProducts: ", e);
        }
        if (productObjects == null || productObjects.size() == 0) {
            // Get all from DB
            products = getProductDaoBean().getProducts(ids);
            return products;
        }
        if (productObjects.size() == ids.size()) {
            // All products found in cache
            products = new LinkedList<>();
            productObjects.forEach(productObj -> {
                Product product = (Product) productObj;
                products.add(product);
            });
            return products;
        }
        // Find missing products in cache
        Map<String, Product> productMap = new HashMap<>();
        productObjects.forEach(productObj -> {
            Product product = (Product)productObj;
            productMap.put(product.getId(), product);
        });
        List<String> missingIds = new LinkedList<>();
        ids.forEach(id -> {
            Product product = productMap.get(id);
            if (product == null) missingIds.add(id);
        });
        // Get missing products from DB
        List<Product> mapProducts = new LinkedList<>();
        products = getProductDaoBean().getProducts(missingIds);
        products.forEach(product -> productMap.put(product.getId(), product));
        // Get all products from map in the order of ids
        ids.forEach(id -> {
            mapProducts.add(productMap.get(id));
        });
        return mapProducts;
    }

    public Product getProduct(String id) {
        Product product = null;
        try {
            if (redisTemplate != null) {
                product = (Product) redisTemplate.opsForHash().get(REDIS_PRODUCTS_MAP_KEY, id);
            }
        } catch(Exception e) {
            logger.error("Unable to access redis cache for getProduct: ", e);
        }
        if (product == null) {
            product = getProductDaoBean().getProduct(id);
            try {
                if (product != null && redisTemplate != null) {
                    redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, product.getId(), product);
                }
            } catch (Exception e) {
                logger.error("Unable to access redis cache for setProduct: ", e);
            }
        }
        return product;
    }

    public boolean addProduct(Product product) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, product.getId(), product);
            }
        } catch(Exception e) {
            logger.error("Unable to access redis cache for addProduct: ", e);
        }
        return getProductDaoBean().addProduct(product);
    }

    public Product modifyProduct(Product product) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, product.getId(), product);
            }
        } catch(Exception e) {
            logger.error("Unable to access redis cache for modifyProduct: ", e);
        }
        return getProductDaoBean().modifyProduct(product);
    }

    public boolean removeProduct(String id) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, id, null);
            }
        } catch(Exception e) {
            logger.error("Unable to access redis cache for removeProduct: ", e);
        }
        return getProductDaoBean().removeProduct(id);
    }

    public boolean removeProducts() {
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(REDIS_PRODUCTS_MAP_KEY);
            }
        } catch(Exception e) {
            logger.error("Unable to access redis cache for removeProducts: ", e);
        }
        return getProductDaoBean().removeProducts();
    }
}
