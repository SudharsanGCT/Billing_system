package Billing_System;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.*;
import java.util.*;

public class Payment {
    static Scanner sc = new Scanner(System.in);
    static Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    public static boolean proceedPayment(int[] paymentDetails) {
        int billId = paymentDetails[0];
        double grandTotal = (double) paymentDetails[1];
        System.out.println("-------------------------");
        System.out.println("1. Cash\n2. UPI\n3. Card\n4. Cancel");
        System.out.print("Choose the payment mode:");
        int mode = sc.nextInt();

        while (true) {
            switch (mode) {
                case 1:
                    makePayment(billId, "cash", grandTotal);
                    return true;
                case 2:
                    makePayment(billId, "UPI", grandTotal);
                    return true;
                case 3:
                    makePayment(billId, "card", grandTotal);
                    return true;
                case 4:
                    insertPendingPayment(billId, grandTotal);
                    return false;
                default:
                    System.out.println("Invalid choice");
                    System.out.println("Choose the payment mode:");
                    System.out.println("1. Cash\n2. UPI\n3. Card\n4. Cancel");
                    mode = sc.nextInt();
            }
        }

    }

    public static void insertPendingPayment(int billId, double grandTotal) {
        String insertPaymentSql = "INSERT INTO payments (bill_id, payment_mode, grandTotal, payment_date, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Main.DB_connection();
             PreparedStatement insertPaymentPstmt = conn.prepareStatement(insertPaymentSql)) {

            // Insert payment with status 0 (pending)


            insertPaymentPstmt.setInt(1, billId);
            insertPaymentPstmt.setString(2, "N/A");
            insertPaymentPstmt.setDouble(3, grandTotal);
            insertPaymentPstmt.setTimestamp(4, timestamp);
            insertPaymentPstmt.setInt(5, 0); // Initial status is 0 (pending)
            insertPaymentPstmt.executeUpdate();

            System.out.println("Payment is canceled");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to insert pending payment");
        }
    }

    public static void makePayment(int billId, String mode, double grandTotal) {
        String insertPaymentSql = "INSERT INTO payments (bill_id, payment_mode, grandTotal, payment_date, status) VALUES (?, ?, ?, ?, ?)";
        String updateStatusSql = "UPDATE payments SET status = ? WHERE bill_id = ?";
        String reduceQuantitySql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        try (Connection conn = Main.DB_connection();
             PreparedStatement insertPaymentPstmt = conn.prepareStatement(insertPaymentSql);
             PreparedStatement updateStatusPstmt = conn.prepareStatement(updateStatusSql);
             PreparedStatement reduceQuantityPstmt = conn.prepareStatement(reduceQuantitySql)) {

            conn.setAutoCommit(false); // Begin transaction

            // Insert payment with status 0
            insertPaymentPstmt.setInt(1, billId);
            insertPaymentPstmt.setString(2, mode);
            insertPaymentPstmt.setDouble(3, grandTotal);
            insertPaymentPstmt.setTimestamp(4, timestamp);
            insertPaymentPstmt.setInt(5, 0); // Initial status is 0 (pending)
            int rowsAffected = insertPaymentPstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Simulate payment processing logic
                // Update payment status to 1 (successful)
                updateStatusPstmt.setInt(1, 1);
                updateStatusPstmt.setInt(2, billId);
                updateStatusPstmt.executeUpdate();

                List<String[]> billItems = getProductsForBill(billId);
                for (String[] itemDetails : billItems) {
                    int productId = Integer.parseInt(itemDetails[0]);
                    int quantitySold = Integer.parseInt(itemDetails[2]);

                    reduceQuantityPstmt.setInt(1, quantitySold);
                    reduceQuantityPstmt.setInt(2, productId);
                    reduceQuantityPstmt.executeUpdate();
                }

                conn.commit(); // Commit transaction
                System.out.println("Payment successful");
            } else {
                System.out.println("Payment insertion failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Payment failed");
        }
    }
    public static List<String[]> getProductsForBill(int billId) {
        List<String[]> billItems = new ArrayList<>();
        String sql = "SELECT bd.product_id, p.p_name, bd.quantity, p.price, bd.coupon " +
                "FROM bill_details bd " +
                "JOIN products p ON bd.product_id = p.product_id " +
                "WHERE bd.bill_id = ?";
        try (Connection conn = Main.DB_connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, billId);
            ResultSet billDetailsRs = pstmt.executeQuery();
            while (billDetailsRs.next()) {
                int productId = billDetailsRs.getInt("product_id");
                String productName = billDetailsRs.getString("p_name");
                int quantity = billDetailsRs.getInt("quantity");
                double price = billDetailsRs.getDouble("price");
                double coupon = billDetailsRs.getDouble("coupon");

                String[] itemDetails = new String[]{
                        String.valueOf(productId),
                        productName,
                        String.valueOf(quantity),
                        String.valueOf(price),
                        String.valueOf(coupon)
                };

                billItems.add(itemDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return billItems;
    }

    public static void Generate_Bill(int bill_id) {
        String getBillDetailsSql = "SELECT bd.product_id, p.p_name, bd.quantity, p.price, bd.coupon " +
                "FROM bill_details bd " +
                "JOIN products p ON bd.product_id = p.product_id " +
                "WHERE bd.bill_id = ?";

        String getBillInfoSql = "SELECT b.customer_id, b.total_amount, b.bill_date " +
                "FROM bills b " +
                "WHERE b.bill_id = ?";

        try (Connection conn = Main.DB_connection();
             PreparedStatement getBillDetailsPstmt = conn.prepareStatement(getBillDetailsSql);
             PreparedStatement getBillInfoPstmt = conn.prepareStatement(getBillInfoSql)) {

            // Set the bill_id parameter
            getBillDetailsPstmt.setInt(1, bill_id);
            getBillInfoPstmt.setInt(1, bill_id);

            // Execute the query for bill details
            ResultSet billDetailsRs = getBillDetailsPstmt.executeQuery();

//            List<String[]> billItems = new ArrayList<>();
//            while (billDetailsRs.next()) {
//                int productId = billDetailsRs.getInt("product_id");
//                String productName = billDetailsRs.getString("p_name");
//                int quantity = billDetailsRs.getInt("quantity");
//                double price = billDetailsRs.getDouble("price");
//                double Coupon = billDetailsRs.getDouble("coupon");
//
//                String[] itemDetails = new String[]{
//                        String.valueOf(productId),
//                        productName,
//                        String.valueOf(quantity),
//                        String.valueOf(price),
//                        String.valueOf(Coupon)
//                };
//
//                billItems.add(itemDetails);
//            }

            // Execute the query for bill info
            ResultSet billInfoRs = getBillInfoPstmt.executeQuery();
            if (billInfoRs.next()) {
                int serialNumber=1;
                int customerId = billInfoRs.getInt("customer_id");
                double totalAmountPaid = billInfoRs.getDouble("total_amount");
                Date billDate = billInfoRs.getDate("bill_date");

                // Print the bill receipt
                System.out.println("------------------- BILL RECEIPT ----------------------------");
                System.out.println("Customer ID: " + customerId);
                System.out.println("Bill Date: " + billDate);
                System.out.println("-------------------------------------------------------------------");
                System.out.printf("%-5s | %-15s | %-8s | %-10s | %-15s | %-10s%n", "Count", "Product Name", "Quantity", "Price", "Coupon Discount", "Total");
                System.out.println("-------------------------------------------------------------------");

                double grandTotal = 0;
                List<String[]> billItems=getProductsForBill(bill_id);
                for (String[] item : billItems) {
                    int productId = Integer.parseInt(item[0]);
                    String productName = item[1];
                    int quantity = Integer.parseInt(item[2]);
                    double price = Double.parseDouble(item[3]);
                    double discountByCoupon = Double.parseDouble(item[4]);

                    double coupon = (discountByCoupon * price) / 100;
                    double total = quantity * price;
                    double amountToPay = total - coupon;
                    grandTotal += amountToPay;

                    System.out.printf("%-5d | %-15s | %-8d x  %-10.2f | %-15.2f | %-10.2f%n", serialNumber++, productName, quantity, price, coupon, amountToPay);
                }

                System.out.println("-------------------------------------------------------------------");
                System.out.printf("%-40s %-10.2f%n", "TOTAL:", grandTotal);
                System.out.println("-------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
