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
public class CustomerGeneratorObject {

    public Customer updatetheCustomer(Account account) {

        Customer cust = new Customer();
        cust.setId(account.getId());
        cust.setGroup(account.getGroup());
        cust.setFirstName(account.getFirstName());
        cust.setLastName(account.getLastName());
        cust.setEmail(account.getEmail());
        return cust;
    }
}
