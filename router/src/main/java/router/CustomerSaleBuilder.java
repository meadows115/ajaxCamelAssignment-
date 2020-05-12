/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package router;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.apache.camel.builder.RouteBuilder;

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
//                + "&searchTerm.subject=<subject>")
//                .log("Found new E-Mail: ${body}")
//                .to("jms:queue:messages");
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
