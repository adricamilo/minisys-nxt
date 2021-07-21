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

    @Autowired
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
            productMap = redisTemplate.opsForHash().entries(REDIS_PRODUCTS_MAP_KEY);
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
                redisTemplate.opsForHash().putAll(REDIS_PRODUCTS_MAP_KEY, productMap);
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

    public Product getProduct(String id) {
        Product product = null;
        try {
            product = (Product) redisTemplate.opsForHash().get(REDIS_PRODUCTS_MAP_KEY, id);
        } catch(Exception e) {
            logger.error("Unable to access redis cache for getProduct: ", e);
        }
        if (product == null) {
            product = getProductDaoBean().getProduct(id);
            try {
                redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, product.getId(), product);
            } catch (Exception e) {
                logger.error("Unable to access redis cache for setProduct: ", e);
            }
        }
        return product;
    }

    public boolean addProduct(Product product) {
        try {
            redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, product.getId(), product);
        } catch(Exception e) {
            logger.error("Unable to access redis cache for addProduct: ", e);
        }
        return getProductDaoBean().addProduct(product);
    }

    public Product modifyProduct(Product product) {
        try {
            redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, product.getId(), product);
        } catch(Exception e) {
            logger.error("Unable to access redis cache for modifyProduct: ", e);
        }
        return getProductDaoBean().modifyProduct(product);
    }

    public boolean removeProduct(String id) {
        try {
            redisTemplate.opsForHash().put(REDIS_PRODUCTS_MAP_KEY, id, null);
        } catch(Exception e) {
            logger.error("Unable to access redis cache for removeProduct: ", e);
        }
        return getProductDaoBean().removeProduct(id);
    }

    public boolean removeProducts() {
        try {
            redisTemplate.delete(REDIS_PRODUCTS_MAP_KEY);
        } catch(Exception e) {
            logger.error("Unable to access redis cache for removeProducts: ", e);
        }
        return getProductDaoBean().removeProducts();
    }
}
