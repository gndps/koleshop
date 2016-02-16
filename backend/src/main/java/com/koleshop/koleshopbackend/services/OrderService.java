package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.common.OrderStatus;
import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.db.models.Address;
import com.koleshop.koleshopbackend.db.models.InventoryProductVariety;
import com.koleshop.koleshopbackend.db.models.Order;
import com.koleshop.koleshopbackend.db.models.OrderItem;
import com.koleshop.koleshopbackend.utils.DatabaseConnectionUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 13/02/16.
 */
public class OrderService {

    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    public Order createNewOrder(Order order) {

        if (order == null) {
            return null;
        }
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean rollbackTransaction = false;

        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.size() == 0) {
            return null;
        }

        try {

            //01. INSERT ORDER ITEMS
            int i = 0;
            String orderItemsQuery = "insert into OrderItems set order_id=?, product_variety_id=?, price=?" +
                    ", quantity=?, order_count=?, available_count=?";
            dbConnection = DatabaseConnection.getConnection();
            dbConnection.setAutoCommit(false);
            preparedStatement = dbConnection.prepareStatement(orderItemsQuery);

            for (OrderItem item : orderItems) {
                preparedStatement.setLong(1, order.getId());
                preparedStatement.setLong(2, item.getVariety().getId());
                preparedStatement.setString(3, item.getName());
                preparedStatement.setString(4, item.getBrand());
                preparedStatement.setFloat(5, item.getPrice());
                preparedStatement.setString(6, item.getQuantity());
                preparedStatement.setInt(7, item.getOrderCount());
                preparedStatement.setInt(8, item.getAvailableCount());
                preparedStatement.addBatch();
                i++;
                if (i % 1000 == 0 || i == orderItems.size()) {
                    preparedStatement.executeBatch(); // Execute every 1000 items.
                }
            }

            //02. INSERT THE ORDER
            String orderQuery = "insert into Orders set customer_id=?, seller_id=?, address_id=?, status_id=?" +
                    ", total_amount=?, not_available_amount=?, delivery_charges=?, carry_bag_charges=?, amount_payable=?" +
                    ", order_time=?, requested_delivery_time=?, actual_delivery_time=?, delivery_or_pickup=?";

            preparedStatement = dbConnection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, order.getCustomerId());
            preparedStatement.setLong(2, order.getSellerId());
            preparedStatement.setLong(3, order.getCustomerAddress().getId());
            preparedStatement.setLong(4, order.getStatusId());
            preparedStatement.setFloat(5, order.getTotalAmount());
            preparedStatement.setFloat(6, order.getNotAvailableAmount());
            preparedStatement.setFloat(7, order.getDeliveryCharges());
            preparedStatement.setFloat(8, order.getCarryBagCharges());
            preparedStatement.setFloat(9, order.getAmountPayable());
            preparedStatement.setDate(10, new Date(order.getOrderTime().getTime()));
            preparedStatement.setDate(11, new Date(order.getRequestedDeliveryTime().getTime()));
            preparedStatement.setDate(12, new Date(order.getActualDeliveryTime().getTime()));
            preparedStatement.setBoolean(13, order.isDeliveryOrPickup());
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs != null && rs.getLong(1) > 0) {
                Long generatedOrderId = rs.getLong(1);
                order.setId(generatedOrderId);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception while creating new order for customer_id = " + order.getCustomerId() + "and seller_id = " + order.getSellerId(), e);
            rollbackTransaction = true;
        }

        if (!rollbackTransaction) {
            //SUCCESS - commit the transaction
            try {
                dbConnection.commit();
                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "some exception while committing create new order for customer_id = " + order.getCustomerId() + "and seller_id = " + order.getSellerId(), e);
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }
        } else {
            //FAILED - rollback the transaction
            try {
                dbConnection.rollback();
                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "some exception while rolling back create new order for customer_id = " + order.getCustomerId() + "and seller_id = " + order.getSellerId(), e);
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }
        }


        return order;
    }

    public Order updateOrder(Order order) {
        if (order == null || order.getId() <= 0) {
            return null;
        }
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean rollbackTransaction = false;

        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.size() == 0) {
            return null;
        }

        try {

            //01. UPDATE THE ORDER ITEMS
            int i = 0;
            String orderItemsQuery = "update OrderItems(order_count,available_count) set values(?,?) where order_id=? and product_variety_id=?";
            dbConnection = DatabaseConnection.getConnection();
            dbConnection.setAutoCommit(false);
            preparedStatement = dbConnection.prepareStatement(orderItemsQuery);

            for (OrderItem item : orderItems) {
                preparedStatement.setInt(1, item.getOrderCount());
                preparedStatement.setInt(2, item.getAvailableCount());
                preparedStatement.setLong(3, order.getId());
                preparedStatement.setLong(4, item.getVariety().getId());
                preparedStatement.addBatch();
                i++;
                if (i % 1000 == 0 || i == orderItems.size()) {
                    preparedStatement.executeBatch(); // Execute every 1000 items.
                }
            }

            //02. UPDATE THE ORDER
            String orderQuery = "update Orders(status_id, total_amount, not_available_amount, amount_payable, actual_delivery_time) set values(?,?,?,?,?)" +
                    " where id=?";

            preparedStatement = dbConnection.prepareStatement(orderQuery);
            preparedStatement.setLong(1, order.getStatusId());
            preparedStatement.setFloat(2, order.getTotalAmount());
            preparedStatement.setFloat(3, order.getNotAvailableAmount());
            preparedStatement.setFloat(4, order.getAmountPayable());
            preparedStatement.setDate(5, new Date(order.getActualDeliveryTime().getTime()));
            preparedStatement.setLong(6, order.getId());
            preparedStatement.execute();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception while updating order with id = " + order.getId(), e);
            rollbackTransaction = true;
        }

        if (!rollbackTransaction) {
            //SUCCESS - commit the transaction
            try {
                dbConnection.commit();
                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "some exception while committing update order with id = " + order.getId(), e);
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }
        } else {
            //FAILED - rollback the transaction
            try {
                dbConnection.rollback();
                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "some exception while rolling back update order with id = " + order.getId(), e);
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }
        }


        return order;
    }

    private enum OrderQueryType {
        Incoming, Pending, Complete, CustomerOrders
    }

    public List<Order> getIncomingOrders(Long userId) {
        return getOrders(userId, OrderQueryType.Incoming, false, 0, 0);
    }

    public List<Order> getPendingOrders(Long userId, boolean pagination, int limit, int offset) {
        return getOrders(userId, OrderQueryType.Pending, pagination, limit, offset);
    }

    public List<Order> getCompleteOrders(Long userId, boolean pagination, int limit, int offset) {
        return getOrders(userId, OrderQueryType.Complete, pagination, limit, offset);
    }

    public List<Order> getMyOrders(Long userId, boolean pagination, int limit, int offset) {
        return getOrders(userId, OrderQueryType.CustomerOrders, pagination, limit, offset);
    }

    public List<Order> getOrders(Long userId, OrderQueryType status,boolean pagination, int limit, int offset) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement;

        List<Order> orders = new ArrayList<>();
        List<Long> orderIds = new ArrayList<>();




        //01. CONFIGURE THE QUERY FOR ORDER IDS
        String query;

        switch (status) {
            case Incoming:
                query = "select id from Orders where seller_id=? and order_status=" + OrderStatus.INCOMING
                        + " order by id desc ";
                break;
            case Pending:
                query =  "select id from Orders where seller_id=? and order_status=" + OrderStatus.ACCEPTED
                        + " order by id desc ";
                break;
            case Complete:
                query = "select id from Orders where seller_id=? and order_status not in (" +
                        OrderStatus.INCOMING + ", " + OrderStatus.ACCEPTED + ") ";
                break;
            case CustomerOrders:
                query = "select id from Orders where customer_id=? order by id desc ";
                break;
            default:
                query = "select id from Orders where seller_id=? order by id desc ";
                break;
        }
        if(pagination) {
            query += "limit ? offset ?";
        }




        //02. FIND THE ORDER IDS
        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            //if pagination is true then set the limit and offset
            if(pagination) {
                preparedStatement.setInt(2, limit);
                preparedStatement.setInt(3, offset);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null) {
                while (resultSet.next()) {
                    orderIds.add(resultSet.getLong(1));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "some problem in getting incoming orders ids for seller_id = " + userId, e);
        }




        //03. FETCH ORDERS FOR THE ORDER IDS FOUND IN STEP 2
        if (orderIds.size() > 0) {


            //03.01 Build the order fetching query
            StringBuilder builder = new StringBuilder();
            for( int i = 0 ; i < orderIds.size(); i++ ) {
                builder.append("?,");
            }
            query = "select o.id as order_id,o.customer_id,o.total_amount,o.not_available_amount,o.delivery_charges,o.carry_bag_charges,o.amount_payable," +
                    "o.order_time,o.requested_delivery_time,o.actual_delivery_time,o.delivery_or_pickup,o.status_id as order_status_id" +
                    "a.id as address_id,a.name as address_name,a.address,a.phone_number,a.country_code,a.nickname as address_nickname,a.gps_long,a.gps_lat," +
                    "oi.name as item_name, oi.brand as item_brand, oi.price as item_price, oi.quantity as item_quantity, oi.order_count, oi.available_count," +
                    "pv.id as pv_id,pv.quantity as pv_quantity,pv.price as pv_price,pv.image as pv_image,pv.limited_stock,pv.valid as pv_valid" +
                    " from Orders o join Address a on o.address_id=a.id" +
                    " join OrderItems oi on o.id=oi.order_id" +
                    " join ProductVariety pv on oi.product_variety_id = pv.id" +
                    " where o.id in (" + builder.deleteCharAt( builder.length() -1 ).toString() +
                    ") order by o.id desc";
            try {
                preparedStatement = dbConnection.prepareStatement(query);
                int index = 1;
                for( Long orderId : orderIds ) {
                    preparedStatement.setLong(index++, orderId);
                }


                //03.02 Execute the order fetching query
                ResultSet rs = preparedStatement.executeQuery();
                HashMap<Long, Integer> ordersHashMap = new HashMap<>();
                int position = 0;
                Order order = null;
                while (rs.next()) {
                    List<OrderItem> orderItems = null;
                    Long orderId = rs.getLong("order_id");
                    if (!ordersHashMap.containsKey(orderId)) {
                        //03.03 EXTRACT ORDER INFO AND ADDRESS AND CREATE NEW ORDER

                        //add the currently looping order to the orders list
                        if (order != null && orderItems != null) {
                            orders.add(order);
                        }

                        //initialize order items list for new order
                        orderItems = new ArrayList<>();

                        //extract order info
                        Long customerId = rs.getLong("customer_id");
                        Float totalAmount = rs.getFloat("total_amount");
                        Float notAvailableAmount = rs.getFloat("not_available_amount");
                        Float deliveryCharges = rs.getFloat("delivery_charges");
                        Float carryBagCharges = rs.getFloat("carry_bag_charges");
                        Float amountPayable = rs.getFloat("amount_payable");
                        int orderStatusId = rs.getInt("order_status_id");
                        java.util.Date orderTime = getDateFromSqlTimestamp(rs.getTimestamp("order_time"));
                        java.util.Date requestedDeliveryTime = rs.getTimestamp("requested_delivery_time");
                        java.util.Date actualDeliveryTime = rs.getTimestamp("actual_delivery_time");
                        boolean deliveryOrPickup = rs.getBoolean("delivery_or_pickup");

                        //extract address info
                        Long addressId = rs.getLong("address_id");
                        String addressName = rs.getString("address_name");
                        String addressString = rs.getString("address");
                        Long phoneNumber = rs.getLong("phone_number");
                        int countryCode = rs.getInt("country_code");
                        String nickname = rs.getString("address_nickname");
                        Double gpsLong = rs.getDouble("gps_long");
                        Double gpsLat = rs.getDouble("gps_lat");
                        Address address = new Address(addressId, customerId, addressName, addressString
                                , Integer.parseInt(Constants.ADDRESS_TYPE_CUSTOMER), phoneNumber
                                , countryCode, nickname, gpsLong, gpsLat);

                        //create new order
                        order = new Order(orderId, customerId, userId, address, orderStatusId, totalAmount, notAvailableAmount, deliveryCharges,
                                carryBagCharges, amountPayable, orderTime, requestedDeliveryTime, actualDeliveryTime, deliveryOrPickup, null);
                        orders.add(order);
                        ordersHashMap.put(orderId, position);
                        position++;
                    } else {
                        //THIS ORDER ALREADY EXISTS...ONLY EXTRACT THE ORDER ITEM AND PRODUCT VARIETY
                    }



                    //03.04 Extract inventory product variety and order item

                    //inventory product variety
                    Long productVarietyId = rs.getLong("pv_id");
                    String productVarietyQuantity = rs.getString("pv_quantity");
                    Float productVarietyPrice = rs.getFloat("pv_price");
                    String productVarietyImageUrl = rs.getString("pv_image");
                    boolean productVarietyLimitedStock = rs.getBoolean("limited_stock");
                    boolean productVarietyValid = rs.getBoolean("pv_valid");
                    InventoryProductVariety productVariety = new InventoryProductVariety(productVarietyId, productVarietyQuantity, productVarietyPrice,
                            productVarietyImageUrl, productVarietyValid, productVarietyLimitedStock);


                    //order item
                    String itemName = rs.getString("item_name");
                    String itemBrand = rs.getString("item_brand");
                    Float itemPrice = rs.getFloat("item_price");
                    String itemQuantity = rs.getString("item_quantity");
                    int orderCount = rs.getInt("order_count");
                    int availableCount = rs.getInt("available_count");
                    OrderItem item = new OrderItem(productVariety, itemName, itemBrand, itemPrice, itemQuantity, orderCount, availableCount);
                    orderItems.add(item);

                }
                //add the last order in the loop to the list
                orders.add(order);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "some problem while getting the complete order objects for user_id = " + userId);
                orders = null;
            }
        }
        return orders;
    }

    private java.util.Date getDateFromSqlTimestamp(Timestamp timestamp) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(timestamp.getTime());
        java.util.Date dt = start.getTime();
        return dt;
    }
}
