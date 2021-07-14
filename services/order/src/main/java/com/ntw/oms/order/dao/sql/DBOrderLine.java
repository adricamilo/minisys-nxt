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

package com.ntw.oms.order.dao.sql;

import com.ntw.oms.order.entity.Order;
import com.ntw.oms.order.entity.OrderLine;

import java.sql.Date;
import java.sql.Time;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by anurag on 19/04/17.
 */
public class DBOrderLine {

    private String orderId;
    private int orderLineId;

    private String productId;
    private float quantity;
    private String userId;
    private String status;
    private Date createdDate;
    private Time createdTime;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String id) {
        this.orderId = id;
    }

    public int getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(int orderLineId) {
        this.orderLineId = orderLineId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Time getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Time createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "{" +
                "\"orderId\":" + (orderId == null ? "null" : "\"" + orderId + "\"") + ", " +
                "\"orderLineId\":\"" + orderLineId + "\"" + ", " +
                "\"productId\":" + (productId == null ? "null" : "\"" + productId + "\"") + ", " +
                "\"quantity\":\"" + quantity + "\"" + ", " +
                "\"userId\":" + (userId == null ? "null" : "\"" + userId + "\"") + ", " +
                "\"status\":" + (status == null ? "null" : "\"" + status + "\"") + ", " +
                "\"createdDate\":" + (createdDate == null ? "null" : createdDate) + ", " +
                "\"createdTime\":" + (createdTime == null ? "null" : createdTime) +
                "}";
    }

    public static List<DBOrderLine> createDBOrder(Order order) {
        List<DBOrderLine> dbOrderLines = new LinkedList<>();
        for (OrderLine orderLine : order.getOrderLines()) {
            DBOrderLine dbOrderLine = new DBOrderLine();
            dbOrderLine.setUserId(order.getUserId());
            dbOrderLine.setOrderId(order.getId());
            dbOrderLine.setOrderLineId(orderLine.getId());
            dbOrderLine.setProductId(orderLine.getProductId());
            dbOrderLine.setQuantity(orderLine.getQuantity());
            dbOrderLine.setStatus(order.getStatus().toString());
            if (order.getCreatedDate() != null) {
                dbOrderLine.setCreatedDate(new Date(order.getCreatedDate().getTime()));
                dbOrderLine.setCreatedTime(new Time(order.getCreatedDate().getTime()));
            }
            dbOrderLines.add(dbOrderLine);
        }
        return dbOrderLines;
    }

    public static Order getOrder(String userId, String id, List<DBOrderLine> dbOrderLines) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        if (dbOrderLines.size() > 0) {
            order.setId(dbOrderLines.get(0).getOrderId());
            order.setUserId(dbOrderLines.get(0).getUserId());
            order.setStatus(dbOrderLines.get(0).getStatus());
            long time = 0;
            if (dbOrderLines.get(0).getCreatedDate() != null)
                time += dbOrderLines.get(0).getCreatedDate().getTime();
            if (dbOrderLines.get(0).getCreatedTime() != null)
                time += dbOrderLines.get(0).getCreatedTime().getTime();
            order.setCreatedDate(new java.util.Date(time));
        }
        for (DBOrderLine dbOrderLine : dbOrderLines) {
            OrderLine orderLine = new OrderLine();
            orderLine.setId(dbOrderLine.getOrderLineId());
            orderLine.setProductId(dbOrderLine.getProductId());
            orderLine.setQuantity(dbOrderLine.getQuantity());
            order.getOrderLines().add(orderLine);
        }
        return order;
    }
}
