package Billing_System;

import java.sql.*;
import java.util.*;
public class Main{
    public static Connection DB_connection()
    {
        Connection conn=null;
        try{
            String url="jdbc:mysql://localhost:3306/supermarket_db";
            String username="root";
            String password="93455759";
            //String query="select * from customers where customer_id=2";
            conn=DriverManager.getConnection(url,username,password);
            //Statement st=conn.createStatement();
            //ResultSet rs= st.executeQuery(query);
//            rs.next();
//            System.out.println("id: "+ rs.getInt(1));
//            System.out.println("name: "+ rs.getString(2));
//            System.out.println("contact "+ rs.getString(3));
//            System.out.println("address "+ rs.getString(4));

            //con.close();
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);



        while(true)
        {
            System.out.println("\n----BILLING SYSTEM----");
            System.out.println("1.Bill Entry\n2.Reports\n3.Exit");
            System.out.print("Enter your choice:");
            int choice=sc.nextInt();
            if(choice==1)
            {
                int customerId=Customers.customer_details();
                int paymentDetails[]=BillEntry.Bill_details(customerId);//0->bill_id,1-grandTotal,2->coupon_amount
                if(Payment.proceedPayment(paymentDetails))
                {
                    Payment.Generate_Bill(paymentDetails[0]);
                }

            }
            else if(choice==2)
            {
                Reports.generateReports();

            }
            else if(choice==3)
            {
                return;
            }
            else
            {
                System.out.println("Invalid Input!!");
            }
        }
    }
}