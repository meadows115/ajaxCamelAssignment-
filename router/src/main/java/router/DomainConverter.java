/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import domain.Account;
import domain.Customer;

/**
 *
 * @author nikki Class to convert customer into account object
 */
public class DomainConverter {

    public Account customerToAccount(Customer customer) {
        return new Account(customer.getId(), customer.getEmail(), customer.getFirstName(), customer.getLastName(), customer.getGroup());
    }
}
