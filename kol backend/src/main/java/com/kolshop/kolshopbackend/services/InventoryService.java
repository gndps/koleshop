package com.kolshop.kolshopbackend.services;

import com.kolshop.kolshopbackend.beans.ParentProductCategory;
import com.kolshop.kolshopbackend.beans.ProductCategory;
import com.kolshop.kolshopbackend.common.Constants;
import com.kolshop.kolshopbackend.db.connection.DatabaseConnection;
import com.kolshop.kolshopbackend.db.models.InventoryCategory;
import com.kolshop.kolshopbackend.db.models.InventoryProduct;
import com.kolshop.kolshopbackend.db.models.InventoryProductVariety;
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

    public List<InventoryCategory> getCategories() {

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

    public List<InventoryCategory> getSubcategories(Long categoryId) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select id,name from ProductCategory where parent_category_id = ? order by sort_order asc;";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, categoryId);

            logger.log(Level.INFO, "query=" + preparedStatement.toString());

            ResultSet rs = preparedStatement.executeQuery();
            List<InventoryCategory> list = new ArrayList<>();

            while (rs.next()) {
                InventoryCategory inventoryCategory = new InventoryCategory();
                inventoryCategory.setId(rs.getLong("id"));
                inventoryCategory.setName(rs.getString("name"));
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

    public List<InventoryProduct> getProductsForCategory(Long categoryId, Long userId) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select i.id,i.name,b.name as brand,i.description,i.additional_info as info,i.special_desc,i.private,p.valid as p_selected\n" +
                ",iv.id as ivar_id,iv.quantity,iv.price,iv.image,iv.content,pvar.id as pvar_selected\n" +
                "from Inventory i \n" +
                "left outer join Brand b on b.id=i.brand_id \n" +
                "left outer join Product p\n" +
                "on i.id=p.inventory_id \n" +
                "and i.category_id=p.category_id \n" +
                "and p.valid='1'\n" +
                "join InventoryVariety iv\n" +
                "on iv.inventory_id = i.id\n" +
                "and iv.valid='1'\n" +
                "left outer join ProductVariety pvar\n" +
                "on pvar.product_id = p.id\n" +
                "and pvar.valid='1'\n" +
                "where i.valid='1'\n" +
                "and iv.valid='1'\n" +
                "and i.category_id=?\n" +
                "and (p.user_id=? or p.user_id is null)\n" +
                "order by brand asc,i.name asc,ivar_id asc;";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, categoryId);
            preparedStatement.setLong(2, userId);

            logger.log(Level.INFO, "query=" + preparedStatement.toString());

            ResultSet rs = preparedStatement.executeQuery();
            List<InventoryProduct> result = new ArrayList<>();
            List<InventoryProductVariety> inventoryProductVarieties = new ArrayList<>();
            InventoryProduct currentInventoryProduct = null;

            while (rs.next()) {
                if(currentInventoryProduct!=null && rs.getLong(1)==currentInventoryProduct.getId()) {
                    //use the existing currentInventoryProduct
                } else {
                    //save the inventory product from previous iteration
                    if(currentInventoryProduct!=null) {
                        //---------------------------------------------------------------------> place 88
                        currentInventoryProduct.setVarieties(inventoryProductVarieties);
                        result.add(currentInventoryProduct);
                    }
                    //clean old data
                    currentInventoryProduct = new InventoryProduct();
                    inventoryProductVarieties = new ArrayList<>();
                    //extract inventory product data
                    currentInventoryProduct.setId(rs.getLong("id"));
                    currentInventoryProduct.setName(rs.getString("name"));
                    currentInventoryProduct.setBrand(rs.getString("brand"));
                    currentInventoryProduct.setDescription(rs.getString("description"));
                    currentInventoryProduct.setAdditionalInfo(rs.getString("info"));
                    currentInventoryProduct.setSpecialDescription(rs.getString("special_desc"));
                    currentInventoryProduct.setPrivateToUser(rs.getBoolean("private"));
                    currentInventoryProduct.setSelectedByUser(rs.getBoolean("p_selected"));
                }

                //extract inventory product variety data
                InventoryProductVariety inventoryProductVariety = new InventoryProductVariety();
                inventoryProductVariety.setId(rs.getLong("ivar_id"));
                inventoryProductVariety.setQuantity(rs.getString("quantity"));
                inventoryProductVariety.setPrice(rs.getFloat("price"));
                inventoryProductVariety.setImageUrl(rs.getString("image"));
                inventoryProductVariety.setVegNonVeg(rs.getString("content"));
                inventoryProductVariety.setSelected(rs.getBoolean("pvar_selected"));
                inventoryProductVarieties.add(inventoryProductVariety);
            }

            //after the last iteration, control will not go to place 88, so add the last product manually
            if(currentInventoryProduct!=null) {
                currentInventoryProduct.setVarieties(inventoryProductVarieties);
                result.add(currentInventoryProduct);
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);

            return result;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
    }

}
