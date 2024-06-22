package Billing_System;
import java.util.*;
import java.sql.*;
public class salesReports {
    public static void getSalesReports()
    {
        String sql = "SELECT p.p_name, " +
                "SUM(bd.quantity) AS total_quantity_sold, " +
                "COUNT(bd.product_id) AS number_of_entries, " +
                "SUM(bd.quantity * p.price) AS total_amount " +
                "FROM bill_details bd " +
                "JOIN products p ON bd.product_id = p.product_id " +
                "GROUP BY p.p_name";

        try (Connection conn = Main.DB_connection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("Sales Report:");
            System.out.println("------------------------------------------------------------------");
            System.out.printf("%-20s %-20s %-20s %-20s%n", "Product_Name", "Total_Quantity_Sold", "Number_of_Entries", "Total_Amount");
            System.out.println("------------------------------------------------------------------");
            double wholeSalesTotal=0;


            while (rs.next()) {

                String productName = rs.getString("p_name");
                int totalQuantitySold = rs.getInt("total_quantity_sold");
                int numberOfEntries = rs.getInt("number_of_entries");
                double totalAmount = rs.getDouble("total_amount");

                System.out.printf("%-20s %-20d %-23d %-20.2f%n",productName,totalQuantitySold,numberOfEntries,totalAmount);
                wholeSalesTotal+=totalAmount;
            }
            System.out.println("------------------------------------------------------------------");
            System.out.print("Total Sales Amount:");
            System.out.printf("%-20.2f%n",wholeSalesTotal);
            System.out.println("------------------------------------------------------------------");


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
