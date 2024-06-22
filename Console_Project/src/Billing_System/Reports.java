package Billing_System;
import java.util.*;
import java.sql.*;
public class Reports {
    static Scanner sc=new Scanner(System.in);
        public static void generateReports()
        {
            System.out.println("-------------------------");
            System.out.println("1.Customer reports\n2.Sales Reports\n3.Stock Reports\n4.Cancel");
            System.out.print("Enter the option:");
            int reportChoice = sc.nextInt();
            while (true) {
                switch (reportChoice) {
                    case 1:
                        customerReports.getCustomerReports();
                        return;
                    case 2:
                        salesReports.getSalesReports();
                        return ;
                    case 3:
                        stockReports.getStockReports();
                        return;
                    case 4:

                        return;
                    default:
                        System.out.println("Invalid choice");
                        System.out.println("Choose the payment mode:");
                        System.out.println("1.Customer reports\n2.Sales Reports\n3.Stock Reports\n4.Cancel");
                        reportChoice = sc.nextInt();
                }
            }
        }

        // Method to fetch and display customer reports

}
