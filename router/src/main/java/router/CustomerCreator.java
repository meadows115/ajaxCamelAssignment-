/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import domain.Customer;

/**
 *
 * @author nikki
 */
public class CustomerCreator {
  public Customer createCustomer(String firstName, String lastName, String email){ 
            return new Customer(firstName, lastName, email);
    }
}