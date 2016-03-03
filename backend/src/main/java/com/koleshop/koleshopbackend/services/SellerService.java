package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.db.models.InventoryProduct;
import com.koleshop.koleshopbackend.db.models.InventoryProductVariety;
import com.koleshop.koleshopbackend.utils.CommonUtils;
import com.koleshop.koleshopbackend.utils.DatabaseConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 17/02/16.
 */
public class SellerService {

    private static final Logger logger = Logger.getLogger(SellerService.class.getName());

    public boolean openCloseShop(Long sellerId, boolean open) {
        Connection dbConnection;
        PreparedStatement preparedStatement = null;
        String query = "update SellerStatus set shop_open=? where seller_id=?";
        dbConnection = DatabaseConnection.getConnection();
        boolean updated = false;
        try {
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setBoolean(1, open);
            preparedStatement.setLong(2, sellerId);
            int update = preparedStatement.executeUpdate();
            if (update > 0) {
                updated = true;
            } else {
                updated = false;
            }
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception in open close shop for seller_id = " + sellerId, e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
        return updated;
    }

    public boolean updateProfilePicture(Long sellerId, String imageUrl, boolean headerImage) {
        Connection dbConnection;
        PreparedStatement preparedStatement = null;
        String query;
        if (headerImage) {
            query = "update SellerSettings set header_image_url=? where user_id=?";
        } else {
            query = "update SellerSettings set image_url=? where user_id=?";
        }
        dbConnection = DatabaseConnection.getConnection();
        boolean updated = false;
        try {
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, imageUrl);
            preparedStatement.setLong(2, sellerId);
            int update = preparedStatement.executeUpdate();
            if (update > 0) {
                updated = true;
            } else {
                updated = false;
            }
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception in updating seller image " + (headerImage ? "header " : "") + "url for seller_id = " + sellerId, e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
        return updated;
    }

    public List<InventoryProduct> searchProducts(Long sellerId, boolean myInventory, String searchQuery, int limit, int offset) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        String[] splitSearchQuery = searchQuery.split(" ");

        //todo optimize these queries and make them smart
        String limitedSearchQuery;

        //========================
        //01. SELECT ALL PRODUCTS
        //========================
        String selectAllProductsQuery;
        if (myInventory) {
            selectAllProductsQuery = "select p.id,p.name,p.category_id,b.name as brand,pv.id as pvar_id,pv.quantity,pv.price as price,pv.image,pv.limited_stock,'1' as selected" +
                    " from Product p join ProductVariety pv" +
                    " on p.id = pv.product_id and pv.valid='1'" +
                    " join Brand b on b.id = p.brand_id" +
                    " where p.valid=1 and p.user_id=? and ( ";

            int loopCount = 0;
            for (String str : splitSearchQuery) {
                selectAllProductsQuery += "(p.name like ? or p.name like ? or b.name like ? or b.name like ?)";
                if (loopCount < splitSearchQuery.length - 1) {
                    selectAllProductsQuery += " and ";
                }
                loopCount++;
            }
            selectAllProductsQuery += " )" +
                    " order by brand asc, p.name asc, price asc";

        } else {
            selectAllProductsQuery = "select p.id,p.name,p.category_id,b.name as brand,pv.id as pvar_id,pv.quantity,pv.price as price,pv.image,pv.limited_stock,pv.valid as selected" +
                    " from Product p join ProductVariety pv" +
                    " on p.id = pv.product_id" +
                    " join Brand b on b.id = p.brand_id" +
                    " where p.valid=1 and p.user_id=? and ( ";

            int loopCount = 0;
            for (String str : splitSearchQuery) {
                selectAllProductsQuery += "(p.name like ? or p.name like ? or b.name like ? or b.name like ?)";
                if (loopCount < splitSearchQuery.length - 1) {
                    selectAllProductsQuery += " and ";
                }
                loopCount++;
            }
            selectAllProductsQuery += ") order by brand asc, p.name asc, price asc";
        }

        //=======================
        //02. LIMIT THE PRODUCTS
        //=======================
        limitedSearchQuery = "select p.id,p.name,p.category_id,b.name as brand,pv.id as pvar_id,pv.quantity,pv.price as price,pv.image,pv.limited_stock,pv.valid as selected " +
                " from Product p join ProductVariety pv on p.id = pv.product_id " +
                (myInventory?"and pv.valid='1' ":"") +
                " join Brand b on b.id = p.brand_id " +
                " where p.user_id=? and p.id in ( " +
                " select * from ( " +
                " select distinct(id) from ( " +
                selectAllProductsQuery +
                " ) ps limit ? offset ?) psTemp) order by brand asc, p.name asc, price asc ;";


        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(limitedSearchQuery);

            int index = 1;
            preparedStatement.setLong(index++, sellerId);
            preparedStatement.setLong(index++, sellerId);
            for (String str : splitSearchQuery) {
                preparedStatement.setString(index++, str + "%");
                preparedStatement.setString(index++, "% " + str + "%");
                preparedStatement.setString(index++, str + "%");
                preparedStatement.setString(index++, "% " + str + "%");
            }

            preparedStatement.setInt(index++, limit);
            preparedStatement.setInt(index++, offset);

            logger.log(Level.INFO, "query=" + preparedStatement.toString());

            ResultSet rs = preparedStatement.executeQuery();
            List<InventoryProduct> result = new ArrayList<>();
            List<InventoryProductVariety> inventoryProductVarieties = new ArrayList<>();
            InventoryProduct currentInventoryProduct = null;

            while (rs.next()) {
                if (currentInventoryProduct != null && rs.getLong(1) == currentInventoryProduct.getId()) {
                    //use the existing currentInventoryProduct
                } else {
                    //save the inventory product from previous iteration
                    if (currentInventoryProduct != null) {
                        //---------------------------------------> place 88
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
                    currentInventoryProduct.setCategoryId(rs.getLong("category_id"));
                    /*this shit is deprecated for now
                    currentInventoryProduct.setDescription(rs.getString("description"));
                    currentInventoryProduct.setAdditionalInfo(rs.getString("info"));
                    currentInventoryProduct.setSpecialDescription(rs.getString("special_desc"));
                    currentInventoryProduct.setPrivateToUser(rs.getBoolean("private"));
                    currentInventoryProduct.setSelectedByUser(rs.getBoolean("p_selected"));*/
                }

                //extract inventory product variety data
                InventoryProductVariety inventoryProductVariety = new InventoryProductVariety();
                inventoryProductVariety.setId(rs.getLong("pvar_id"));
                inventoryProductVariety.setQuantity(rs.getString("quantity"));
                inventoryProductVariety.setPrice(rs.getFloat("price"));
                inventoryProductVariety.setImageUrl(rs.getString("image"));
                inventoryProductVariety.setLimitedStock(rs.getBoolean("limited_stock"));
                inventoryProductVariety.setValid(rs.getBoolean("selected"));
                inventoryProductVarieties.add(inventoryProductVariety);
            }

            //after the last iteration, control will not go to place 88, so add the last product manually
            if (currentInventoryProduct != null) {
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
