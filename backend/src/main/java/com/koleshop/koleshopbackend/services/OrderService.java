package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.common.OrderStatus;
import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.db.models.Address;
import com.koleshop.koleshopbackend.db.models.BuyerSettings;
import com.koleshop.koleshopbackend.db.models.InventoryProductVariety;
import com.koleshop.koleshopbackend.db.models.KoleResponse;
import com.koleshop.koleshopbackend.db.models.Order;
import com.koleshop.koleshopbackend.db.models.OrderItem;
import com.koleshop.koleshopbackend.db.models.SellerSettings;
import com.koleshop.koleshopbackend.utils.CommonUtils;
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

    public Order createNewOrder(Order order, int addMinutes, int addHours) {

        //01. update buyer settings
        //02. save/update buyer address
        //03. insert order items and insert order

        if (order == null) {
            logger.log(Level.SEVERE, "Order not created - order was null");
            return null;
        }
        if(order.getAddress() == null) {
            logger.log(Level.SEVERE, "Order not created - order address was null");
            return null;
        }
        if(order.getBuyerSettings() == null) {
            logger.log(Level.SEVERE, "Order not created - order buyer settings were null");
            return null;
        }

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean rollbackTransaction = false;

        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.size() == 0) {
            logger.log(Level.SEVERE, "Order not created - order items size was zero");
            return null;
        }

        try {

            //01. UPDATE BUYER SETTINGS IF NOT EXIST
            BuyerSettings buyerSettings = order.getBuyerSettings();
            if(buyerSettings==null) return null;
            boolean buyerSettingsUpdated = new BuyerService().updateBuyerSettings(buyerSettings);

            if(!buyerSettingsUpdated) {
                logger.log(Level.SEVERE, "Order not created - couldn't update buyer settings  for userId" + order.getAddress().getUserId());
                return null;
            }

            //02. SAVE OR UPDATE ADDRESS
            KoleResponse response = new CommonService().saveOrUpdateAddress(order.getAddress());
            if(response.getSuccess()) {
                order.setAddress((Address) response.getData()); //address id generated
            } else {
                logger.log(Level.SEVERE, "Order not created - address couldn't be saved/updated for userId" + order.getAddress().getUserId());
                return null;
            }

            //03.01 GET ORDER NUMBER OF THE DAY
            int orderNumberOfTheDay;
            String getOrderNumberQuery = "select order_number from Orders where seller_id=? and date(order_time)=date(?) order by order_number desc limit 1";
            dbConnection = DatabaseConnection.getConnection();
            dbConnection.setAutoCommit(false);
            preparedStatement = dbConnection.prepareStatement(getOrderNumberQuery);
            preparedStatement.setFloat(1, order.getSellerSettings().getUserId());
            preparedStatement.setDate(2, new Date(new java.util.Date().getTime()));
            ResultSet rsOrderNumber = preparedStatement.executeQuery();
            if(rsOrderNumber!=null && rsOrderNumber.first()) {
                String latestOrderNumber = rsOrderNumber.getString(1);
                try {
                    orderNumberOfTheDay = Integer.parseInt(latestOrderNumber.split("-")[2]) + 1;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Order not created - problem in parsing order number of the day for userId" + order.getAddress().getUserId(), e);
                    return null;
                }
            } else {
                //this is the first order for the day
                orderNumberOfTheDay = 1;
            }


            //03.02 INSERT THE ORDER
            String orderQuery = "insert into Orders set order_number=?, customer_id=?, seller_id=?, address_id=?, status_id=?" +
                    ",delivery_charges=?,carry_bag_charges=?,not_available_amount=?,total_amount=?,amount_payable=?" +
                    ",home_delivery=?,asap=?,order_time,requested_delivery_time=?";


            //prepare data
            String sellerNameWithoutSpaces = order.getSellerSettings().getAddress().getName().replaceAll(" ", "");
            String orderNumber;
            String sellerShortCode;
            if(sellerNameWithoutSpaces.length()>2) {
                sellerShortCode = sellerNameWithoutSpaces.substring(0, 3);
            } else {
                sellerShortCode = CommonUtils.getDigestedString(sellerNameWithoutSpaces);
            }
            orderNumber = sellerShortCode + "-" + CommonUtils.getDateTimeDdMmYy(Constants.TIME_ZONE_STRING_INDIA) + "-" + orderNumberOfTheDay;
            java.util.Date orderDate = new java.util.Date();


            //prepare query
            int index=1;
            preparedStatement = dbConnection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            logger.info("will run create order query = " + preparedStatement.toString());
            preparedStatement.setString(index++, orderNumber);
            preparedStatement.setLong(index++, order.getBuyerSettings().getUserId());
            preparedStatement.setLong(index++, order.getSellerSettings().getUserId());
            preparedStatement.setLong(index++, order.getAddress().getId());
            preparedStatement.setLong(index++, order.getStatus());
            preparedStatement.setFloat(index++, order.getDeliveryCharges());
            preparedStatement.setFloat(index++, order.getCarryBagCharges());
            preparedStatement.setFloat(index++, order.getNotAvailableAmount());
            preparedStatement.setFloat(index++, order.getTotalAmount());
            preparedStatement.setFloat(index++, order.getAmountPayable());
            preparedStatement.setBoolean(index++, order.isHomeDelivery());
            preparedStatement.setBoolean(index++, order.isAsap());
            preparedStatement.setDate(index++, new Date(orderDate.getTime()));
            if(order.isAsap()) {
                //requested delivery time = order time
                preparedStatement.setDate(index++, new Date(orderDate.getTime()));
            } else {
                //requested delivery time with time diff
                preparedStatement.setDate(index++, new Date(CommonUtils.getDate(orderDate, addMinutes, addHours).getTime()));
            }
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs != null && rs.getLong(1) > 0) {
                logger.info("order created!");
                Long generatedOrderId = rs.getLong(1);
                order.setId(generatedOrderId);
            } else {
                logger.severe("some problem in creating order");
                rollbackTransaction = true;
            }

            //03.03 INSERT ORDER ITEMS
            int i = 0;
            String orderItemsQuery = "insert into OrderItems set order_id=?, product_variety_id=?, name=?, brand=?, price=?" +
                    ", quantity=?, order_count=?, available_count=?, image_url=?";
            preparedStatement = dbConnection.prepareStatement(orderItemsQuery);

            for (OrderItem item : orderItems) {
                index = 1;
                preparedStatement.setLong(index++, order.getId());
                preparedStatement.setLong(index++, item.getProductVarietyId());
                preparedStatement.setString(index++, item.getName());
                preparedStatement.setString(index++, item.getBrand());
                preparedStatement.setFloat(index++, item.getPricePerUnit());
                preparedStatement.setString(index++, item.getQuantity());
                preparedStatement.setInt(index++, item.getOrderCount());
                preparedStatement.setInt(index++, item.getAvailableCount());
                preparedStatement.setString(index++, item.getImageUrl());
                preparedStatement.addBatch();
                i++;
                if (i % 1000 == 0 || i == orderItems.size()) {
                    preparedStatement.executeBatch(); // Execute every 1000 items.
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception while creating new order for customer_id = " + order.getBuyerSettings().getUserId() + "and seller_id = " + order.getSellerSettings().getUserId(), e);
            rollbackTransaction = true;
        }

        if (!rollbackTransaction) {
            //SUCCESS - commit the transaction
            try {
                dbConnection.commit();
                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "some exception while committing create new order for customer_id = " + order.getBuyerSettings().getUserId() + "and seller_id = " + order.getSellerSettings().getUserId(), e);
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }
        } else {
            //FAILED - rollback the transaction
            try {
                dbConnection.rollback();
                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "some exception while rolling back create new order for customer_id = " + order.getBuyerSettings().getUserId() + "and seller_id = " + order.getSellerSettings().getUserId(), e);
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }
        }


        return order;
    }

    public Order updateOrder(Order order) {
        if (order == null || order.getId() <= 0) {
            logger.log(Level.SEVERE, "order not update - order id was null");
            return null;
        }
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean rollbackTransaction = false;

        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.size() == 0) {
            logger.log(Level.SEVERE, "order not update - order items size was zero");
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
                preparedStatement.setLong(4, item.getProductVarietyId());
                preparedStatement.addBatch();
                i++;
                if (i % 1000 == 0 || i == orderItems.size()) {
                    preparedStatement.executeBatch(); // Execute every 1000 items.
                }
            }

            //02. UPDATE THE ORDER
            String orderQuery = "update Orders(status_id, total_amount, not_available_amount, amount_payable, actual_delivery_time, delivery_start_time, minutes_to_delivery) set values(?,?,?,?,?,?,?)" +
                    " where id=?";

            preparedStatement = dbConnection.prepareStatement(orderQuery);
            int index = 1;
            preparedStatement.setLong(index++, order.getStatus());
            preparedStatement.setFloat(index++, order.getTotalAmount());
            preparedStatement.setFloat(index++, order.getNotAvailableAmount());
            preparedStatement.setFloat(index++, order.getAmountPayable());
            preparedStatement.setDate(index++, new Date(order.getActualDeliveryTime().getTime()));
            preparedStatement.setDate(index++, new Date(order.getDeliveryStartTime().getTime()));
            preparedStatement.setInt(index++, order.getMinutesToDelivery());
            preparedStatement.setLong(index++, order.getId());
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
            /*String queryOld = "select o.id as order_id,o.customer_id,o.total_amount,o.not_available_amount,o.delivery_charges,o.carry_bag_charges,o.amount_payable," +
                    "o.order_time,o.requested_delivery_time,o.actual_delivery_time,o.delivery_or_pickup,o.status_id as order_status_id" +
                    "a.id as address_id,a.name as address_name,a.address,a.phone_number,a.country_code,a.nickname as address_nickname,a.gps_long,a.gps_lat," +
                    "oi.name as item_name, oi.brand as item_brand, oi.price as item_price, oi.quantity as item_quantity, oi.order_count, oi.available_count," +
                    "pv.id as pv_id,pv.quantity as pv_quantity,pv.price as pv_price,pv.image as pv_image,pv.limited_stock,pv.valid as pv_valid" +
                    " from Orders o join Address a on o.address_id=a.id" +
                    " join OrderItems oi on o.id=oi.order_id" +
                    " join ProductVariety pv on oi.product_variety_id = pv.id" +
                    " where o.id in (" + builder.deleteCharAt( builder.length() -1 ).toString() +
                    ") order by o.id desc";*/

            query = "select o.id as order_id,o.order_number,o.status_id as order_status_id,o.delivery_charges,o.carry_bag_charges,o.not_available_amount,o.total_amount,o.amount_payable," +
                    "o.home_delivery,o.asap," +
                    "o.order_time,o.requested_delivery_time,o.actual_delivery_time,o.delivery_start_time,o.minutes_to_delivery," +
                    "a.id as address_id,a.user_id as buyer_id,a.name as address_name,a.address,a.phone_number,a.country_code,a.nickname as address_nickname,a.gps_long,a.gps_lat," +
                    "oi.product_variety_id,oi.name as item_name, oi.brand as item_brand, oi.price as item_price, oi.quantity as item_quantity, oi.order_count, oi.available_count,oi.image_url as item_image_url" +
                    ",ss.id as seller_settings_id,ss.user_id as seller_id, ss.image_url as ss_image_url, ss.header_image_url as ss_header_image_url,ss.address_id as ss_address_id, ss.open_time,ss.close_time,ss.pickup_from_shop," +
                    "ss.home_delivery,ss.max_delivery_distance,ss.min_order,ss.delivery_charges as ss_delivery_charges,ss.carry_bag_charges as ss_carry_bag_charges,ss.delivery_start_time as ss_delivery_start_time,ss.delivery_end_time as ss_delivery_end_time," +
                    "bs.id as buyer_settings_id,bs.user_id as buyer_settings_buyer_id,bs.name as buyer_settings_name,bs.image_url as bs_image_url,bs.header_image_url as bs_header_image_url," +
                    "sa.id as seller_address_id,sa.name as seller_address_name,sa.address as seller_address,sa.phone_number as seller_phone_number,sa.country_code as seller_country_code,sa.nickname as seller_address_nickname,sa.gps_long as seller_gps_long,sa.gps_lat as seller_gps_lat," +
                    "sst.shop_open" +
                    " from Orders o join Address a on o.address_id=a.id" +
                    " join OrderItems oi on o.id=oi.order_id" +
                    " join SellerSettings ss on ss.user_id = o.seller_id" +
                    " join Address sa on sa.user_id = ss.user_id" +
                    " join BuyerSettings bs on bs.user_id = o.customer_id" +
                    " join SellerStatus sst on sst.seller_id = ss.id" +
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
                        //03.03 EXTRACT ORDER INFO, ADDRESS, SELLER_SETTINGS, BUYER_SETTINGS

                        //add the currently looping order to the orders list
                        if (order != null && orderItems != null) {
                            orders.add(order);
                        }

                        //initialize order items list for new order
                        orderItems = new ArrayList<>();

                        //extract order info
                        String orderNumber = rs.getString("order_number");
                        int orderStatusId = rs.getInt("order_status_id");
                        Float deliveryCharges = rs.getFloat("delivery_charges");
                        Float carryBagCharges = rs.getFloat("carry_bag_charges");
                        Float notAvailableAmount = rs.getFloat("not_available_amount");
                        Float totalAmount = rs.getFloat("total_amount");
                        Float amountPayable = rs.getFloat("amount_payable");
                        boolean homeDelivery = rs.getBoolean("home_delivery");
                        boolean asap = rs.getBoolean("asap");
                        java.util.Date orderTime = getDateFromSqlTimestamp(rs.getTimestamp("order_time"));
                        java.util.Date requestedDeliveryTime = rs.getTimestamp("requested_delivery_time");
                        java.util.Date actualDeliveryTime = rs.getTimestamp("actual_delivery_time");
                        java.util.Date deliveryStartTime = rs.getTimestamp("delivery_start_time");
                        int minutesToDelivery = rs.getInt("minutes_to_delivery");

                        //extract address info
                        Long addressId = rs.getLong("address_id");
                        Long buyerId = rs.getLong("buyer_id");
                        String addressName = rs.getString("address_name");
                        String addressString = rs.getString("address");
                        Long phoneNumber = rs.getLong("phone_number");
                        int countryCode = rs.getInt("country_code");
                        String nickname = rs.getString("address_nickname");
                        Double gpsLong = rs.getDouble("gps_long");
                        Double gpsLat = rs.getDouble("gps_lat");
                        Address address = new Address(addressId, buyerId, addressName, addressString
                                , Integer.parseInt(Constants.ADDRESS_TYPE_CUSTOMER), phoneNumber
                                , countryCode, nickname, gpsLong, gpsLat);

                        //extract seller settings
                        Long sellerSettingsId = rs.getLong("seller_settings_id");
                        Long sellerId = rs.getLong("seller_id");
                        String imageUrl = rs.getString("ss_image_url");
                        String headerImageUrl = rs.getString("ss_header_image_url");
                        Long sellerSettingsAddressId = rs.getLong("ss_address_id");
                        int openTime = rs.getInt("open_time");
                        int closeTime = rs.getInt("close_time");
                        boolean pickupFromShop = rs.getBoolean("pickup_from_shop");
                        boolean sellerSettingsHomeDelivery = rs.getBoolean("home_delivery");
                        Long maxDeliveryDistance = rs.getLong("max_delivery_distance");
                        Float minOrder = rs.getFloat("min_order");
                        Float sellerSettingsDeliveryCharges = rs.getFloat("ss_delivery_charges");
                        Float sellerSettingsCarryBagCharges = rs.getFloat("ss_carry_bag_charges");
                        int sellerSettingsDeliveryStartTime = rs.getInt("ss_delivery_start_time");
                        int sellerSettingsDeliveryEndTime = rs.getInt("ss_delivery_end_time");

                        //extract buyer settings
                        Long buyerSettingsId = rs.getLong("buyer_settings_id");
                        Long buyerSettingsBuyerId = rs.getLong("buyer_settings_buyer_id");
                        String buyerSettingsName = rs.getString("buyer_settings_name");
                        String buyerSettingsImageUrl = rs.getString("bs_image_url");
                        String buyerSettingsHeaderImageUrl = rs.getString("bs_header_image_url");

                        //extract seller address
                        Long sellerAddressId = rs.getLong("seller_address_id");
                        String sellerAddressName = rs.getString("seller_address_name");
                        String sellerAddressString = rs.getString("seller_address");
                        Long sellerAddressPhone = rs.getLong("seller_phone_number");
                        int sellerAddressCountryCode = rs.getInt("seller_country_code");
                        String sellerAddressNickname = rs.getString("seller_address_nickname");
                        Double sellerAddressGpsLong = rs.getDouble("seller_gps_long");
                        Double sellerAddressGpsLat = rs.getDouble("seller_gps_lat");

                        boolean shopOpen = rs.getBoolean("shop_open");

                        BuyerSettings buyerSettings = new BuyerSettings(buyerSettingsId, buyerSettingsBuyerId, buyerSettingsName, buyerSettingsImageUrl, buyerSettingsHeaderImageUrl);

                        Address sellerAddress = new Address(sellerAddressId, sellerId, sellerAddressName, sellerAddressString, Constants.ADDRESS_TYPE_SELLER_INT,
                                sellerAddressPhone, sellerAddressCountryCode, sellerAddressNickname, sellerAddressGpsLong, sellerAddressGpsLat);

                        SellerSettings sellerSettings = new SellerSettings(sellerSettingsId, sellerId, imageUrl, headerImageUrl, sellerAddress, openTime, closeTime, pickupFromShop, sellerSettingsHomeDelivery,
                                maxDeliveryDistance, minOrder, sellerSettingsDeliveryCharges, sellerSettingsCarryBagCharges, sellerSettingsDeliveryStartTime, sellerSettingsDeliveryEndTime, shopOpen);

                        //create new order
                        order = new Order(orderId, orderNumber, sellerSettings, buyerSettings, address, orderStatusId, null, deliveryCharges, carryBagCharges, notAvailableAmount,
                                totalAmount, amountPayable, homeDelivery, asap, orderTime, requestedDeliveryTime, actualDeliveryTime, deliveryStartTime, minutesToDelivery);
                        orders.add(order);
                        ordersHashMap.put(orderId, position);
                        position++;
                    } else {
                        //THIS ORDER ALREADY EXISTS...ONLY EXTRACT THE ORDER ITEM AND PRODUCT VARIETY
                    }



                    //03.04 Extract order item

                    //order item
                    Long itemProductVarietyId = rs.getLong("product_variety_id");
                    String itemName = rs.getString("item_name");
                    String itemBrand = rs.getString("item_brand");
                    Float itemPrice = rs.getFloat("item_price");
                    String itemQuantity = rs.getString("item_quantity");
                    int orderCount = rs.getInt("order_count");
                    int availableCount = rs.getInt("available_count");
                    String imageUrl = rs.getString("item_image_url");
                    OrderItem item = new OrderItem(itemProductVarietyId, itemName, itemBrand, itemQuantity, itemPrice, imageUrl, orderCount, availableCount);
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
