package com.kolshop.kolshopbackend.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.kolshop.kolshopbackend.db.models.ParentProductCategory;
import com.kolshop.kolshopbackend.db.models.ProductCategory;
import com.kolshop.kolshopbackend.db.models.deprecated.Product;
import com.kolshop.kolshopbackend.db.models.deprecated.ProductInfoPackage;
import com.kolshop.kolshopbackend.db.models.deprecated.ProductVariety;
import com.kolshop.kolshopbackend.db.models.deprecated.ProductVarietyAttribute;
import com.kolshop.kolshopbackend.db.models.deprecated.ProductVarietyAttributeMeasuringUnit;
import com.kolshop.kolshopbackend.db.connection.DatabaseConnection;
import com.kolshop.kolshopbackend.utils.DatabaseConnectionUtils;
import com.kolshop.kolshopbackend.utils.ProductUtil;


public class ProductService {

    @Deprecated
    public List<Product> getProduct(int shopId, int startIndex, int count) {
        Long[] productIdList = getProductIds(shopId, startIndex, count);
        return getProductForIds(productIdList);
    }

    @Deprecated
    public Long[] getProductIds(int shopId, int startIndex, int count) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select id from Product where shop_id = ? limit ?,?";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, shopId);
            preparedStatement.setInt(2, startIndex);
            preparedStatement.setInt(3, count);
            System.out.println(query);

            ResultSet rs = preparedStatement.executeQuery();
            Long[] productIdsList = new Long[count + 1];
            int index = 0;
            while (rs.next()) {
                productIdsList[index++] = rs.getLong(1);
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return productIdsList;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return null;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }
    }

    @Deprecated
    public List<Product> getProductForIds(Long[] productIds) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select p.id, p.name, p.description, b.name, p.user_id, p.product_category_id," +
                "pv.id, pv.name, pv.limited_stock, pv.valid, pv.image_url, pv.date_added, pv.date_modified" +
                "pva.id, pva.name, pva.measuring_unit_id, " +
                "pvav.id, pvav.detail, b.id" +
                "from Product p " +
                "join Brand b on p.brand_id = b.id" +
                "join ProductVariety pv on pv.product_id = p.id " +
                "join ProductVarietyAttributeValue pvav on pvav.product_variety_id = pv.id " +
                "join ProductVarietyAttribute pva on pva.id = pvav.product_variety_attribute_id " +
                "where p.id in ? " +
                "order by p.id desc, pv.id asc";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            String productIdListString = "'" + productIds[0] + "'";
            for (int i = 1; i < productIds.length; i++) {
                if (productIds[i] > 0) {
                    productIdListString += ", '" + productIds[i] + "'";
                }
            }
            preparedStatement.setString(1, productIdListString);

            ResultSet rs = preparedStatement.executeQuery();

            List<ProductInfoPackage> productInfoPackages = new ArrayList<ProductInfoPackage>();

            while (rs.next()) {

                try {
                    ProductInfoPackage productInfoPackage = new ProductInfoPackage();
                    productInfoPackage.setProductId(rs.getLong(1));
                    productInfoPackage.setProductName(rs.getString(2));
                    productInfoPackage.setProductDescription(rs.getString(3));
                    productInfoPackage.setBrand(rs.getString(4));
                    productInfoPackage.setUserId(rs.getLong(5));
                    productInfoPackage.setProductCategoryId(rs.getLong(6));

                    productInfoPackage.setProductVarietyId(rs.getLong(7));
                    productInfoPackage.setProductVarietyName(rs.getString(8));
                    productInfoPackage.setLimitedStock(rs.getInt(9));
                    productInfoPackage.setIsValid(rs.getBoolean(10));
                    productInfoPackage.setImageUrl(rs.getString(11));
                    productInfoPackage.setDateAdded(rs.getDate(12));
                    productInfoPackage.setDateModified(rs.getDate(13));

                    productInfoPackage.setAttributeId(rs.getLong(14));
                    productInfoPackage.setAttributeName(rs.getString(15));
                    productInfoPackage.setMeasuringUnitId(rs.getInt(16));
                    productInfoPackage.setAttributeValueId(rs.getLong(17));
                    productInfoPackage.setAttributeValueDetail(rs.getString(18));
                    productInfoPackage.setBrandId(rs.getLong(19));
                    productInfoPackages.add(productInfoPackage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            List<Product> products = ProductUtil.getProductList(productInfoPackages);
            return products;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return null;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }
    }

    @Deprecated
    public Long addNewProduct(Product product) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean rollbackTransaction = false;

        if(!adjustBrandId(product))
        {
            return 0L;
        }

        String query = "insert into Product (name, description, brand_id, user_id, product_category_id) values(?,?,?,?,?)";

        try {
            dbConnection = DatabaseConnection.getConnection();
            dbConnection.setAutoCommit(false);
            preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, product.getName());
            preparedStatement.setString(2, product.getDescription());
            preparedStatement.setLong(3, product.getBrandId());
            preparedStatement.setLong(4, product.getUserId());
            preparedStatement.setLong(5, product.getProductCategoryId());

            // execute insert product SQL statement
            preparedStatement.executeUpdate();
            ResultSet keyResultSet = preparedStatement.getGeneratedKeys();
            Long newProductId = 0L;
            if (keyResultSet.next()) {
                newProductId = keyResultSet.getLong(1);
                product.setId(newProductId);
            }

            if (newProductId > 0) {
                List<ProductVariety> productVarieties = product.getProductVarieties();
                outerloop:
                for (ProductVariety productVariety : productVarieties) {
                    //add product variety
                    query = "insert into ProductVariety (product_id, name, limited_stock, valid, image_url)" +
                            "values (?,?,?, '1', ?)";
                    preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    preparedStatement.setLong(1, newProductId);
                    preparedStatement.setString(2, productVariety.getName());
                    preparedStatement.setInt(3, productVariety.getLimitedStock());
                    preparedStatement.setString(4, productVariety.getImageUrl());
                    preparedStatement.executeUpdate();
                    ResultSet rs = preparedStatement.getGeneratedKeys();
                    Long generatedProductVarietyId = 0L;
                    if (rs.next()) {
                        generatedProductVarietyId = rs.getLong(1);
                    }
                    if (generatedProductVarietyId > 0) {
                        List<ProductVarietyAttribute> productVarietyAttributes = productVariety.getProductVarietyAttributes();
                        for (ProductVarietyAttribute pva : productVarietyAttributes) {
                            query = "insert ignore into ProductVarietyAttribute(name, measuring_unit_id) values (?,?);";
                            preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                            preparedStatement.setString(1, pva.getName());
                            preparedStatement.setInt(2, pva.getMeasuringUnitId());
                            preparedStatement.setString(4, productVariety.getImageUrl());
                            int i = preparedStatement.executeUpdate();
                            Long productVarietyAttributeId = 0L;
                            if (i > 0) {
                                //new ProductVarietyAttribute generated
                                ResultSet rs2 = preparedStatement.getGeneratedKeys();
                                Long generatedProductVarietyAttributeId = 0L;
                                if (rs2.next()) {
                                    generatedProductVarietyAttributeId = rs2.getLong(1);
                                }
                                if (generatedProductVarietyAttributeId > 0) {
                                    productVarietyAttributeId = generatedProductVarietyAttributeId;
                                } else {
                                    rollbackTransaction = true;
                                    break outerloop;
                                }

                            } else {
                                //ProductVarietyAttribute with this name already exists
                                //select productVarietyAttributeId from DB
                                query = "select id from ProductVarietyAttribute where name = ? and measuring_unit_id = ?";
                                preparedStatement = dbConnection.prepareStatement(query);
                                preparedStatement.setString(1, pva.getName());
                                preparedStatement.setInt(2, pva.getMeasuringUnitId());

                                ResultSet rs4 = preparedStatement.executeQuery();
                                if (rs4.first()) {
                                    productVarietyAttributeId = rs4.getLong("id");
                                }
                            }

                            if (productVarietyAttributeId > 0) {

                                query = "insert into ProductVarietyAttributeValue (product_variety_id, product_variety_attribute_id, detail)" +
                                        "values (?,?,?)";
                                preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                                preparedStatement.setLong(1, productVariety.getId());
                                preparedStatement.setLong(2, productVarietyAttributeId);
                                preparedStatement.setString(3, pva.getValue());
                                preparedStatement.executeUpdate();
                                ResultSet rs3 = preparedStatement.getGeneratedKeys();
                                Long generatedProductVarietyAttributeValueId = 0L;
                                if (rs3.next()) {
                                    generatedProductVarietyAttributeValueId = rs3.getLong(1);
                                }
                                if (generatedProductVarietyAttributeValueId <= 0) {
                                    //some problem in generating pvav
                                    rollbackTransaction = true;
                                    break outerloop;
                                }
                            } else {
                                rollbackTransaction = true;
                                break outerloop;
                            }

                        }
                    } else {
                        rollbackTransaction = true;
                        break;
                    }
                }

            } else {
                //product id not generated, some problem occured;
                rollbackTransaction = true;
            }

            if (rollbackTransaction) {
                dbConnection.rollback();
                System.out.print("\n========\nproduct updating failed\n========\n" + product + "\n========\n");
                product.setId(0L);
            } else {
                dbConnection.commit();
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return product.getId();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            System.out.print("\n========\nproduct updating failed\n========\n" + product + "\n========\n");
            return 0L;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }
    }

    @Deprecated
    public Long updateProduct(Product product) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean rollbackTransaction = false;

        if(!adjustBrandId(product))
        {
            return 0L;
        }
        if (product.getBrandId() == 0 && !product.getBrand().trim().isEmpty()) {
            Long newBrandId = insertBrandIfNotAlready(product.getBrand());
            product.setBrandId(newBrandId);
        }

        String query = "update Product set name=?, description=?, brand_id=?, product_category_id=? where id=?";

        try {
            dbConnection = DatabaseConnection.getConnection();
            dbConnection.setAutoCommit(false);
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, product.getName());
            preparedStatement.setString(2, product.getDescription());
            preparedStatement.setLong(3, product.getBrandId());
            preparedStatement.setLong(4, product.getProductCategoryId());
            preparedStatement.setLong(5, product.getId());

            int updated = preparedStatement.executeUpdate();
            if(updated<=0)
            {
                rollbackTransaction = true;
            }
            else {
                List<ProductVariety> productVarieties = product.getProductVarieties();
                outerloop:
                for (ProductVariety productVariety : productVarieties) {

                    Long productVarietyId = 0L;

                    if(productVariety.getId()>0)
                    {
                        productVarietyId = productVariety.getId();
                        query = "update ProductVariety set name=?, limited_stock=?, valid=?, image_url=? where id=?";
                        preparedStatement = dbConnection.prepareStatement(query);
                        preparedStatement.setString(1, productVariety.getName());
                        preparedStatement.setInt(2, productVariety.getLimitedStock());
                        preparedStatement.setBoolean(3, productVariety.isValid());
                        preparedStatement.setString(4, productVariety.getImageUrl());
                        preparedStatement.setLong(5, productVarietyId);
                        int update = preparedStatement.executeUpdate();
                        if(update<=0)
                        {
                            rollbackTransaction = true;
                            break outerloop;
                        }
                    }

                    else {
                        //insert product variety
                        query = "insert into ProductVariety (product_id, name, limited_stock, valid, image_url)" +
                                "values (?,?,?, '1', ?)";
                        preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setLong(1, product.getId());
                        preparedStatement.setString(2, productVariety.getName());
                        preparedStatement.setInt(3, productVariety.getLimitedStock());
                        preparedStatement.setString(4, productVariety.getImageUrl());
                        preparedStatement.executeUpdate();
                        ResultSet rs = preparedStatement.getGeneratedKeys();

                        if (rs.next()) {
                            productVarietyId = rs.getLong(1);
                        }
                    }
                    if (productVarietyId > 0) {
                        List<ProductVarietyAttribute> productVarietyAttributes = productVariety.getProductVarietyAttributes();
                        for (ProductVarietyAttribute pva : productVarietyAttributes) {
                            query = "insert ignore into ProductVarietyAttribute(name, measuring_unit_id) values (?,?);";
                            preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                            preparedStatement.setString(1, pva.getName());
                            preparedStatement.setInt(2, pva.getMeasuringUnitId());
                            preparedStatement.setString(4, productVariety.getImageUrl());
                            int i = preparedStatement.executeUpdate();
                            Long productVarietyAttributeId = 0L;
                            if (i > 0) {
                                //new ProductVarietyAttribute generated
                                ResultSet rs2 = preparedStatement.getGeneratedKeys();
                                Long generatedProductVarietyAttributeId = 0L;
                                if (rs2.next()) {
                                    generatedProductVarietyAttributeId = rs2.getLong(1);
                                }
                                if (generatedProductVarietyAttributeId > 0) {
                                    productVarietyAttributeId = generatedProductVarietyAttributeId;
                                } else {
                                    rollbackTransaction = true;
                                    break outerloop;
                                }

                            } else {
                                //ProductVarietyAttribute with this name already exists
                                //select productVarietyAttributeId from DB
                                //productVarietyAttributeId = selectedId
                                query = "select id from ProductVarietyAttribute where name = ? and measuring_unit_id = ?";
                                preparedStatement = dbConnection.prepareStatement(query);
                                preparedStatement.setString(1, pva.getName());
                                preparedStatement.setInt(2, pva.getMeasuringUnitId());

                                ResultSet rs4 = preparedStatement.executeQuery();
                                if (rs4.first()) {
                                    productVarietyAttributeId = rs4.getLong("id");
                                }
                            }

                            if (productVarietyAttributeId > 0) {
                                boolean pvaAlreadyExists = false;
                                if(pva.getAttributeValueId()>0)
                                {
                                    //already exists need update
                                    pvaAlreadyExists = true;
                                    query = "update ProductVarietyAttributeValue set product_variety_id=?, product_variety_attribute_id=?, detail=? where id=?";
                                }
                                else {
                                    query = "insert into ProductVarietyAttributeValue (product_variety_id, product_variety_attribute_id, detail)" +
                                            "values (?,?,?)";
                                }
                                preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                                preparedStatement.setLong(1, productVariety.getId());
                                preparedStatement.setLong(2, productVarietyAttributeId);
                                preparedStatement.setString(3, pva.getValue());
                                preparedStatement.setLong(4, pva.getAttributeValueId());
                                int updatedInserted = preparedStatement.executeUpdate();
                                if(updatedInserted<=0)
                                {
                                    rollbackTransaction = true;
                                    break outerloop;
                                }
                                else if(!pvaAlreadyExists) {
                                    ResultSet rs3 = preparedStatement.getGeneratedKeys();
                                    Long generatedProductVarietyAttributeValueId = 0L;
                                    if (rs3.next()) {
                                        generatedProductVarietyAttributeValueId = rs3.getLong(1);
                                    }
                                    if (generatedProductVarietyAttributeValueId <= 0) {
                                        //some problem in generating pvav
                                        rollbackTransaction = true;
                                        break outerloop;
                                    }
                                }
                            } else {
                                rollbackTransaction = true;
                                break outerloop;
                            }

                        }
                    } else {
                        rollbackTransaction = true;
                        break;
                    }
                }

            }

            if (rollbackTransaction) {
                dbConnection.rollback();
                System.out.print("\n========\nproduct updating failed\n========\n" + product + "\n========\n");
                product.setId(0L);
            } else {
                dbConnection.commit();
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return product.getId();

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            System.out.print("\n========\nproduct updating failed\n========\n" + product + "\n========\n");
            return 0L;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }

    }

    @Deprecated
    public boolean saveProduct(Product product) {
        if (product.getId() == 0) {
            addNewProduct(product);
        } else {
            updateProduct(product);
        }
        return false;
    }

    private boolean adjustBrandId(Product product) {
        if (product.getBrandId() == 0) {
            Long newBrandId = insertBrandIfNotAlready(product.getBrand());
            product.setBrandId(newBrandId);
            return newBrandId > 0;
        } else
            return true;
    }

    private Long insertBrandIfNotAlready(String brand) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select id from Brand where name = ? ";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, brand);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                Long brandId = rs.getLong(1);
                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
                return brandId;
            } else {

                query = "insert ignore into Brand (name) values(?)";
                preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, brand);

                if (preparedStatement.executeUpdate() > 0) {
                    //brand inserted
                    ResultSet keyResultSet = preparedStatement.getGeneratedKeys();
                    Long newBrandId = 0L;
                    if (keyResultSet.next()) {
                        newBrandId = keyResultSet.getLong(1);
                    }
                    DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
                    return newBrandId;
                } else {
                    return 0L;
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return 0L;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }
    }

    public List<ProductCategory> getAllProductCategories() {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select id,name,image_url,parent_category_id from ProductCategory";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();
            List<ProductCategory> productCategories = new ArrayList<>();
            while (rs.next()) {
                ProductCategory pc = new ProductCategory(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                productCategories.add(pc);
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return productCategories;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return null;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }

    }

    public List<ParentProductCategory> getProductCategories() {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        List<ParentProductCategory> parentProductCategoryList = new ArrayList<>();

        String query = "select id,name,image_url,parent_category_id from ProductCategory";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();
            List<ProductCategory> productCategories = new ArrayList<>();
            int index = 0;
            while (rs.next()) {
                ProductCategory pc = new ProductCategory(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                if(rs.getInt(4) == 0)
                {
                    parentProductCategoryList.add(new ParentProductCategory(pc));
                }
                else {
                    productCategories.add(pc);
                }
            }

            for(ProductCategory pc : productCategories) {
                for (ParentProductCategory ppc : parentProductCategoryList) {
                    if (pc.getParentProductCategoryId() == ppc.getParentProductCategory().getId()) {
                        ppc.getChildrenProductCategories().add(pc);
                    }
                }
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return parentProductCategoryList;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return null;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }
    }

    @Deprecated
    public List<ProductVarietyAttributeMeasuringUnit> getMeasuringUnits()
    {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        List<ProductVarietyAttributeMeasuringUnit> measuringUnits = new ArrayList<>();

        String query = "select id,unit_type,unit,is_base_unit,conversion_rate,unit_full_name from ProductVarietyAttributeMeasuringUnit";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                ProductVarietyAttributeMeasuringUnit mu = new ProductVarietyAttributeMeasuringUnit(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getBoolean(4), rs.getFloat(5), rs.getString(6));
                measuringUnits.add(mu);
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return measuringUnits;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return null;

        } finally {

            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);

        }
    }
}
