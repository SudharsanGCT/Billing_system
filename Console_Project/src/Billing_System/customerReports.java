package Billing_System;

import java.sql.*;
import java.util.Scanner;

public  class customerReports {
    static Scanner sc=new Scanner(System.in);
    static Timestamp paymentTime=null;
    // Method to display customer reports for a specific customer ID
    public static void getCustomerReports() {
        System.out.println("Choose an option:");
        System.out.println("1. Specific Customer ID");
        System.out.println("2. All Customers");
        int option = sc.nextInt();
        sc.nextLine(); // Consume newline left-over

        switch (option) {
            case 1:
                System.out.print("Enter Customer ID: ");
                int customerId = sc.nextInt();
                sc.nextLine(); // Consume newline left-over
                displayCustomerReports(customerId);
                break;
            case 2:
                displayAllCustomersReports();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
    private static void displayCustomerReports(int customerId) {
        String customerSql = "SELECT name, contact FROM customers WHERE customer_id = ?";
        String billsSql = "SELECT bill_id, total_amount FROM bills WHERE customer_id = ?";
        String billDetailsSql = "SELECT p.p_name, bd.quantity, p.price, py.status , py.payment_date " +
                "FROM bill_details bd " +
                "JOIN products p ON bd.product_id = p.product_id " +
                "JOIN payments py ON bd.bill_id = py.bill_id " +
                "WHERE bd.bill_id = ? ";//AND py.status = 1

        try (Connection conn = Main.DB_connection();
             PreparedStatement customerPstmt = conn.prepareStatement(customerSql);
             PreparedStatement billsPstmt = conn.prepareStatement(billsSql);
             PreparedStatement billDetailsPstmt = conn.prepareStatement(billDetailsSql)) {

            // Retrieve customer details
            customerPstmt.setInt(1, customerId);
            try (ResultSet customerRs = customerPstmt.executeQuery()) {
                if (customerRs.next()) {
                    String customerName = customerRs.getString("name");
                    String contact = customerRs.getString("contact");
                    System.out.println("Customer ID: " + customerId);
                    System.out.println("Customer Name: " + customerName);
                    System.out.println("Contact: " + contact);
                } else {
                    System.out.println("Customer not found with ID: " + customerId);
                    return;
                }
            }

            // Retrieve bills for the customer
            billsPstmt.setInt(1, customerId);
            try (ResultSet billsRs = billsPstmt.executeQuery()) {
                while (billsRs.next()) {
                    int billId = billsRs.getInt("bill_id");
                    double totalAmount = billsRs.getDouble("total_amount");
                    System.out.println("--------------------------------------------------------");
                    System.out.println("Bill ID: " + billId);
                    System.out.println("Total Amount: " + totalAmount);

                    // Retrieve bill details for each bill (only if payment status is 1)
                    billDetailsPstmt.setInt(1, billId);
                    try (ResultSet billDetailsRs = billDetailsPstmt.executeQuery()) {
                        while (billDetailsRs.next()) {
                            String itemName = billDetailsRs.getString("p_name");
                            int quantity = billDetailsRs.getInt("quantity");
                            double price = billDetailsRs.getDouble("price");
                            int paymentStatus = billDetailsRs.getInt("status");
                            String paymentStatusStr = (paymentStatus == 1) ? "successful" : "unsuccessful";
                            paymentTime=billDetailsRs.getTimestamp("payment_date");
                            System.out.println("Item: " + itemName + ", Quantity: " + quantity + ", Price: " + price +
                                    ", Payment Status: " + paymentStatusStr);
                            if(paymentTime!=null)
                                System.out.println("Payment date: "+ paymentTime);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to display reports for all customers
    private static void displayAllCustomersReports() {
        String customerSql = "SELECT customer_id, name, contact FROM customers";
        String billsSql = "SELECT bill_id, total_amount FROM bills WHERE customer_id = ?";
        String billDetailsSql = "SELECT p.p_name, bd.quantity, p.price, py.status, py.payment_date " +
                "FROM bill_details bd " +
                "JOIN products p ON bd.product_id = p.product_id " +
                "JOIN payments py ON bd.bill_id = py.bill_id " +
                "WHERE bd.bill_id = ?";

        try (Connection conn = Main.DB_connection();
             PreparedStatement customerPstmt = conn.prepareStatement(customerSql);
             PreparedStatement billsPstmt = conn.prepareStatement(billsSql);
             PreparedStatement billDetailsPstmt = conn.prepareStatement(billDetailsSql)) {

            try (ResultSet customerRs = customerPstmt.executeQuery()) {
                while (customerRs.next()) {
                    int customerId = customerRs.getInt("customer_id");
                    String customerName = customerRs.getString("name");
                    String contact = customerRs.getString("contact");
                    System.out.println("\nCustomer ID: " + customerId);
                    System.out.println("Customer Name: " + customerName);
                    System.out.println("Contact: " + contact);

                    // Retrieve bills for the customer
                    billsPstmt.setInt(1, customerId);
                    try (ResultSet billsRs = billsPstmt.executeQuery()) {
                        while (billsRs.next()) {
                            int billId = billsRs.getInt("bill_id");
                            double totalAmount = billsRs.getDouble("total_amount");
                            System.out.println("\nBill ID: " + billId);
                            System.out.println("Total Amount: " + totalAmount);

                            // Retrieve bill details for each bill (only if payment status is 1)
                            billDetailsPstmt.setInt(1, billId);
                            try (ResultSet billDetailsRs = billDetailsPstmt.executeQuery()) {
                                while (billDetailsRs.next()) {
                                    String itemName = billDetailsRs.getString("p_name");
                                    int quantity = billDetailsRs.getInt("quantity");
                                    double price = billDetailsRs.getDouble("price");
                                    int paymentStatus = billDetailsRs.getInt("status");
                                    String paymentStatusStr = (paymentStatus == 1) ? "successful" : "unsuccessful";
                                    Timestamp paymentTime = billDetailsRs.getTimestamp("payment_date");

                                    System.out.println("Item: " + itemName + ", Quantity: " + quantity + ", Price: " + price +
                                            ", Payment Status: " + paymentStatusStr);
                                    if (paymentTime != null) {
                                        System.out.println("Payment date: " + paymentTime);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
