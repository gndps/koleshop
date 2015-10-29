package com.kolshop.kolshopbackend.services;

import com.kolshop.kolshopbackend.beans.ParentProductCategory;
import com.kolshop.kolshopbackend.beans.ProductCategory;
import com.kolshop.kolshopbackend.common.Constants;
import com.kolshop.kolshopbackend.db.connection.DatabaseConnection;
import com.kolshop.kolshopbackend.db.models.InventoryCategory;
import com.kolshop.kolshopbackend.utils.DatabaseConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 16/10/15.
 */
public class InventoryService {

    private static final Logger logger = Logger.getLogger(InventoryService.class.getName());

    public List<InventoryCategory> getCategories() throws Exception {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select id,name,description,image_url from ProductCategory where parent_category_id is null and id not in ("
                + Constants.EXCLUDED_INVENTORY_CATEGORIES_IDS
                + ") order by sort_order asc";

        logger.log(Level.INFO, "query="+query);

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();
            List<InventoryCategory> list = new ArrayList<>();

            while (rs.next()) {
                InventoryCategory inventoryCategory = new InventoryCategory(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
                list.add(inventoryCategory);
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return list;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
    }

}
