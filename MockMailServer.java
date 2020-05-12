
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nikki
 */
public class MockMailServer {
    
    public static void main(String[] args) throws IOException {
        String subject = "<subject>";
        String body = "{\"created_at\":\"2020-05-12 21:09:33\",\"customer\":{\"balance\":\"0.00000\",\"company_name\":null,\"contact_first_name\":null,\"contact_last_name\":null,\"created_at\":\"2018-01-30 03:34:09\",\"custom_field_1\":null,\"custom_field_2\":null,\"custom_field_3\":null,\"custom_field_4\":null,\"customer_code\":\"WALKIN\",\"customer_group_id\":\"0afa8de1-147c-11e8-edec-201e0f00872c\",\"date_of_birth\":null,\"deleted_at\":null,\"do_not_email\":false,\"email\":null,\"enable_loyalty\":false,\"fax\":null,\"first_name\":null,\"id\":\"0afa8de1-1450-11e8-edec-056e6ec3a1d6\",\"last_name\":null,\"loyalty_balance\":\"0.00000\",\"mobile\":null,\"note\":null,\"phone\":null,\"points\":0,\"sex\":null,\"updated_at\":\"2020-05-08 07:55:20\",\"year_to_date\":\"0.00000\"},\"customer_id\":\"0afa8de1-1450-11e8-edec-056e6ec3a1d6\",\"deleted_at\":null,\"id\":\"f184284e-acda-931d-11ea-949465993256\",\"invoice_number\":\"3\",\"note\":\"\",\"outlet_id\":\"069db350-8d7c-11ea-f6a9-89c142e47d9e\",\"register_id\":\"069db350-8d7c-11ea-f6a9-89c14b0cdd92\",\"register_sale_payments\":[{\"amount\":69.9,\"id\":\"f184284e-acda-931d-11ea-9494dca71a18\",\"payment_date\":\"2020-05-12T21:09:26Z\",\"payment_type\":{\"has_native_support\":false,\"id\":\"1\",\"name\":\"Cash\"},\"payment_type_id\":1,\"retailer_payment_type\":{\"config\":null,\"id\":\"0afa8de1-1450-11e8-edec-056e6ed01696\",\"name\":\"Cash\",\"payment_type_id\":\"1\"},\"retailer_payment_type_id\":\"0afa8de1-1450-11e8-edec-056e6ed01696\"}],\"register_sale_products\":[{\"discount\":\"0.00000\",\"id\":\"f184284e-acda-931d-11ea-949467619647\",\"is_return\":false,\"loyalty_value\":\"0.00000\",\"note\":null,\"price\":\"60.78261\",\"price_set\":false,\"price_total\":\"60.78261\",\"product_id\":\"0afa8de1-147c-11e8-edec-056e701f4190\",\"quantity\":1,\"tax\":\"9.11739\",\"tax_id\":\"0afa8de1-1450-11e8-edec-056e6ec70277\",\"tax_total\":\"9.11739\"}],\"return_for\":null,\"sale_date\":\"2020-05-12T21:09:26Z\",\"short_code\":\"ta7slw\",\"source\":\"USER\",\"source_id\":null,\"status\":\"CLOSED\",\"taxes\":[{\"id\":\"6ecd4ad7-056e-11e8-adec-0afa8de11450\",\"name\":\"GST\",\"rate\":\"0.15000\",\"tax\":9.11739}],\"totals\":{\"total_loyalty\":\"0.00000\",\"total_payment\":\"69.90000\",\"total_price\":\"60.78261\",\"total_tax\":\"9.11739\",\"total_to_pay\":\"0.00000\"},\"updated_at\":\"2020-05-12T21:09:33+00:00\",\"user\":{\"created_at\":\"2020-04-29 03:18:43\",\"display_name\":\"Nikki\",\"email\":\"meani898@student.otago.ac.nz\",\"id\":\"069db350-8d7c-11ea-f6a9-89c8216589f7\",\"name\":\"meani898\",\"target_daily\":null,\"target_monthly\":null,\"target_weekly\":null,\"updated_at\":\"2020-04-29 03:18:43\"},\"user_id\":\"069db350-8d7c-11ea-f6a9-89c8216589f7\",\"version\":13973750110}";

        // create a mock server than can receive (SMTP) and host (IMAP)
        GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP_IMAP);

        // add a user
        greenMail.setUser("test@localhost", "test@localhost", "password");

        // start the server
        greenMail.start();

        System.out.println("IMAP Server Ready on port " + greenMail.getImap().getPort());

        // a loop for sending 5 messages
        for (int i = 0; i < 5; i++) {

            // let the user press enter to send a message
            System.out.println("Press Enter to send E-Mail " + (i + 1) + " of 5.");
            System.in.read();

            // send a message
            GreenMailUtil.sendTextEmailTest("test@localhost", "test@localhost",
                      subject, body);
        }

        // shutdown
        System.out.println("All E-Mails sent.  Press enter to shutdown mail server.");
        System.in.read();
        greenMail.stop();
    }
}
