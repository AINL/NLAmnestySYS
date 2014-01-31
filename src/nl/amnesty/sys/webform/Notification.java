/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.webform;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.amnesty.crm.persistence.EMRole;
import nl.amnesty.smtp.controller.MessageController;
import nl.amnesty.webform.action.Action;

/**
 *
 * @author evelzen
 */
public class Notification {

    public static boolean send(List<String> recipientlist, String subject, long roleid, List<Action> actionlist, String roledetailsold, String roledetailsnew) {
        EMRole emrole = new EMRole();
        String text = "";
        try {
            for (Action action : actionlist) {
                SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String daterequest = simpledateformat.format(action.getDaterequest());
                String message = action.getMessage();
                String status = "";
                switch (action.getStatus()) {
                    case Action.ACTIONSTATUS_REQUESTED:
                        status = "Verzocht ";
                        break;
                    case Action.ACTIONSTATUS_NO_ACTION:
                        status = "Genegeerd";
                        break;
                    case Action.ACTIONSTATUS_COMPLETED:
                        status = "Voltooid ";
                        break;
                    case Action.ACTIONSTATUS_ABORTED:
                        status = "Mislukt  ";
                        break;
                    default:
                        status = "         ";
                        break;
                }
                if (daterequest==null) {
                    daterequest="";
                }
                if (status==null) {
                    status="";
                }
                if (message ==null) {
                    message="";
                }
                text = text.concat(daterequest).concat(" | ").concat(status).concat(" | ").concat(message);
                text = text.concat("\n");

            }

            text = text.concat("\n");
            for (Action action : actionlist) {
                if (action.getComment() != null) {
                    if (!action.getComment().isEmpty()) {
                        text = text.concat(action.getComment());
                        text = text.concat("\n");
                    }
                }
            }

            text = text.concat("\n");
            text = text.concat(roledetailsold);
            text = text.concat("\n");
            text = text.concat(roledetailsnew);

            MessageController.sendEmail(recipientlist, subject, text);
            return true;
        } catch (Exception e) {
            Logger.getLogger(Notification.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }
}
