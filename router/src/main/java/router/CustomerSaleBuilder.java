/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import domain.Customer;
import domain.Sale;
import domain.Summary;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 *
 * @author nikki
 */
public class CustomerSaleBuilder extends RouteBuilder {

    @Override
    public void configure() {

        // get Camel to look in my student email account for vend emails
//        from("imaps://outlook.office365.com?username=meani898@student.otago.ac.nz"
//                + "&password=" + getPassword("Enter your E-Mail password")
//                + "&searchTerm.subject=Vend:SaleUpdate"
//                + "&debugMode=false" // set to true if you want to see the authentication details
//                + "&folderName=INBOX") // change to whatever folder your Vend messages end up in
//                .convertBodyTo(String.class)
//                .log("${body}")
//                .to("jms:queue:vend-new-sale");
        from("imap://localhost?username=test@localhost"
                + "&port=3143"
                + "&password=password"
                + "&consumer.delay=5000"
                + "&searchTerm.subject=Vend:SaleUpdate")
                .log("Found new E-Mail: ${body}")
                .to("jms:queue:vend-new-sale");

        //extract the customers group, id, first name, last name and email
        from("jms:queue:vend-new-sale")
                .setProperty("custgroup").jsonpath("$.customer.customer_group_id") // extract ID from JSON, and store in header 
                .setProperty("firstName").jsonpath("$.customer.first_name") // same for name 
                .setProperty("lastName").jsonpath("$.customer.last_name")
                .setProperty("email").jsonpath("$.customer.email")
                .setProperty("id").jsonpath("$.customer.id")
                .to("jms:queue:extracted-properties");

        //convert the JSON payload into Java objects that are compatible with the sales service.
        from("jms:queue:extracted-properties")
                // convert JSON to Sale object
                .unmarshal().json(JsonLibrary.Gson, Sale.class)
                .to("jms:queue:sales-java-objects");

        //Create a sale on the phase 1 sales service.
        from("jms:queue:sales-java-objects")
                // remove headers
                .removeHeaders("*")
                // marshal to JSON
                .marshal().json(JsonLibrary.Gson)
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                // set HTTP method
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                // send it
                .to("http://localhost:8081/api/sales")
                .to("jms:queue:sales-service");

        //Retrieve ("get") the customer’s sales summary from the phase 1 sales service. 
        from("jms:queue:sales-service")
                // remove headers
                .removeHeaders("*")
                // remove message body since you can't send a body in a GET or DELETE
                //.setBody(constant(null))
                .setProperty("summary-customer-id").jsonpath("$.customer.id")
                // set HTTP method
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                //dynamic end point using simple expression language
                .toD("http://localhost:8081/api/sales/customer/${exchangeProperty.summary-customer-id}/summary")
                .setProperty("cust-id").simple("${exchangeProperty.summary-customer-id}")
                .to("jms:queue:customer-sales-summary");

        //extract the group from the sales summary
        from("jms:queue:customer-sales-summary")
                //  .marshal().json(JsonLibrary.Gson)
                .setProperty("group").jsonpath("$.group") //at this point the group is Regular Customers
                .to("jms:queue:customer-summary-group");

        //change the group name into the vend group ID
        from("jms:queue:customer-summary-group")
                .setProperty("groupcustomer").method(GroupCalculator.class,
                "calculateGroup(${exchangeProperty.group})")
                .to("jms:queue:calculated-group");
        
        //compare the calculated group with the current group to see if its changed
        from("jms:queue:calculated-group")
                .unmarshal().json(JsonLibrary.Gson, Summary.class)
                .choice()
                .when().simple("${exchangeProperty.groupcustomer} == ${exchangeProperty.custgroup}")
                .to("jms:queue:no-update-customer-group")
                .otherwise()
                .to("jms:queue:update-customer-group");      
    }

    //method to prompt for a password using the dialog box
    public static String getPassword(String prompt) {
        JPasswordField txtPasswd = new JPasswordField();
        int resp = JOptionPane.showConfirmDialog(null, txtPasswd, prompt,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            String password = new String(txtPasswd.getPassword());
            return password;
        }
        return null;
    }
}
