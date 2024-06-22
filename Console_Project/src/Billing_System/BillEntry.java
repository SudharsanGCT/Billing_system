package Billing_System;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.*;
import java.sql.*;
import java.time.LocalDate;
import java.lang.*;
public  class BillEntry {
     static Scanner sc=new Scanner(System.in);
     static Timestamp timestamp = new Timestamp(System.currentTimeMillis());

     public static int[] Bill_details(int customerId)
     {
          double amountToPay=0;
          boolean addProduct=true;
          LocalDate billDate = LocalDate.now();
          int bill_id=-1;
          while(addProduct)
          {
               System.out.print("Product ID         : ");
               int product_id=sc.nextInt();
               double availableQuantiy = getAvailableQuantity(product_id);
               System.out.print("quantity           : ");
               int quantity=sc.nextInt();
               if(quantity>availableQuantiy)
               {
                    System.out.println("------Sorry-----.\nThe Available quantity is: "+availableQuantiy);
                    continue;
               }
               System.out.print("coupon code(if available) : ");
               String coupon_code=sc.next();


               String[] productDetails = getProductDetails(product_id,coupon_code);

               if (productDetails != null) {
                    String p_name = productDetails[0];
                    Double price = Double.parseDouble(productDetails[1]);
                    Double discountByCoupon=Double.parseDouble(productDetails[2]);

                    //Generate_Bill(product_id, p_name, quantity, price,discountByCoupon);
                    //System.out.println(coupon);
                    double total = (quantity * price);
                    double coupon=(discountByCoupon * total)/100;
                    amountToPay=amountToPay+(total-coupon);
                    bill_id=addBillDetails(product_id,quantity,amountToPay,coupon);

               } else {
                    System.out.println("Product not found!");
               }
               System.out.print("addProduct(y/n):");
               char add=sc.next().charAt(0);
               addProduct = (add == 'y') || (add == 'Y');

          }
          addToBills(customerId,amountToPay);
          //Generate_Bill(product_id, p_name, quantity, price,discountByCoupon);
          //Generate_Bill(bill_id);
          return new int[]{bill_id,(int)amountToPay};
     }


     public static void addToBills(int customerId, double amountToPay) {
          String addBillSql = "INSERT INTO bills (customer_id, total_amount, bill_date) VALUES (?, ?, ?)";

          try (Connection conn = Main.DB_connection();
               PreparedStatement addBillPstmt = conn.prepareStatement(addBillSql)) {

               // Set the parameters for the SQL statement
               addBillPstmt.setInt(1, customerId);
               addBillPstmt.setDouble(2, amountToPay);
               addBillPstmt.setTimestamp(3, timestamp);

               // Execute the update
              // int rowsAffected = addBillPstmt.executeUpdate();
               addBillPstmt.executeUpdate();
               // If the update was successful, return true
               //return rowsAffected > 0;

          } catch (SQLException e) {
               e.printStackTrace();
          }

          // If there was an error or no rows were affected, return false
          //return false;
     }

     public static int addBillDetails(int productId, int quantity, double amountToPay,double coupon) {
          String getBillIdSql = "SELECT bill_id FROM bills ORDER BY bill_id DESC LIMIT 1";
          String addBillDetailsSql = "INSERT INTO bill_details (bill_id, product_id, quantity, amountPaid,coupon) VALUES (?, ?, ?, ?, ?)";
          int billId = -1;
          try (Connection conn = Main.DB_connection();
               PreparedStatement getBillIdPstmt = conn.prepareStatement(getBillIdSql);
               PreparedStatement addBillDetailsPstmt = conn.prepareStatement(addBillDetailsSql)) {

               // Retrieve the most recent bill ID

               try (ResultSet rs = getBillIdPstmt.executeQuery()) {
                    if (rs.next()) {
                         billId = rs.getInt("bill_id");

                    } else {
                         // No bills found, handle accordingly
                         System.out.println("No bills found in the bills table.");
                    }
               }

               // Set the parameters for the SQL statement
               addBillDetailsPstmt.setInt(1, billId);
               addBillDetailsPstmt.setInt(2, productId);
               addBillDetailsPstmt.setInt(3, quantity);
               addBillDetailsPstmt.setDouble(4, amountToPay);
               addBillDetailsPstmt.setDouble(5, coupon);

               addBillDetailsPstmt.executeUpdate();

          } catch (SQLException e) {
               e.printStackTrace();
          }
     return billId;
     }


     public static Double getAvailableQuantity(int product_id) {
          String quantitySql = "SELECT stock_quantity FROM products WHERE product_id=?";
          try (
                  Connection conn = Main.DB_connection();
                  PreparedStatement quantityPstmt = conn.prepareStatement(quantitySql)
          ) {
               quantityPstmt.setInt(1, product_id);
               ResultSet rs = quantityPstmt.executeQuery();
               if (rs.next()) {
                    return rs.getDouble("stock_quantity");
               }
          } catch (SQLException e) {
               e.printStackTrace();
          }
          return -1.0; // Return -1.0 if product is not found or an error occurs
     }


     public static String[] getProductDetails(int product_id, String coupon_code) {
          String[] details = new String[3];  // Increased size to 3 to include coupon details

          String productSql = "SELECT p_name, price FROM products WHERE product_id = ?";
          String couponSql = "SELECT coupon_code, discount_percentage, expiry_date ,available_coupon FROM coupons WHERE coupon_code = ?";
          String updateCouponSql = "UPDATE coupons SET available_coupon = available_coupon - 1 WHERE coupon_code = ?";
          try (
                  Connection conn = Main.DB_connection();
                  PreparedStatement productPstmt = conn.prepareStatement(productSql);
                  PreparedStatement couponPstmt = conn.prepareStatement(couponSql);
                  PreparedStatement updateCouponStmt = conn.prepareStatement(updateCouponSql)) {
               // Set parameter and execute product query
               productPstmt.setInt(1, product_id);
               try (ResultSet productRs = productPstmt.executeQuery()) {
                    if (productRs.next()) {
                         details[0] = productRs.getString("p_name");
                         details[1] = productRs.getString("price");  // Store price as string
                    }
               }

               // Set parameter and execute coupon query
               couponPstmt.setString(1, coupon_code);
               try (ResultSet couponRs = couponPstmt.executeQuery()) {
                    if (couponRs.next()) {
                         Date expiryDate = couponRs.getDate("expiry_date");
                         int discountPercentage = couponRs.getInt("discount_percentage");
                         int AvailableCoupons=couponRs.getInt("available_coupon");
                         //System.out.println(AvailableCoupons);

                         // Check if coupon is expired
                         if (expiryDate != null && AvailableCoupons>0 && expiryDate.toLocalDate().isAfter(LocalDate.now())) {
                              updateCouponStmt.setString(1, coupon_code);
                              updateCouponStmt.executeUpdate();

                              details[2] = String.valueOf(discountPercentage);
                              //System.out.println(details[2]);// Coupon is valid
                         } else {
                              System.out.println("Coupon Invalid!!");
                              details[2] = "0";  // Coupon is expired
                         }
                    } else {
                         System.out.println("Coupon Invalid!!");
                         details[2] = "0";  // Coupon not present
                    }
               }
          } catch (SQLException e) {
               e.printStackTrace();
          }

          // Ensure all details are fetched
          return (details[0] != null && details[1] != null) ? details : null;
     }



}
