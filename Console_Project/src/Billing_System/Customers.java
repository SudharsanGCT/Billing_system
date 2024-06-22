package Billing_System;
import java.util.*;
import java.sql.*;

public class Customers {
    static Scanner sc = new Scanner(System.in);

    public static int customer_details() {
//        System.out.print("Customer Name      : ");
//        String customerName = sc.next();
        String contactNumber = "";
        String name="";
        // Validate contact number
        boolean isValidNumber = false;
        while (!isValidNumber) {
            System.out.print("Contact Number     : ");
            contactNumber = sc.next();
            if (contactNumber.matches("\\d{10}")) {  // Check if it's exactly 10 digits
                isValidNumber = true;
            } else {
                System.out.println("Please enter a valid 10-digit contact number.");
            }
        }
        System.out.print("Customer Name(optional):");
        name=sc.next();

        int customerId = getOrAddCustomer(name,contactNumber);
        return customerId;

    }

    public static int getOrAddCustomer(String name, String contactNumber) {
        String checkCustomerSql = "SELECT customer_id FROM customers WHERE contact = ?";
        String addCustomerSql = "INSERT INTO customers (name, contact) VALUES (?, ?)";
        String getCustomerIdSql = "SELECT customer_id FROM customers WHERE contact = ?";

        try (Connection conn = Main.DB_connection();
             PreparedStatement checkCustomerPstmt = conn.prepareStatement(checkCustomerSql);
             PreparedStatement addCustomerPstmt = conn.prepareStatement(addCustomerSql, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement getCustomerIdPstmt = conn.prepareStatement(getCustomerIdSql)) {

            // Check if customer exists
            checkCustomerPstmt.setString(1, contactNumber);
            try (ResultSet rs = checkCustomerPstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id"); // Customer exists, return the customer_id
                }
            }

            // If customer does not exist, prompt for customer name and add new customer
//            System.out.print("Customer Name: ");
//            name = sc.nextLine(); // Read the customer name from user input

            addCustomerPstmt.setString(1, name);
            addCustomerPstmt.setString(2, contactNumber);
            int rowsAffected = addCustomerPstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve new customer ID
                try (ResultSet generatedKeys = addCustomerPstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the customer_id of the newly added customer
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Return -1 if there was an error or customer was not found
    }
//    public static int getOrAddCustomer(String name, String contactNumber) {
//        String checkCustomerSql = "SELECT customer_id FROM customers WHERE contact = ?";
//        String addCustomerSql = "INSERT INTO customers (name, contact) VALUES (?, ?)";
//        String getCustomerIdSql = "SELECT customer_id FROM customers WHERE contact = ?";
//
//        try (Connection conn = Main.DB_connection();
//             PreparedStatement checkCustomerPstmt = conn.prepareStatement(checkCustomerSql);
//             PreparedStatement addCustomerPstmt = conn.prepareStatement(addCustomerSql);
//             PreparedStatement getCustomerIdPstmt = conn.prepareStatement(getCustomerIdSql)) {
//
//            // Check if customer exists
//            checkCustomerPstmt.setString(1, contactNumber);
//            try (ResultSet rs = checkCustomerPstmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("customer_id");
//                }
//            }
//
//            // Add new customer
//            addCustomerPstmt.setString(1, name);
//            addCustomerPstmt.setString(2, contactNumber);
//            addCustomerPstmt.executeUpdate();
//
//            // Retrieve new customer ID
//            getCustomerIdPstmt.setString(1, contactNumber);
//            try (ResultSet rs = getCustomerIdPstmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("customer_id");
//                }
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
}
