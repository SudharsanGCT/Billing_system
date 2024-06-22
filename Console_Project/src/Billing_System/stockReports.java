package Billing_System;
import java.util.*;
import java.sql.*;
public class stockReports {
    public static void getStockReports()
    {
        String sql = "SELECT product_id, p_name, category, stock_quantity AS remaining_quantity, price FROM products";

        try (Connection conn = Main.DB_connection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("Stock Analysis Report:");
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.printf("%-10s %-20s %-20s %-20s %-10s%n", "Product ID", "Product Name", "Category", "Remaining Quantity", "Price");
            System.out.println("-------------------------------------------------------------------------------------");


            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("p_name");
                String category = rs.getString("category");
                int remainingQuantity = rs.getInt("remaining_quantity");
                double price = rs.getDouble("price");
                System.out.printf("%-10d %-20s %-20s %-20d %-10.2f%n",productId,productName,category,remainingQuantity,price);
            }
            System.out.println("-------------------------------------------------------------------------------------");


        } catch (SQLException e) {
        e.printStackTrace();
        }
    }
}
