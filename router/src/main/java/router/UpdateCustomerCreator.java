/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import domain.Account;

/**
 *
 * @author nikki
 */
public class UpdateCustomerCreator {
   
    public Account updateAccount(String id, String email, String firstName, String lastName, String group){
        Account updateCustomerAccount=new Account();
        updateCustomerAccount.setId(id);
        updateCustomerAccount.setEmail(email);
        updateCustomerAccount.setFirstName(firstName);
        updateCustomerAccount.setLastName(lastName);
        updateCustomerAccount.setGroup("0afa8de1-147c-11e8-edec-201e0f00872c");//VIP customer group
        return updateCustomerAccount;
    }
}
