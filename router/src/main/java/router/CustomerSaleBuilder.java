/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import domain.Account;
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
        from("imaps://outlook.office365.com?username=meani898@student.otago.ac.nz"
                + "&password=" + getPassword("Enter your E-Mail password")
                + "&searchTerm.subject=Vend:SaleUpdate"
                + "&debugMode=false" // set to true if you want to see the authentication details
                + "&folderName=INBOX") // change to whatever folder your Vend messages end up in
                .convertBodyTo(String.class)
                .log("${body}")
                .to("jms:queue:vend-new-sale");
//        from("imap://localhost?username=test@localhost"
//                + "&port=3143"
//                + "&password=password"
//                + "&consumer.delay=5000"
//                + "&searchTerm.subject=Vend:SaleUpdate")
//                .log("Found new E-Mail: ${body}")
//                .to("jms:queue:vend-new-sale");

        //extract the customers group, id, first name, last name and email
        from("jms:queue:vend-new-sale")
                .setProperty("id").jsonpath("$.customer.id")
                .setProperty("email").jsonpath("$.customer.email")
                .setProperty("firstName").jsonpath("$.customer.first_name") // same for name 
                .setProperty("lastName").jsonpath("$.customer.last_name")
                .setProperty("custgroup").jsonpath("$.customer.customer_group_id") // extract ID from JSON 
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
                // set HTTP method
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                //dynamic end point using simple expression language
                .toD("http://localhost:8081/api/sales/customer/${exchangeProperty.id}/summary")
                .setProperty("summarygroup").jsonpath("$.group")
                .to("jms:queue:customer-sales-summary");

        //change the group name into the vend group ID
        from("jms:queue:customer-sales-summary")
                .log("step 3 - ${body}")
                .setProperty("groupcustomer").method(GroupCalculator.class,
                "calculateGroup(${exchangeProperty.summarygroup})")
                .log("step 4 - ${body}")
                .to("jms:queue:calculated-group");

        //compare the calculated group with the current group to see if its changed
        from("jms:queue:calculated-group")
                .unmarshal().json(JsonLibrary.Gson, Summary.class)
                .choice()
                .when().simple("${exchangeProperty.custgroup} == ${exchangeProperty.groupcustomer}")
                .log("The customer group has not changed: ${body.group} == ${exchangeProperty.groupcustomer}")
                .to("jms:queue:no-update-customer-group")
                .otherwise()
                .log("The customer group has changed: ${exchangeProperty.custgroup} != ${exchangeProperty.groupcustomer}")
                .to("jms:queue:update-customer-group");

        from("jms:queue:update-customer-group")
                .bean(UpdateCustomerCreator.class, "updateAccount(${exchangeProperty.id}, ${exchangeProperty.email},${exchangeProperty.firstName}, ${exchangeProperty.lastName})")
                .multicast()
                .log("step 5 - ${body}")
                .to("jms:queue:updated-customer-account", "jms:queue:update-for-vend");

        //if the group has changed, needs to be updated customer accounts service
        from("jms:queue:updated-customer-account")
                // marshal to JSON
                .marshal().json(JsonLibrary.Gson)
                // remove headers
                .removeHeaders("*")
                // set HTTP method
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                .log("step 6 - ${body}") 
                .toD("http://localhost:8086/api/accounts/account/${exchangeProperty.id}")
                .log("step 7 - ${body}")
                .to("jms:queue:customer-account-updated");

        //updated account needs to be updated on Vend
//        from("jms:queue:update-for-vend")
//                // remove headers so they don't get sent to Vend
//                .removeHeaders("*")
//                // add authentication token to authorization header
//                .setHeader("Authorization", constant("Bearer KiQSsELLtocyS2WDN5w5s_jYaBpXa0h2ex1mep1a"))
//                // marshal to JSON
//                .marshal().json(JsonLibrary.Gson) // only necessary if the message is an object, not JSON
//                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
//                // set HTTP method
//                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
//                // send it
//                .to("https://info303otago.vendhq.com/api/2.0/customers/${exchangeProperty.customer.id}")
//                // store the response
//                .to("jms:queue:vend-up date-response");
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
