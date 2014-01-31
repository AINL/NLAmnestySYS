package nl.amnesty.sys.controller;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.amnesty.crm.config.NetworkDef;
import nl.amnesty.crm.entity.*;
import nl.amnesty.crm.persistence.*;
import nl.amnesty.postal.controller.PostalrangeController;
import nl.amnesty.postal.entity.Postalrange;
import nl.amnesty.smtp.controller.MessageController;
import nl.amnesty.sys.webform.Notification;
import nl.amnesty.sys.webform.entity.Form;
import nl.amnesty.sys.webform.entity.Property;
import nl.amnesty.webform.action.Action;
import nl.amnesty.webform.config.WebformDef;
import nl.amnesty.webform.config.WebformDef.operation;
import nl.amnesty.webform.exception.WEBFORMWebformException;
import nl.amnesty.webform.http.FormHTTP;
import nl.amnesty.webform.mapping.Attributelink;
import nl.amnesty.webform.mapping.Mapping;

/**
 *
 * @author evelzen
 */
public class CRMWebformController {

    private static final String NOTIFICATION_ESCALATE = "logwebservices@amnesty.nl";
    //private static Properties networknames;

    /*
     * private static void init() { networknames = new Properties();
     * networknames.setProperty("238", "WFR"); networknames.setProperty("279",
     * "RSV"); networknames.setProperty("280", "TXT");
     * networknames.setProperty("281", "UAN"); networknames.setProperty("283",
     * "EAN"); networknames.setProperty("345", "JAN");
     * networknames.setProperty("4816", "GRT"); networknames.setProperty("5951",
     * ""); }
     *
     */
    public static boolean processForm(Form form) {
        EMRole emrole = new EMRole();
        List<Action> actionlist = new ArrayList();
        Address addressold;
        Address addressnew;
        String roledetailsold = "";
        String roledetailsnew = "";
        try {
            markeerSpecialeFormulieren(form);
            WebformDef webformdef = getWebformdef(form);
            if (webformdef == null) {
                return false;
            }
            Properties sourcevalues = getWebformvalues(form);
            Properties targetvalues = getCRMobjectvalues(webformdef, sourcevalues);
            meldingOntbrekendeVeldverwerking(form, webformdef, actionlist, roledetailsold, roledetailsnew);

            Role role = new Role();
            // Map all webform data to all role objects, including person, address, etc.
            role.mapPropertyValue(targetvalues, webformdef.getSimpledateformat());
            // Either create a new role or return an existing role that matches
            Role rolepersisted = emrole.persist(role);
            // A persistence error has occured
            if (rolepersisted == null) {
                List<String> recipientlist = new ArrayList();
                recipientlist.add(NOTIFICATION_ESCALATE);
                String subject;
                subject = "Webform ERROR id: " + form.getId();
                Action action = new Action();
                action.setMessage("Unable to persist role.");
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                actionlist.add(action);
                // Uitgebreidere debug (formulierwaarden)
                for (Property property : form.getPropertylist()) {
                    action = new Action();
                    action.setMessage("Key [" + property.getKey() + "] value [" + property.getValue() + "]");
                    action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                    actionlist.add(action);
                }
                Notification.send(recipientlist, subject, 0, actionlist, roledetailsold, roledetailsnew);
                return false;
            }

            // Register the old details of the matched or created role
            roledetailsold = emrole.roleDetailsPlaintext(rolepersisted);

            // Do possible change of address if we have found a matching existing role
            if (!rolepersisted.isNew()) {
                addressold = rolepersisted.getAddress();
                addressnew = role.getAddress();
                if (addressnew.isDecentAddress()) {
                    actionlist.add(changeAddress(rolepersisted, addressold, addressnew));
                }
            }

            // Get phone, url and bankaccount values
            Phone phone = rolepersisted.getPhone(role.getPhonenumber());
            URL url = rolepersisted.getEmail(role.getEmailinternetaddress());
            Bankaccount bankaccount = rolepersisted.getBankaccount(role.getBankaccountnumber());

            for (operation o : webformdef.getOperationlist()) {
                switch (o) {
                    case ADDADDRESS:
                        break;
                    case ADDBANKACCOUNT:
                        break;
                    case ADDCHANNEL:
                        break;
                    case ADDCOMMITMENT:
                        Commitment commitment = new Commitment();
                        // Broncode commitment, toevoegen bij propertyvalues
                        commitment.mapPropertyValue(targetvalues);
                        Action action = new Action();
                        action.setMessage("Frequency: "+commitment.getFrequency());
                        action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                        actionlist.add(action);
                        actionlist.add(addCommitment(rolepersisted, bankaccount, commitment));
                        break;
                    case ADDFINANCE:
                        Finance finance = new Finance();
                        finance.mapPropertyValue(targetvalues);
                        Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, "DEBUG: "+finance.getAmountdue());
                        action = new Action();
                        action.setMessage("Amount: "+finance.getAmountdue());
                        action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                        actionlist.add(action);
                        actionlist.add(addFinance(rolepersisted, bankaccount, finance));
                        break;
                    case ADDCONTACT:
                        Contact contact = new Contact();
                        contact.mapPropertyValue(targetvalues);
                        long formid=form.getId();
                        if (contact.getContent() == null) {
                            contact.setContent("nid: " + form.getId() + ";");
                        } else {
                            if (!contact.getContent().startsWith("nid")) {
                                contact.setContent("nid: " + form.getId() + "; " + contact.getContent());
                            } else {
                                formid=getFirstNumber(contact.getContent());
                            }
                        }
                        contact.setUsernumb5(formid);
                        actionlist.add(addContact(rolepersisted, contact));
                        break;
                    case ADDCOUNTRY:
                        break;
                    case ADDDOCUMENT:
                        break;
                    case ADDFLAG:
                        break;
                    case ADDGROUP:
                        break;
                    case ADDINVOLVEMENT:
                        Involvement involvement = new Involvement();
                        involvement.mapPropertyValue(targetvalues);
                        actionlist.add(addInvolvement(rolepersisted, involvement));
                        break;
                    case ADDNETWORK:
                        Network network = new Network();
                        network.mapPropertyValue(targetvalues);
                        actionlist.add(addNetwork(rolepersisted, network));
                        break;
                    case ADDPERSON:
                        break;
                    case ADDPHONE:
                        break;
                    case ADDPRODUCT:
                        break;
                    case ADDRELATION:
                        Relation relation = new Relation();
                        relation.mapPropertyValue(targetvalues);
                        break;
                    case ADDROLE:
                        break;
                    case ADDSUBSCRIPTION:
                        Subscription subscription = new Subscription();
                        Product product = new Product();
                        product.mapPropertyValue(targetvalues);
                        subscription.mapPropertyValue(targetvalues, webformdef.getSimpledateformat());
                        // Formulier 16122 deelt 3 nummers WV uit. Gezien onregelmatige verschijning, en dubbelnummers een lastige
                        // rekensom om de stopdatum te bepalen
                        if (form.getId()==16122) {
                            bepaalEinddatum(subscription);                            
                        }
                        actionlist.add(addSubscription(rolepersisted, product, subscription));
                        break;
                    case ADDURL:
                        break;
                    case CHANGEADDRESS:
                        addressold = new Address();
                        addressnew = new Address();
                        addressold.mapPropertyValueOld(targetvalues);
                        addressnew.mapPropertyValueNew(targetvalues);
                        actionlist.add(changeAddress(rolepersisted, addressold, addressnew));
                        break;
                    case CHANGEBANKACCOUNT:
                        break;
                    case CHANGECHANNEL:
                        break;
                    case CHANGECOMMITMENT:
                        break;
                    case CHANGECONTACT:
                        break;
                    case CHANGECOUNTRY:
                        break;
                    case CHANGEDOCUMENT:
                        break;
                    case CHANGEFLAG:
                        break;
                    case CHANGEGROUP:
                        break;
                    case CHANGEINVOLVEMENT:
                        break;
                    case CHANGENETWORK:
                        break;
                    case CHANGEPERSON:
                        break;
                    case CHANGEPHONE:
                        Phone phoneold = new Phone();
                        Phone phonenew = new Phone();
                        phoneold.mapPropertyValueOld(targetvalues);
                        phonenew.mapPropertyValueNew(targetvalues);
                        actionlist.add(changePhone(rolepersisted, phoneold, phonenew));
                        break;
                    case CHANGEPRODUCT:
                        break;
                    case CHANGERELATION:
                        break;
                    case CHANGEROLE:
                        break;
                    case CHANGESUBSCRIPTION:
                        break;
                    case CHANGEURL:
                        URL urlold = new URL();
                        URL urlnew = new URL();
                        urlold.mapPropertyValueOld(targetvalues);
                        urlnew.mapPropertyValueNew(targetvalues);
                        actionlist.add(changeEmail(rolepersisted, urlold, urlnew));
                        break;
                    case REMOVENETWORK:
                        network = new Network();
                        network.mapPropertyValue(targetvalues);
                        actionlist.add(removeNetwork(rolepersisted, network));
                        break;
                    case SENDRESPONSE:
                        actionlist.add(sendResponse());
                        break;
                    default:
                        break;
                }
            }

            // DEBUG
            for (Property property : form.getPropertylist()) {
                Action action = new Action();
                action.setMessage("Key [" + property.getKey() + "] value [" + property.getValue() + "]");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                actionlist.add(action);
            }


            // Register the new details of modified role
            roledetailsnew = emrole.roleDetailsPlaintext(rolepersisted);

            List<String> recipientlist = new ArrayList();
            if (webformdef.getNotification() != null) {
                if (!webformdef.getNotification().isEmpty()) {
                    recipientlist.add(webformdef.getNotification());
                }
            }
            //DEBUG
            recipientlist.add(NOTIFICATION_ESCALATE);
            if (!recipientlist.isEmpty()) {
                String subject = formatSubject(webformdef);
                Notification.send(recipientlist, subject, rolepersisted.getRoleid(), actionlist, roledetailsold, roledetailsnew);
            }
            return true;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            List<String> recipientlist = new ArrayList();
            recipientlist.add(NOTIFICATION_ESCALATE);
            String subject = "Webform ERROR";
            Action action = new Action();
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            actionlist.add(action);
            Notification.send(recipientlist, subject, 0, actionlist, roledetailsold, roledetailsnew);
            return false;
        }
    }

    public static boolean processNetworkAdd(Form form, long roleid) {
        List<Action> actionlist = new ArrayList();
        EMRole emrole = new EMRole();
        String roledetailsold = "";
        String roledetailsnew = "";
        try {
            WebformDef webformdef = getWebformdef(form);
            if (webformdef == null) {
                return false;
            }
            String networkname = webformdef.getNetworkname();
            Role rolefound = emrole.find(roleid);
            if (rolefound == null) {
                return false;
            }

            // Register the old details of role found
            roledetailsold = emrole.roleDetailsPlaintext(rolefound);

            // DEBUG
            for (Property property : form.getPropertylist()) {
                Action action = new Action();
                action.setMessage("Key [" + property.getKey() + "] value [" + property.getValue() + "]");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                actionlist.add(action);
            }

            Network network = new Network();
            network.setName(networkname);
            Action action = addNetwork(rolefound, network);
            actionlist.add(action);
            List<String> recipientlist = new ArrayList();
            if (webformdef.getNotification() != null) {
                if (!webformdef.getNotification().isEmpty()) {
                    recipientlist.add(webformdef.getNotification());
                }
            }

            // Register the new details of role found
            roledetailsnew = emrole.roleDetailsPlaintext(rolefound);

            recipientlist.add(NOTIFICATION_ESCALATE);
            String subject = formatSubject(webformdef);
            Notification.send(recipientlist, subject, rolefound.getRoleid(), actionlist, roledetailsold, roledetailsnew);
            return true;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            List<String> recipientlist = new ArrayList();
            recipientlist.add(NOTIFICATION_ESCALATE);
            String subject = "Webform ERROR";
            Action action = new Action();
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            actionlist.add(action);
            Notification.send(recipientlist, subject, 0, actionlist, roledetailsold, roledetailsnew);
            return false;
        }
    }

    public static boolean processNetworkRemove(Form form, long roleid) {
        List<Action> actionlist = new ArrayList();
        EMRole emrole = new EMRole();
        String roledetailsold = "";
        String roledetailsnew = "";
        try {
            WebformDef webformdef = getWebformdef(form);
            if (webformdef == null) {
                return false;
            }
            String networkname = webformdef.getNetworkname();
            Role rolefound = emrole.find(roleid);
            if (rolefound == null) {
                return false;
            }

            // Register the old details of role found
            roledetailsold = emrole.roleDetailsPlaintext(rolefound);

            // DEBUG
            for (Property property : form.getPropertylist()) {
                Action action = new Action();
                action.setMessage("Key [" + property.getKey() + "] value [" + property.getValue() + "]");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                actionlist.add(action);
            }

            Network network = new Network();
            network.setName(networkname);
            Action action = removeNetwork(rolefound, network);
            actionlist.add(action);
            List<String> recipientlist = new ArrayList();
            if (webformdef.getNotification() != null) {
                if (!webformdef.getNotification().isEmpty()) {
                    recipientlist.add(webformdef.getNotification());
                }
            }

            // Register the new details of role found
            roledetailsnew = emrole.roleDetailsPlaintext(rolefound);

            recipientlist.add(NOTIFICATION_ESCALATE);
            String subject = formatSubject(webformdef);
            Notification.send(recipientlist, subject, rolefound.getRoleid(), actionlist, roledetailsold, roledetailsnew);
            return true;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            List<String> recipientlist = new ArrayList();
            recipientlist.add(NOTIFICATION_ESCALATE);
            String subject = "Webform ERROR";
            Action action = new Action();
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            actionlist.add(action);
            Notification.send(recipientlist, subject, 0, actionlist, roledetailsold, roledetailsnew);
            return false;
        }
    }

    /*
     * public static boolean processNetworkPartof(Form form, long roleid) {
     * List<Action> actionlist = new ArrayList(); EMRole emrole = new EMRole();
     * try { WebformDef webformdef = getWebformdef(form); if (webformdef ==
     * null) { return false; } String networkname = webformdef.getNetworkname();
     * Role rolefound = emrole.find(roleid); if (rolefound == null) { return
     * false; }
     *
     * // DEBUG for (Property property : form.getPropertylist()) { Action
     * action = new Action(); action.setMessage("Key [" + property.getKey() + "]
     * value [" + property.getValue() + "]");
     * action.setStatus(Action.ACTIONSTATUS_NO_ACTION); actionlist.add(action);
     * }
     *
     * Network network = new Network(); network.setName(networkname); Action
     * action = partofNetwork(rolefound, network);
     *
     * // DEBUG actionlist.add(action); List<String> recipientlist = new
     * ArrayList(); if (webformdef.getNotification() != null) { if
     * (!webformdef.getNotification().isEmpty()) {
     * recipientlist.add(webformdef.getNotification()); } }
     * recipientlist.add(NOTIFICATION_ESCALATE); String subject =
     * formatSubject(webformdef); Notification.send(recipientlist, subject,
     * rolefound.getRoleid(), actionlist);
     *
     * if (action.getStatus() == Action.ACTIONSTATUS_REQUESTED) { return false;
     * } else { return true; } } catch (Exception e) {
     * Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE,
     * e.getMessage(), e); List<String> recipientlist = new ArrayList();
     * recipientlist.add(NOTIFICATION_ESCALATE); String subject = "Webform
     * ERROR"; Action action = new Action(); action.setMessage(e.getMessage());
     * action.setStatus(Action.ACTIONSTATUS_ABORTED); actionlist.add(action);
     * Notification.send(recipientlist, subject, 0, actionlist); return false; }
     * }
     *
     */
    private static Action addCommitment(Role role, Bankaccount bankaccountnew, Commitment commitmentnew) {
        Action action = new Action();
        try {
            EMCommitment emcommitment = new EMCommitment();
            List<Bankaccount> bankaccountlist = role.getBankaccountlist();
            if (bankaccountlist != null) {
                boolean found = false;
                for (Bankaccount element : bankaccountlist) {
                    if (element.getNumber() == bankaccountnew.getNumber()) {
                        bankaccountnew = element;
                        found = true;
                    }
                }
                // Create financial commitment based on the amount and given payment frequency
                if (found) {
                    commitmentnew.setRoleid(role.getRoleid());
                    commitmentnew.setBankaccountid(bankaccountnew.getBankaccountid());
                    commitmentnew = emcommitment.persist(commitmentnew);
                    if (commitmentnew == null) {
                        action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het registreren van incasso voor lidnummer {0}.", new Object[]{role.getFormattedRoleid()}));
                        action.setStatus(Action.ACTIONSTATUS_ABORTED);
                        return action;
                    } else {
                        action.setMessage(MessageFormat.format("Incasso geregistreerd voor lidnummer {0} (bedrag {1} {2}, rekeningnummer {3})", new Object[]{role.getFormattedRoleid(), commitmentnew.getFormattedAmount(), commitmentnew.getFormattedFrequency(), bankaccountnew.getFormattedNumber()}));
                        action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                        return action;
                    }
                } else {
                    action.setMessage(MessageFormat.format("Het bankrekeningnummer {0} bestaat niet voor persoon met lidnummer {1}.", new Object[]{bankaccountnew.getFormattedNumber(), role.getFormattedRoleid()}));
                    action.setStatus(Action.ACTIONSTATUS_ABORTED);
                    return action;
                }
            } else {
                action.setMessage(MessageFormat.format("Voor het persoon met lidnummer {0} zijn geen bankrekeningnummers geregistreerd.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }
    
      private static Action addFinance(Role role, Bankaccount bankaccountnew, Finance financenew) {
        Action action = new Action();
        try {
            EMFinance emfinance = new EMFinance();
            List<Bankaccount> bankaccountlist = role.getBankaccountlist();
            if (bankaccountlist != null) {
                boolean found = false;
                for (Bankaccount element : bankaccountlist) {
                    if (element.getNumber() == bankaccountnew.getNumber()) {
                        bankaccountnew = element;
                        found = true;
                    }
                }
                // Create financial commitment based on the amount and given payment frequency
                if (found) {
                    financenew.setRoleid(role.getRoleid());
                    financenew.setBankkey(bankaccountnew.getBankaccountid());
                    financenew = emfinance.persist(financenew);
                    if (financenew == null) {
                        action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het registreren van eenmalige betaling voor lidnummer {0}.", new Object[]{role.getFormattedRoleid()}));
                        action.setStatus(Action.ACTIONSTATUS_ABORTED);
                        return action;
                    } else {
                        action.setMessage(MessageFormat.format("Eenmalige betaling geregistreerd voor lidnummer {0} (bedrag {1}, rekeningnummer {2})", new Object[]{role.getFormattedRoleid(), financenew.getAmountdue(), bankaccountnew.getFormattedNumber()}));
                        action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                        return action;
                    }
                } else {
                    action.setMessage(MessageFormat.format("Het bankrekeningnummer {0} bestaat niet voor persoon met lidnummer {1}.", new Object[]{bankaccountnew.getFormattedNumber(), role.getFormattedRoleid()}));
                    action.setStatus(Action.ACTIONSTATUS_ABORTED);
                    return action;
                }
            } else {
                action.setMessage(MessageFormat.format("Voor het persoon met lidnummer {0} zijn geen bankrekeningnummers geregistreerd.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action changeAddress(Role role, Address addressold, Address addressnew) {
        Action action = new Action();
        try {
            if (role.getStatus() == Role.STATUS_NEW) {
                // Makes no sense to change an address for a role we just created
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (addressold == null) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (addressnew == null) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (addressold.getPostalcodenumeric() == 0) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (addressnew.getPostalcodenumeric() == 0) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (addressold.getHouseno() == 0) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (addressnew.getHouseno() == 0) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            //Address addressold = roleold.getAddress();
            //Address addressnew = rolenew.getAddress();
            Postalrange postalrangeold = PostalrangeController.readViaPostalcodeHouseno(addressold.getPostalcodenumeric(), addressold.getPostalcodealpha(), addressold.getHouseno());
            if (postalrangeold == null) {
                // TODO: Change of address cannot be processed: find alternative way to get this done manually!
                action.setMessage(MessageFormat.format("Oud adres {0} {1} {2} is geen geldig adres.", new Object[]{addressold.getFormattedStreetHouseno(), addressold.getFormattedPostalcode(), addressold.getFormattedCity()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            addressold.setStreet(postalrangeold.getStraatnaamofficieel().trim());
            addressold.setCity(postalrangeold.getWoonplaatstnt().trim());

            Postalrange postalrangenew = PostalrangeController.readViaPostalcodeHouseno(addressnew.getPostalcodenumeric(), addressnew.getPostalcodealpha(), addressnew.getHouseno());
            if (postalrangenew == null) {
                // TODO: Change of address cannot be processed: find alternative way to get this done manually!
                action.setMessage(MessageFormat.format("Oud adres {0} {1} {2} is geen geldig adres.", new Object[]{addressold.getFormattedStreetHouseno(), addressold.getFormattedPostalcode(), addressold.getFormattedCity()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            addressnew.setStreet(postalrangenew.getStraatnaamofficieel().trim());
            addressnew.setCity(postalrangenew.getWoonplaatstnt().trim());

            // Do nothing if new address is the same as old address
            if (addressnew.isEqual(addressold)) {
                action.setMessage(MessageFormat.format("Nieuw adres ({0} {1} {2}) is al bekend of is gelijk aan oud adres", new Object[]{addressnew.getFormattedStreetHouseno(), addressnew.getFormattedPostalcode(), addressold.getFormattedCity()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }

            EMRole emrole = new EMRole();
            Role updatedrole = emrole.roleChangeAddress(role, addressold, addressnew);
            if (updatedrole == null) {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het wijzigen van het adres. Oud adres (postcode {0} huisnummer {1}), nieuw adres (postcode {2} huisnummer {3})", new Object[]{addressold.getFormattedPostalcode(), addressold.getHouseno(), addressnew.getFormattedPostalcode(), addressnew.getHouseno()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Adres gewijzigd van {0} {1} {2} naar {3} {4} {5}.", new Object[]{addressold.getFormattedStreetHouseno(), addressold.getFormattedPostalcode(), addressnew.getFormattedCity(), addressnew.getFormattedStreetHouseno(), addressnew.getFormattedPostalcode(), addressnew.getFormattedCity()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action addPhone(Role role, Phone phonenew) {
        Action action = new Action();
        try {
            if (phonenew == null) {
                // Nothing to do
                //action.setMessage(MessageFormat.format("Nieuw telefoonnummer voor persoon met lidnummer {0} ontbreekt of is ongeldig.", new Object[]{role.getFormattedRoleid()}));
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (phonenew.getNumber() == 0) {
                // Nothing to do
                //action.setMessage(MessageFormat.format("Nieuw telefoonnummer voor persoon met lidnummer {0} ontbreekt of is ongeldig.", new Object[]{role.getFormattedRoleid()}));
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }

            EMRole emrole = new EMRole();
            Role updatedrole = emrole.roleAddPhone(role, phonenew);
            if (updatedrole == null) {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het toevoegen van het telefoonnummer {0}.", new Object[]{role.getPhone().getFormattedNumber()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Telefoonnummer {0} is toegevoegd.", new Object[]{phonenew.getFormattedNumber()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action addEmail(Role role, URL urlnew) {
        Action action = new Action();
        try {
            if (urlnew == null) {
                // Nothing to do
                //action.setMessage(MessageFormat.format("Nieuw emailadres voor persoon met lidnummer {0} ontbreekt of is ongeldig.", new Object[]{role.getFormattedRoleid()}));
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (!urlnew.isInternetAddress()) {
                // Nothing to do
                //action.setMessage(MessageFormat.format("Nieuw emailadres voor persoon met lidnummer {0} ontbreekt of is ongeldig.", new Object[]{role.getFormattedRoleid()}));
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }

            EMRole emrole = new EMRole();
            Role updatedrole = emrole.roleAddEmail(role, urlnew);
            if (updatedrole == null) {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het toevoegen van het emailadres {0}.", new Object[]{urlnew.getInternetAddress()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Emailadres {0} is toegevoegd.", new Object[]{urlnew.getInternetAddress()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action addBankaccount(Role role, Bankaccount bankaccountnew) {
        Action action = new Action();
        try {
            EMRole emrole = new EMRole();
            if (bankaccountnew == null) {
                action.setMessage(MessageFormat.format("Bankrekeningnummer voor persoon met lidnummer {0} ontbreekt.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            if (!bankaccountnew.isValidNumber()) {
                action.setMessage(MessageFormat.format("Bankrekeningnummer voor persoon met lidnummer {0} is ongeldig.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            Role updatedrole = emrole.roleAddBankaccount(role, bankaccountnew);
            if (updatedrole == null) {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het toevoegen van het bankrekeningnummer {0}.", new Object[]{bankaccountnew.getFormattedNumber()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Bankrekeningnummer {0} is toegevoegd.", new Object[]{bankaccountnew.getFormattedNumber()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action changePhone(Role role, Phone phoneold, Phone phonenew) {
        Action action = new Action();
        try {
            if (role.getPerson() == null) {
                action.setMessage(MessageFormat.format("Persoon met lidnummer {0} heeft geen geldig persoonsrecord.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            if (role.getAddress() == null) {
                action.setMessage(MessageFormat.format("Persoon met lidnummer {0} heeft geen geldig adresrecord.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            if (phoneold == null) {
                // Nothing to do
                //action.setMessage(MessageFormat.format("Oud telefoonnummer voor persoon met lidnummer {0} ontbreekt of is ongeldig.", new Object[]{roleold.getFormattedRoleid()}));
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (phoneold.getNumber() == 0) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (phonenew == null) {
                // Nothing to do
                //action.setMessage(MessageFormat.format("Nieuw telefoonnummer voor persoon met lidnummer {0} ontbreekt of is ongeldig.", new Object[]{roleold.getFormattedRoleid()}));
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (phonenew.getNumber() == 0) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }

            EMRole emrole = new EMRole();
            Role updatedrole = emrole.roleChangePhone(role.getPerson(), role.getAddress(), phoneold, phonenew);
            if (updatedrole == null) {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het wijzigen van het telefoonnummer. Oud telefoonnummer {0}, nieuw telefoonnummer {1}", new Object[]{phoneold.getFormattedNumber(), phonenew.getFormattedNumber()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Telefoonnummer gewijzigd van {0} naar {1}", new Object[]{phoneold.getFormattedNumber(), phonenew.getFormattedNumber()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action changeEmail(Role role, URL urlold, URL urlnew) {
        Action action = new Action();
        try {
            if (role.getPerson() == null) {
                action.setMessage(MessageFormat.format("Persoon met lidnummer {0} heeft geen geldig persoonsrecord.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            if (role.getAddress() == null) {
                action.setMessage(MessageFormat.format("Persoon met lidnummer {0} heeft geen geldig adresrecord.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            if (urlold == null) {
                // Nothing to do
                //action.setMessage(MessageFormat.format("Oud emailadres voor persoon met lidnummer {0} ontbreekt of is ongeldig.", new Object[]{roleold.getFormattedRoleid()}));
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (urlold.getInternetAddress().isEmpty()) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }
            if (urlnew.getInternetAddress().isEmpty()) {
                // Nothing to do
                action.setMessage("");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                return action;
            }

            EMRole emrole = new EMRole();
            Role updatedrole = emrole.roleChangeEmail(role.getPerson(), role.getAddress(), urlold, urlnew);
            if (updatedrole == null) {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het wijzigen van het emailadres. Oud emailadres {0}, nieuw emailadres {1}", new Object[]{urlold.getInternetAddress(), urlnew.getInternetAddress()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Emailadres gewijzigd van {0} naar {1}", new Object[]{urlold.getInternetAddress(), urlnew.getInternetAddress()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action addInvolvement(Role role, Involvement involvement) {
        // TODO: Make this method generic instead of for LA ACTINF only!!!
        Action action = new Action();
        try {
            EMInvolvement eminvolvement = new EMInvolvement();
            // Register active membership "LA ACTINF"
            if (involvement.getName() != null) {
                if (involvement.getName().equals(Involvement.INVOLVEMENT_NAME_ACTIVE_MEMBERSHIP)) {
                    involvement.setName(Involvement.INVOLVEMENT_NAME_ACTIVE_MEMBERSHIP);
                    involvement.setDescription(Involvement.INVOLVEMENT_DESCRIPTION_ACTIVE_MEMBERSHIP);
                    involvement.setRoleid(role.getRoleid());
                    involvement = eminvolvement.persist(involvement);
                    if (involvement == null) {
                        action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het registreren van het actieve lidmaatschap {0}.", new Object[]{Involvement.INVOLVEMENT_NAME_ACTIVE_MEMBERSHIP}));
                        action.setStatus(Action.ACTIONSTATUS_ABORTED);
                        return action;
                    } else {
                        action.setMessage(MessageFormat.format("Het actieve lidmaatschap {0} is geregistreerd.", new Object[]{Involvement.INVOLVEMENT_NAME_ACTIVE_MEMBERSHIP}));
                        action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                        return action;
                    }
                }
            } else {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het registreren van het actieve lidmaatschap {0}.", new Object[]{Involvement.INVOLVEMENT_NAME_ACTIVE_MEMBERSHIP}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            return action;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action addNetwork(Role role, Network network) {
        Action action = new Action();
        if (role.getRoleid() == 0) {
            action.setMessage("Action addnetwork: Roleid is 0");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (network.getName() == null) {
            action.setMessage("Action addnetwork: Network name is null");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (network.getName().isEmpty()) {
            action.setMessage("Action addnetwork: Network name is empty");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (role.getEmail() == null) {
            action.setMessage(MessageFormat.format("Action addnetwork: Url object for role with roleid {0} is null", role.getRoleid()));
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (role.getEmail().getInternetAddress().isEmpty()) {
            action.setMessage(MessageFormat.format("Action addnetwork: Url object for role with roleid {0} is empty", role.getRoleid()));
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        try {
            EMNetwork emnetwork = new EMNetwork();
            java.net.URL urlconfignetwork = new java.net.URL(NetworkDef.URLCONFIGNETWORK);
            boolean networkadd = emnetwork.networkAdd(urlconfignetwork, network.getName(), role.getRoleid(), network.getSource());
            if (networkadd) {
                action.setMessage(MessageFormat.format("Persoon met lidnummer {0} is toegevoegd aan netwerk {1}.", new Object[]{role.getFormattedRoleid(), network.getName()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het toevoegen aan netwerk {0}.", new Object[]{network.getName()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action removeNetwork(Role role, Network network) {
        Action action = new Action();
        if (role.getRoleid() == 0) {
            action.setMessage("Roleid is 0");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (network.getName() == null) {
            action.setMessage("Network name is null");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (network.getName().isEmpty()) {
            action.setMessage("Network name is empty");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (role.getEmail() == null) {
            action.setMessage("Url object for role is null");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        if (role.getEmail().getInternetAddress().isEmpty()) {
            action.setMessage("Url object for role is empty");
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
        try {
            EMNetwork emnetwork = new EMNetwork();
            java.net.URL urlconfignetwork = new java.net.URL(NetworkDef.URLCONFIGNETWORK);
            boolean networkremove = emnetwork.networkEnd(urlconfignetwork, network.getName(), role.getRoleid());
            if (networkremove) {
                action.setMessage(MessageFormat.format("Persoon met lidnummer {0} is verwijderd uit netwerk {1}.", new Object[]{role.getFormattedRoleid(), network.getName()}));
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het verwijderen uit netwerk {0}.", new Object[]{network.getName()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    /*
     * private static Action partofNetwork(Role role, Network network) { Action
     * action = new Action(); if (role.getRoleid() == 0) {
     * action.setMessage("Roleid is 0");
     * action.setStatus(Action.ACTIONSTATUS_ABORTED); return action; } if
     * (network.getName() == null) { action.setMessage("Network name is null");
     * action.setStatus(Action.ACTIONSTATUS_ABORTED); return action; } if
     * (network.getName().isEmpty()) { action.setMessage("Network name is
     * empty"); action.setStatus(Action.ACTIONSTATUS_ABORTED); return action; }
     * if (role.getEmail() == null) { action.setMessage("Url object for role is
     * null"); action.setStatus(Action.ACTIONSTATUS_ABORTED); return action; }
     * if (role.getEmail().getInternetAddress().isEmpty()) {
     * action.setMessage("Url object for role is empty");
     * action.setStatus(Action.ACTIONSTATUS_ABORTED); return action; } try {
     * EMNetwork emnetwork = new EMNetwork(); java.net.URL urlconfignetwork =
     * new java.net.URL(NetworkDef.URLCONFIGNETWORK); boolean networkpartof =
     * emnetwork.networkPartof(urlconfignetwork, network.getName(),
     * role.getRoleid()); if (networkpartof) {
     * action.setMessage(MessageFormat.format("Persoon met lidnummer {0} maakt
     * reeds deel uit van netwerk {1}.", new Object[]{role.getFormattedRoleid(),
     * network.getName()})); action.setStatus(Action.ACTIONSTATUS_COMPLETED);
     * return action; } else { action.setMessage(MessageFormat.format("Persoon
     * met lidnummer {0} maakt nog geen deel uit van netwerk {1}.", new
     * Object[]{role.getFormattedRoleid(), network.getName()}));
     * action.setStatus(Action.ACTIONSTATUS_REQUESTED); return action; } } catch
     * (Exception e) {
     * Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE,
     * null, e); action.setMessage(e.getMessage());
     * action.setStatus(Action.ACTIONSTATUS_ABORTED); return action; } }
     *
     */
    private static Action addContact(Role role, Contact contact) {
        Action action = new Action();
        try {
            EMContact emcontact = new EMContact();
            contact.setRoleid(role.getRoleid());
            contact = emcontact.persist(contact);
            if (contact == null) {
                action.setMessage(MessageFormat.format("Fout bij het opslaan contactmoment {0} voor lidnummer {1}.", new Object[]{contact.getSubject(), role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            } else {
                action.setMessage(MessageFormat.format("Contactmoment {0} opgeslagen voor persoon met lidnummer {1}.", new Object[]{contact.getSubject(), role.getFormattedRoleid()}));
                action.setComment(contact.getContent());
                action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action addSubscription(Role role, Product product, Subscription subscription) {
        Action action = new Action();
        try {
            EMSubscription emsubscription = new EMSubscription();
            // Register subscription to quarterly periodical "KW00"
            if (product != null) {
                subscription = new Subscription(0, product.getSource(), subscription.getStartdate(), subscription.getEnddate(), 0, Channel.MEDIATYPE_EMAIL, 0, role.getRoleid(), product.getIdViaName());
                subscription = emsubscription.persist(subscription);
                if (subscription == null) {
                    action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het registreren van het abonnement op {0}.", new Object[]{product.getName()}));
                    action.setStatus(Action.ACTIONSTATUS_ABORTED);
                    return action;
                } else {
                    action.setMessage(MessageFormat.format("Abonnement op {0} is geregistreerd.", new Object[]{product.getName()}));
                    action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                    return action;
                }
            } else {
                action.setMessage(MessageFormat.format("Het product {0} bestaat niet of is ongeldig. Het abonnement is niet geregistreerd.", new Object[]{product.getName()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, null, e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action changeDateofbirth(Role role) {
        Action action = new Action();
        try {
            EMPerson emperson = new EMPerson();
            if (role.getPerson() == null) {
                action.setMessage(MessageFormat.format("Persoon met lidnummer {0} heeft geen geldig persoonsrecord.", new Object[]{role.getFormattedRoleid()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
            if (role.hasDateofbirth()) {
                Person updatedperson = emperson.roleUpdateBirth(role.getPerson());
                if (updatedperson == null) {
                    action.setMessage(MessageFormat.format("Er is een fout opgetreden bij het registreren van de geboortedatum voor persoon met lidnummer {0} (geboortedatum {1}).", new Object[]{role.getFormattedRoleid(), role.getPerson().getFormattedBirth()}));
                    action.setStatus(Action.ACTIONSTATUS_ABORTED);
                    return action;
                } else {
                    action.setMessage(MessageFormat.format("Geboortedatum is geregistreerd voor persoon met lidnummer {0} (geboortedatum {1}).", new Object[]{role.getFormattedRoleid(), role.getPerson().getFormattedBirth()}));
                    action.setStatus(Action.ACTIONSTATUS_COMPLETED);
                    return action;
                }
            } else {
                action.setMessage(MessageFormat.format("Geboortedatum ontbreekt of is ongeldig (datum {0}), registreren van geboortedatum kan niet worden uitgevoerd.", new Object[]{role.getPerson().getFormattedBirth()}));
                action.setStatus(Action.ACTIONSTATUS_ABORTED);
                return action;
            }
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    private static Action sendResponse() {
        Action action = new Action();
        char[] buffer = new char[1024];
        Writer writer = new StringWriter();
        try {
            //TODO: This is just a stub... get Response object from WebformDef, get HTML via URL, send it...
            EMRole emrole = new EMRole();
            java.net.URL url = new java.net.URL("http://www.amnesty.nl");
            InputStream inputstream = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
            while (br.read(buffer) != -1) {
                writer.write(buffer);
            }
            List<String> recipientlist = new ArrayList();
            recipientlist.add("e.vanvelzen@amnesty.nl");
            String text = writer.toString();
            String subject = "Test";
            MessageController.sendEmail(recipientlist, subject, text);
            return action;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            action.setMessage(e.getMessage());
            action.setStatus(Action.ACTIONSTATUS_ABORTED);
            return action;
        }
    }

    /**
     * Gets the form values and stores them in a properties list
     *
     * @param form
     * @return
     */
    private static Properties getWebformvalues(Form form) {
        Properties webformvalues = new Properties();
        try {
            for (Property property : form.getPropertylist()) {
                if (property.getValue() == null) {
                    webformvalues.setProperty(property.getKey(), "");

                    // DEBUG
                    //Logger.getLogger(CRMWebformController.class.getName()).log(Level.INFO, "Properties: webform key {0} value null", new Object[]{property.getKey()});

                } else {
                    // Prevent SQL injection
                    String value = property.getValue();
                    if (value != null) {
                        value = value.replace(";", "").replace("&", "").replace("%", "");
                    }
                    webformvalues.setProperty(property.getKey(), value);

                    // DEBUG
                    //Logger.getLogger(CRMWebformController.class.getName()).log(Level.INFO, "Properties: webform key {0} value {1}", new Object[]{property.getKey(), property.getValue()});

                }
            }
            return webformvalues;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return webformvalues;
        }
    }

    /**
     * Reading the values for the CRM objects is done by processing the webform
     * field values given through the webformvalues properties array against the
     * webform definition. The webform definition is read from the Formdef
     * configuration file which is kept online and is modified each time a new
     * webform comes into existence or a existing webform evolves. The
     * attributelinklist is built from the webform definition and links a
     * webform fieldname to the name of a CRM object for instance the fieldname
     * "city" to the CRM object "address_city". The attributelinklist is part of
     * the mapping object that describes the mapping between the webform fields
     * and the CRM objects.
     *
     * The CRM object values are retrieved by traversing the CRM object names as
     * defined in the Formdef configuration file and consequently retrieving the
     * corresponding webform value from the webformvalues array through the
     * mapping.getFormdefcrmobjectValue() method.
     *
     * @param webformdef
     * @param webformvalues
     * @return
     */
    private static Properties getCRMobjectvalues(WebformDef webformdef, Properties webformvalues) {
        Properties formdefvalues = new Properties();
        try {
            Mapping mapping = webformdef.getMapping();
            List<Attributelink> attributelinklist = mapping.getAttributelinklist();
            for (Attributelink attributelink : attributelinklist) {
                // The formdefkey points to the CRM object that receives a form field value
                String formdefkey = attributelink.getFormdefcrmobject();
                String formdefvalue = mapping.getFormdefcrmobjectValue(webformdef, formdefkey, webformvalues);
                formdefvalues.setProperty(formdefkey, formdefvalue);

                //DEBUG
                //Logger.getLogger(CRMWebformController.class.getName()).log(Level.INFO, "Properties: target key {0} value {1}", new Object[]{formdefkey, formdefvalue});

            }
            return formdefvalues;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return formdefvalues;
        }
    }

    private static WebformDef getWebformdef(Form form) {
        try {
            FormHTTP formhttp = new FormHTTP();
            java.net.URL urlconfigwebform = new java.net.URL(WebformDef.URLCONFIGWEBFORM);
            return formhttp.getWebformDef(urlconfigwebform, (int) form.getId());
        } catch (WEBFORMWebformException wfe) {
            List<Action> actionlist = new ArrayList();
            for (Property property : form.getPropertylist()) {
                Action action = new Action();
                action.setMessage("Key [" + property.getKey() + "] value [" + property.getValue() + "]");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                actionlist.add(action);
            }
            List<String> recipientlist = new ArrayList();
            recipientlist.add(NOTIFICATION_ESCALATE);
            String subject = "Webform ERROR: Unsupported form (node is " + form.getId() + ")";
            String roledetailsold = "";
            String roledetailsnew = "";
            Notification.send(recipientlist, subject, 0, actionlist, roledetailsold, roledetailsnew);
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, wfe.getMessage(), wfe);
            return null;
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    private static String formatSubject(WebformDef webformdef) {
        try {
            String nodevalue = String.valueOf(webformdef.getNode());
            if (nodevalue.length() < 5) {
                nodevalue = "00000".substring(nodevalue.length()).concat(nodevalue);
            }
            return "Node: ".concat(nodevalue).concat(" - webform: ").concat(webformdef.getName());
        } catch (Exception e) {
            Logger.getLogger(CRMWebformController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    private static void meldingOntbrekendeVeldverwerking(Form form, WebformDef webformdef, List<Action> actionlist, String roledetailsold, String roledetailsnew) {
        Action action;
        //Boolean eengevonden = false;
        Boolean dezegevonden = false;
        Mapping mapping = webformdef.getMapping();
        List<Attributelink> attributelinklist = mapping.getAttributelinklist();

        for (Property property : form.getPropertylist()) {
            dezegevonden = false;
            if (property.getKey().equals("networkname")) {  // gereserveerd
                dezegevonden = true;
            } else {
                for (Attributelink attributelink : attributelinklist) {
                    if (attributelink.getFormdeffieldname().equals(property.getKey())) {
                        dezegevonden = true;
                    }
                }
            }
            if (!dezegevonden) {
                action = new Action();
                action.setMessage("Key [" + property.getKey() + "] value [" + property.getValue() + "] | Mislukt | Geen mapping gespecificeerd ");
                action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
                actionlist.add(action);
                //eengevonden = true;
            }
        }
        // Versturen hoeft niet, want het komt bovenaan de normale versturing te staan.
        //if (eengevonden) {
        //  action = new Action();
        //action.setMessage("Node: " + form.getId());
        //action.setStatus(Action.ACTIONSTATUS_NO_ACTION);
        //actionlist.add(action);

        //List<String> recipientlist = new ArrayList();
        //recipientlist.add(NOTIFICATION_ESCALATE);
        //String subject = "Webform MISSING DEFINITION";
        //Notification.send(recipientlist, subject, 0, actionlist, roledetailsold, roledetailsnew);
        //}
    }

    private static void markeerSpecialeFormulieren(Form form) {
        /* Speciale afspraak voor petitielijsten. Als daar de property Formuliersoort in staat, en de waarde is Petitie
         * dan wordt het automatisch herkend. Om in de pas te lopen met het bestaande schema wordt de id van het formulier naar 1 gezet.
         */
        if (form.getPropertylist()==null) {
            return;
        }
        for (Property property : form.getPropertylist()) {
            if (property.getKey().toUpperCase().equals("FORMULIERSOORT")) {
                if (property.getValue()!=null) {
                    if (property.getValue().toUpperCase().equals("PETITIE")) {
                        form.setId(1);
                    }
                    if (property.getValue().toUpperCase().equals("EMAIL")) {
                        form.setId(2);
                    }
                }
            } else if (property.getKey().toUpperCase().equals("FREQUENCY") &&
                    property.getValue().toUpperCase().equals("ONETIME") &&
                    form.getId()==500) {
                // Dit is niet fraai, maar wel snel gerealiseerd. Een aanpassing op het word lid formulier maakt eenmalige incasso
                // mogelijk. daar hoort geen lidmaatschap bij (of donateurschap). De switch wordt gemaakt naar een virtueel formulier
                // dat eenmalig gift via incasso afschrijft.
                form.setId(501);
            }
            else if (property.getKey().toUpperCase().equals("METHOD") &&
                    property.getValue().toUpperCase().equals("IDEAL") &&
                    (form.getId()==500 || form.getId()==501)) {
                // Dit is niet fraai, maar wel snel gerealiseerd. Een aanpassing op het word lid formulier maakt ideal betaling mogelijk.
                // Op dit moment is niet duidelijk of de ideal betaling gelukt is. Met de id wordt dat teruggezocht op de site van buckaroo
                // joepie.
                form.setId(502);
            }
        }
    }
    
     static public long getFirstNumber(String numberString) {
        long parsedNo = 0;
        for (int n = 0; n < numberString.length(); n++) {
            String i = numberString.substring(n, n + 1);
            if (i.equals("1") || i.equals("2") || i.equals("3") || i.equals("4") || i.equals("5")
                    || i.equals("6") || i.equals("7") || i.equals("8") || i.equals("9") || i.equals("0")) {
                parsedNo = parsedNo * 10 + Integer.parseInt(i);
            } else {
                if (parsedNo > 0) {
                    return parsedNo;
                }
            }
        }
        return parsedNo;
    }

    private static void bepaalEinddatum(Subscription subscription) {
        // periode 27 januari t/m 11 april -> 105 dagen durend
        // periode 12 april t/m 26 augustus -> 135 dagen durend (zomerpauze)
        // periode 27 augustus t/m 11 september -> 105 dagen durend
        // periode 12 september t/m 26 januari -> 135 dagen durend (winderpauze)
        long currentTimeMillis = System.currentTimeMillis();
        GregorianCalendar gc=new GregorianCalendar();
        gc.setTimeInMillis(currentTimeMillis);
        int month = gc.get(GregorianCalendar.MONTH);
        int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
        int increment=135;
        if (month==1 || month==8) {
            if (day>=27) {
                increment=105;
            }
        }
        if (month==2 || month==3) {
            increment=105;
        }
        if (month==4 || month==9) {
            if (day<11) {
                increment=105;
            }
        }
        gc.add(GregorianCalendar.DAY_OF_MONTH, increment);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, increment); //minus number would decrement the days
        subscription.setEnddate(cal.getTime());
    }
}
