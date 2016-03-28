package com.koleshop.koleshopbackend.servlets;

import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.common.OrderStatus;
import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.utils.DatabaseConnectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Gundeep on 27/03/16.
 */
public class KoleshopServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(KoleshopServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.log(Level.INFO, "processing pending requests " + new Date());
        processPendingRequests();
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.log(Level.INFO, "new post request at time " + new Date());
        super.doPost(req, resp);
    }

    private void processPendingRequests() {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;


        String updatePendingPickupOrdersQuery = "update Orders set status_id = " +
                OrderStatus.DELIVERED +
                " where status_id in (" +
                OrderStatus.READY_FOR_PICKUP +
                ") and date_add(date_modified, INTERVAL " +
                Constants.MARK_AS_PICKED_UP_DELAY +
                " HOUR) <= date(current_timestamp());";

        String updatePendingDeliveryOrdersQuery = "update Orders set status_id = " +
                OrderStatus.DELIVERED +
                " where status_id in (" +
                OrderStatus.OUT_FOR_DELIVERY +
                ") and actual_delivery_time <= date_add(current_timestamp(), INTERVAL -" +
                Constants.MARK_AS_DELIVERED_OFFSET_TIME +
                " MINUTE);";


        try {
            dbConnection = DatabaseConnection.getConnection();

            //01. update pending pickup orders
            preparedStatement = dbConnection.prepareStatement(updatePendingPickupOrdersQuery);
            int pickupUpdated = preparedStatement.executeUpdate();
            logger.log(Level.INFO, pickupUpdated + " ready for pickup orders set as picked up");

            //02. update pending delivery orders
            preparedStatement = dbConnection.prepareStatement(updatePendingDeliveryOrdersQuery);
            int deliveryUpdated = preparedStatement.executeUpdate();
            logger.log(Level.INFO, deliveryUpdated + " out for delivery orders set as delivered");

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "problem in updating orders status to delivered", e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }


    }

}
