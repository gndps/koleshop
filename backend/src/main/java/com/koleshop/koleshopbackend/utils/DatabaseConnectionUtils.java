package com.koleshop.koleshopbackend.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnectionUtils {

	public static void closeStatementAndConnection(PreparedStatement stmt,Connection conn) throws SQLException{
		stmt.close();                                                             
        stmt = null;                                                              
        conn.close();                                                             
        conn = null; 
	}
	
	public static void finallyCloseStatementAndConnection(PreparedStatement stmt, Connection conn){
		if (stmt != null) {                                            
            try {                                                         
                stmt.close();                                                
            } catch (SQLException sqlex) {                                
                // ignore -- as we can't do anything about it here           
            }                                                             
 
            stmt = null;                                            
        }                                                        
 
        if (conn != null) {                                      
            try {                                                   
                conn.close();                                          
            } catch (SQLException sqlex) {                          
                // ignore -- as we can't do anything about it here     
            }                                                       
 
            conn = null;                                            
        }                    
	}
	
}
