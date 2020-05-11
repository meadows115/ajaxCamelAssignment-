/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import domain.Account;
import domain.Customer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 *
 * @author nikki
 */
public class CustomerAccountBuilder extends RouteBuilder {

    @Override
    public void configure() {
        //Jetty endpoint to receive the account details from the AJAX client
        // create HTTP endpoint for receiving messages via HTTP
        from("jetty:http://localhost:9000/?enableCORS=true")
                // make message in-only so web browser doesn't have to wait on a non-existent response
                .setExchangePattern(ExchangePattern.InOnly)
                .log("${body}")
                // convert JSON to Account object
                .unmarshal().json(JsonLibrary.Gson, Account.class)
                .to("jms:queue:new-customer-account");

        //bean method to convert account into customer so compatible with Vend
        from("jms:queue:new-customer-account")
                .bean(CustomerCreator.class, "createCustomer(${body.firstName},${body.lastName}, ${body.email})")
                .to("jms:queue:send-to-vend");

// route to check that the account is actually showing 
//from("jms:queue:account-for-vend")
//  // convert to JSON using marshal method
//  .marshal().json(JsonLibrary.Gson)
//  // ensure the message body is a string
//  .convertBodyTo(String.class)
//  // send to a queue that expects JSON
//  .to("jms:queue:json");
//   
        //send the message to vend and catch response in another queue
        from("jms:queue:send-to-vend")
                // remove headers so they don't get sent to Vend
                .removeHeaders("*")
                // add authentication token to authorization header
                .setHeader("Authorization", constant("Bearer KiQSsELLtocyS2WDN5w5s_jYaBpXa0h2ex1mep1a"))
                // marshal to JSON
                .marshal().json(JsonLibrary.Gson) // only necessary if the message is an object, not JSON
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                // set HTTP method
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                // send it
                .to("https://info303otago.vendhq.com/api/2.0/customers")
                // store the response
                .to("jms:queue:vend-response");

        //extract the customer data and unmarshall into a customer object
        from("jms:queue:vend-response")
                .setBody().jsonpath("$.data")
                .marshal().json(JsonLibrary.Gson)
                // convert JSON to Customer object
                .unmarshal().json(JsonLibrary.Gson, Customer.class)
                .to("jms:queue:extracted-vend-response");
        
        //convert customer object into an account object
        from("jms:queue:extracted-vend-response")
                .bean(DomainConverter.class, "customerToAccount(${body})")
                .to("jms:queue:account-object");
       
        //marshall the account back into json and send to account service 
        from("jms:queue:account-object")
                //convert into json
                .marshal().json(JsonLibrary.Gson)
                //send to accounts service queue
                .to("jms:queue:account-service");
        
        //send to the accounts service local host
        from("jms:queue:account-service")
        .to("http://localhost:8086/api/accounts");
    }
}
