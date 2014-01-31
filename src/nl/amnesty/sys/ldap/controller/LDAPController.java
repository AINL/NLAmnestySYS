/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.ldap.controller;

import java.util.logging.Level;
import nl.amnesty.crm.entity.Address;
import nl.amnesty.crm.entity.Person;
import nl.amnesty.crm.entity.Phone;
import nl.amnesty.crm.entity.Role;
import nl.amnesty.crm.entity.URL;
import nl.amnesty.ldap.entity.LDAPConnection;
import nl.amnesty.ldap.person.entity.LDAPinetOrgPerson;
import org.apache.log4j.Logger;

/**
 *
 * @author evelzen
 */
public class LDAPController {

    public static LDAPinetOrgPerson constructLDAPinetOrgPerson(LDAPConnection ldapgenericconnection, Role role) {
        return constructLDAPinetOrgPersonGeneric(ldapgenericconnection, role, false);
    }

    public static LDAPinetOrgPerson constructLDAPinetOrgPersonUnaccent(LDAPConnection ldapgenericconnection, Role role) {
        return constructLDAPinetOrgPersonGeneric(ldapgenericconnection, role, true);
    }

    public static LDAPinetOrgPerson constructLDAPAdmin(Logger logger, LDAPConnection ldapgenericconnection) {
        String mail = LDAPinetOrgPerson.LDAPADMIN_MAIL;
        try {
            LDAPinetOrgPerson ldapinetorgperson = new LDAPinetOrgPerson();

            ldapinetorgperson.setUserpassword(LDAPinetOrgPerson.LDAPADMIN_USERPASSWORD);

            ldapinetorgperson.setCn(LDAPinetOrgPerson.LDAPADMIN_CN);
            ldapinetorgperson.setDisplayname(LDAPinetOrgPerson.LDAPADMIN_CN);
            ldapinetorgperson.setEmployeenumber(LDAPinetOrgPerson.LDAPADMIN_EMPLOYEENUMBER);
            ldapinetorgperson.setGivenname(LDAPinetOrgPerson.LDAPADMIN_GIVENNAME);
            ldapinetorgperson.setInitials("");
            ldapinetorgperson.setMail(mail);
            ldapinetorgperson.setMobile("");
            ldapinetorgperson.setPostaladdress(LDAPinetOrgPerson.LDAPADMIN_POSTALADDRESS);
            ldapinetorgperson.setPostalcode(LDAPinetOrgPerson.LDAPADMIN_POSTALCODE);
            ldapinetorgperson.setSn(LDAPinetOrgPerson.LDAPADMIN_SN);
            ldapinetorgperson.setSt("");
            ldapinetorgperson.setTelephonenumber(LDAPinetOrgPerson.LDAPADMIN_TELEPHONENUMBER);
            ldapinetorgperson.setTitle("");

            // The new Garnaal LDAP server uses the e-mail address as the unique identifier uid
            if (ldapgenericconnection.getLdapconnectionopends() != null) {
                if (mail.length() > 0) {
                    ldapinetorgperson.setUid(mail);
                    return ldapinetorgperson;
                } else {
                    return null;
                }
            }
            // The old Lof LDAP server uses the pvkey CRM id as the unique identifier uid
            if (ldapgenericconnection.getLdapconnectionsunone() != null) {
                ldapinetorgperson.setUid(LDAPinetOrgPerson.LDAPADMIN_EMPLOYEENUMBER);
                return ldapinetorgperson;
            }

            // Return null if neither OpenDS nor SunONE
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private static LDAPinetOrgPerson constructLDAPinetOrgPersonGeneric(LDAPConnection ldapgenericconnection, Role role, boolean unaccent) {
        return setLDAPinetOrgPerson(role, unaccent);
    }

    public static LDAPinetOrgPerson setLDAPinetOrgPerson(Role role, boolean unaccent) {
        String displayname = "";
        String telephonenumber = "";
        String mail = "";
        String postalcode = "";
        String postaladdress = "";
        try {
            Person person = role.getPerson();
            Address address = role.getAddress();

            if (unaccent) {
                
                displayname = person.getFormattedNameUnaccent();
            } else {
                displayname = person.getFormattedName();
            }
            postaladdress = address.getFormatedAddress();
            postalcode = String.valueOf(address.getPostalcodenumeric()).concat(address.getPostalcodealpha().toUpperCase());

            Phone phone = role.getPhone();
            if (phone != null) {
                telephonenumber = phone.getFormattedNumber();
            }
            URL url = role.getEmail();
            if (url != null) {
                mail = url.getInternetAddress();
            }

            LDAPinetOrgPerson ldapinetorgperson = new LDAPinetOrgPerson();

            ldapinetorgperson.setUserpassword(postalcode);

            ldapinetorgperson.setCn(displayname);
            ldapinetorgperson.setDisplayname(displayname);
            ldapinetorgperson.setEmployeenumber(String.valueOf(role.getRoleid()));
            ldapinetorgperson.setGivenname(person.getForenames());
            ldapinetorgperson.setInitials(person.getInitials());
            ldapinetorgperson.setMail(mail);
            ldapinetorgperson.setMobile("");
            ldapinetorgperson.setPostaladdress(postaladdress);
            ldapinetorgperson.setPostalcode(postalcode);
            ldapinetorgperson.setSn(person.getSurname());
            ldapinetorgperson.setSt(address.getState());
            ldapinetorgperson.setTelephonenumber(telephonenumber);
            ldapinetorgperson.setTitle(person.getTitle());
            ldapinetorgperson.setUid(mail);

            return ldapinetorgperson;
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(LDAPController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
}
