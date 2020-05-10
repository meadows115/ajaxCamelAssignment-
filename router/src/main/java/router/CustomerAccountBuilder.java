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

        // routes go here
        //bean method?
        //Jetty endpoint to receive the account details from the AJAX client
        // create HTTP endpoint for receiving messages via HTTP
        from("jetty:http://localhost:9000/?enableCORS=true")
                // make message in-only so web browser doesn't have to wait on a non-existent response
                .setExchangePattern(ExchangePattern.InOnly)
                .log("${body}")
                // convert JSON to domain object
                .unmarshal().json(JsonLibrary.Gson, Account.class)
                .to("jms:queue:customer-account");
        //create customer account on vend 
    }

}
