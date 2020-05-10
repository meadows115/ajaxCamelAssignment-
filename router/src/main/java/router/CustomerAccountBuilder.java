/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import domain.Account;
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
                .to("jms:queue:account-for-vend");

// route to check that the account is actually showing 
//from("jms:queue:account-for-vend")
//  // convert to JSON using marshal method
//  .marshal().json(JsonLibrary.Gson)
//  // ensure the message body is a string
//  .convertBodyTo(String.class)
//  // send to a queue that expects JSON
//  .to("jms:queue:json");
//        
    }

}
