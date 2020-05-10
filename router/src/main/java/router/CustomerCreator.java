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
 * @author nikki
 */
public class CustomerCreator {

    public Account createCustomer(String firstName, String lastName, String email, String group) {
        Account newCustomerAccount = new Account();
        newCustomerAccount.setFirstName(firstName);
        newCustomerAccount.setLastName(lastName);
        newCustomerAccount.setEmail(email);
        newCustomerAccount.setGroup("0afa8de1-147c-11e8-edec-2b197906d816");
        return newCustomerAccount;
    }
}
