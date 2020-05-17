/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

/**
 *
 * @author nikki
 */
public class GroupCalculator {
    
    public String calculateGroup (String group){
        
        String VIP="0afa8de1-147c-11e8-edec-201e0f00872c";
        String regular= "0afa8de1-147c-11e8-edec-2b197906d816";
        
        if(group.equals("VIP Customers")){
        return VIP;
        } else{
        return regular;
        }       
    }
}

