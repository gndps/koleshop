package com.koleshop.koleshopbackend.servlets;

import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.common.OrderStatus;
import com.koleshop.koleshopbackend.models.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.utils.DatabaseConnectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
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
    private static final String PROCESS_PENDING_REQUESTS = "/processpendingrequests";
    private static final String UPDATE_ESSENTIAL_INFO = "/updateessentialinfo";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.log(Level.INFO, "request received in KoleshopServlet " + new Date());
        String servletPath = req.getServletPath();
        logger.log(Level.INFO, "servlet path = " + servletPath);
        switch (servletPath) {
            case PROCESS_PENDING_REQUESTS:
                processPendingRequests();
                break;
            case UPDATE_ESSENTIAL_INFO:
                updateEssentialInfo();
                break;
        }
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.log(Level.INFO, "new post request at time " + new Date());
        super.doPost(req, resp);
    }

    private void processPendingRequests() {

        logger.log(Level.INFO, "processing pending requests " + new Date());

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

    private void updateEssentialInfo() {

        logger.log(Level.INFO, "updating daily essential information" + new Date());

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;


        String updateEssentialInfo = "update EssentialInfo set date_today = ?";


        try {
            dbConnection = DatabaseConnection.getConnection();

            //01. update essential info
            preparedStatement = dbConnection.prepareStatement(updateEssentialInfo);
            preparedStatement.setTimestamp(1, new Timestamp(new Date().getTime()));
            int updatedEssentialInfo = preparedStatement.executeUpdate();
            logger.log(Level.INFO, updatedEssentialInfo + " essential info updated");

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "problem in updating essential info date_today", e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }

    }

}
