/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import nl.amnesty.crm.collection.IdStartdateEnddate;
import nl.amnesty.crm.entity.Address;
import nl.amnesty.crm.entity.Person;
import nl.amnesty.crm.entity.Role;
import nl.amnesty.crm.entity.URL;
import nl.amnesty.crm.persistence.EMRole;
import nl.amnesty.crm.persistence.EntityManager;
import nl.amnesty.crm.sql.RoleSQL;
import nl.amnesty.ldap.entity.LDAP;
import nl.amnesty.ldap.entity.LDAPConnection;
import nl.amnesty.ldap.group.controller.LDAPGroupController;
import nl.amnesty.ldap.group.entity.LDAPgroupOfUniqueNames;
import nl.amnesty.ldap.person.controller.LDAPPersonController;
import nl.amnesty.ldap.person.entity.LDAPinetOrgPerson;
import nl.amnesty.sys.ldap.controller.LDAPController;
import nl.amnesty.sys.sync.Delta;
import nl.amnesty.sys.sync.Replicator;
import org.apache.log4j.Logger;

/**
 *
 * @author evelzen
 */
public class CRMLDAPController {

    public static LDAPinetOrgPerson createLoginaccount(Role role, String password) {
        LDAP ldap = null;
        LDAPConnection ldapgenericconnection = null;
        try {
            // Open a connection to the LDAP server
            ldap = new LDAP(LDAP.LDAP_TYPE_OPENDS, LDAP.HOSTNAME_GARNAAL, LDAP.PORTNUMBER_GARNAAL, LDAP.BASEDN_GARNAAL, LDAP.BINDDN_GARNAAL, LDAP.BINDPASSWORD_GARNAAL);
            ldapgenericconnection = ldap.open();
            if (ldapgenericconnection == null) {
                java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.SEVERE, "Unable to connect to LDAP server " + LDAP.HOSTNAME_GARNAAL + ".");
                return null;
            }
            // Perform generic LDAP update action, including creation of new LDAP entry if needed
            return doLoginaccount(ldap, ldapgenericconnection, role, password, true);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    public static LDAPinetOrgPerson updateLoginaccount(Role role, String password) {
        LDAP ldap = null;
        LDAPConnection ldapgenericconnection = null;
        try {
            // Open a connection to the LDAP server
            ldap = new LDAP(LDAP.LDAP_TYPE_OPENDS, LDAP.HOSTNAME_GARNAAL, LDAP.PORTNUMBER_GARNAAL, LDAP.BASEDN_GARNAAL, LDAP.BINDDN_GARNAAL, LDAP.BINDPASSWORD_GARNAAL);
            ldapgenericconnection = ldap.open();
            if (ldapgenericconnection == null) {
                java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.SEVERE, "Unable to connect to LDAP server " + LDAP.HOSTNAME_GARNAAL + ".");
                return null;
            }
            // Perform generic LDAP update action, but do not create new LDAP entry
            return doLoginaccount(ldap, ldapgenericconnection, role, password, false);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    public static LDAPinetOrgPerson doLoginaccount(LDAP ldap, LDAPConnection ldapgenericconnection, Role role, String password, boolean create) {
        String mode = "";
        if (create) {
            mode = "create";
        } else {
            mode = "update";
        }
        if (password == null) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.WARNING, "LDAP: Attempt to {0} LDAPinetOrgPerson with null password", mode);
            return null;
        }
        if (password.isEmpty()) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.WARNING, "LDAP: Attempt to {0} LDAPinetOrgPerson with empty password", mode);
            return null;
        }
        if (role == null) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.WARNING, "LDAP: Attempt to {0} LDAPinetOrgPerson from null role object", mode);
            return null;
        }
        Person person = role.getPerson();
        Address address = role.getAddress();
        if (person == null) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.WARNING, "LDAP: Attempt to {0} LDAPinetOrgPerson from null person object", mode);
            return null;
        }
        if (address == null) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.WARNING, "LDAP: Attempt to {0} LDAPinetOrgPerson from null address object", mode);
            return null;
        }
        if (role.getEmail().getInternetAddress().isEmpty()) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.WARNING, "LDAP: Attempt to {0} LDAPinetOrgPerson from empty emailaddress object", mode);
            return null;
        }
        try {
            LDAPinetOrgPerson ldapinetorgperson = LDAPPersonController.read(ldapgenericconnection, ldap.getBasedn(), role.getEmail().getInternetAddress());
            if (ldapinetorgperson == null) {
                // No existing LDAP entry found
                if (create) {
                    ldapinetorgperson = LDAPController.constructLDAPinetOrgPerson(ldapgenericconnection, role);
                    if (ldapinetorgperson != null) {
                        LDAPPersonController.create(ldapgenericconnection, ldap.getBasedn(), ldapinetorgperson);
                        LDAPPersonController.setPassword(ldapgenericconnection, ldap.getBasedn(), ldapinetorgperson, password);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                // Update existing LDAP entry with value from Role object
                ldapinetorgperson = LDAPController.setLDAPinetOrgPerson(role, false);
                LDAPPersonController.update(ldapgenericconnection, ldap.getBasedn(), ldapinetorgperson);
                LDAPPersonController.setPassword(ldapgenericconnection, ldap.getBasedn(), ldapinetorgperson, password);
            }
            return ldapinetorgperson;
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(CRMLDAPController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public static void replicateLDAPGroupCollection(Logger logger, Connection connection, LDAPConnection ldapconnection, String basedn, String ldapgroupname, Collection<IdStartdateEnddate> rolecollection) {
        Collection<IdStartdateEnddate> ldapcollection = null;
        Delta delta = new Delta();
        try {
            ldapcollection = transformLDAPGroupToCollection(logger, ldapconnection, basedn, ldapgroupname);

            // DEBUG
            //System.out.println("--- ".concat(ldapgroupname).concat(" ---"));
            //for (IdStartdateEnddate element : ldapcollection) {
            //    System.out.println("id: ".concat(element.getId()));
            //}
            //System.out.println("");
            //System.out.println("");

            delta = Replicator.replicate(delta, rolecollection, ldapcollection);
            int count_i = 0;
            for (IdStartdateEnddate element : delta.getIncrease_collection_b()) {
                count_i = count_i + 1;
                // Role not found in LDAP group: role should be added to LDAP group

                // DEBUG
                //System.out.println("delta.getIncrease_collection_b() id: " + element.getId());

                addRoleToLDAPGroup(logger, connection, ldapconnection, basedn, ldapgroupname, element.getId());
            }
            int count_d = 0;
            /*
             * There may be entries in de LDAP group that are not part of the
             * CRM collection. Normally this would indicate that the CRM
             * collection would have to be increased with the entries found in
             * LDAP but not in CRM. But as CRM is leading we will take another
             * approach. The entries that would have been added to CRM will no
             * be removed from LDAP. In this way LDAP and CRM will be balanced.
             * Instead of using delta.getDecrease_collection_b() will will use
             * delta.getIncrease_collection_a() to decrease the LDAP group.
             */
            //for (IdStartdateEnddate element : delta.getDecrease_collection_b()) {
            for (IdStartdateEnddate element : delta.getIncrease_collection_a()) {
                count_d = count_d + 1;
                // LDAP person not found in role list: LDAP person should be removed from LDAP group

                // DEBUG
                //System.out.println("delta.getDecrease_collection_b() id: " + element.getId());
                //System.out.println("delta.getIncrease_collection_a() id: " + element.getId());

                removeRoleFromLDAPGroup(logger, ldapconnection, basedn, ldapgroupname, element.getId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ldapcollection = null;
            delta = null;
        }
    }

    private static void addRoleToLDAPGroup(Logger logger, Connection connection, LDAPConnection ldapconnection, String basedn, String ldapgroupname, String uid) {
        LDAPinetOrgPerson ldapinetorgperson = new LDAPinetOrgPerson();
        LDAPgroupOfUniqueNames ldapgroupofuniquenames = new LDAPgroupOfUniqueNames();
        Role role = new Role();
        try {
            // Check if person is already listed in LDAP directory
            ldapinetorgperson = LDAPPersonController.read(ldapconnection, basedn, uid);
            if (ldapinetorgperson == null) {
                // Person needs to be added to the LDAP directory first before it can be added to the LDAP group
                RoleSQL rolesql = new RoleSQL();
                if (ldapconnection.getLdapconnectionopends() != null) {
                    role = rolesql.readViaEmail(connection, uid);
                }
                if (ldapconnection.getLdapconnectionsunone() != null) {
                    role = rolesql.readViaEmail(connection, uid);
                }
                if (role != null) {
                    // The new Garnaal LDAP server allows for accented characters in person's names
                    if (ldapconnection.getLdapconnectionopends() != null) {
                        ldapinetorgperson = LDAPController.constructLDAPinetOrgPerson(ldapconnection, role);
                    }
                    // The old Lof LDAP server does not allow for accented characters in person's names
                    if (ldapconnection.getLdapconnectionsunone() != null) {
                        ldapinetorgperson = LDAPController.constructLDAPinetOrgPersonUnaccent(ldapconnection, role);
                    }
                        if (ldapinetorgperson != null) {
                        // Add person to LDAP directory
                        LDAPPersonController.create(ldapconnection, basedn, ldapinetorgperson);
                        // For the old Lof LDAP server, the password will be set during create, Garnaal needs another pass...
                        if (ldapconnection.getLdapconnectionopends() != null) {
                            // Set the password
                            String password = ldapinetorgperson.getPostalcode();
                            LDAPPersonController.setPassword(ldapconnection, basedn, ldapinetorgperson, password);
                        }
                    } else {

                        // DEBUG
                        //System.out.println("addRoleToLDAPGroup(): ldapinetorgperson is null");

                        return;
                    }
                } else {

                    // DEBUG
                    //System.out.println("addRoleToLDAPGroup(): role is null");

                    return;
                }
            }
            // Check if LDAP group is already listed in LDAP directory
            ldapgroupofuniquenames = LDAPGroupController.read(logger, ldapconnection, basedn, ldapgroupname);
            if (ldapgroupofuniquenames == null) {
                // LDAP group not yet present in LDAP directory, needs to be added first before person can be added to the group
                ldapgroupofuniquenames = new LDAPgroupOfUniqueNames();
                ldapgroupofuniquenames.setCn(ldapgroupname);
                ldapgroupofuniquenames.setDescription("LDAP CRM group");
                LDAPGroupController.create(logger, ldapconnection, basedn, ldapgroupofuniquenames);
            }
            // Now add LDAP person to LDAP group
            LDAPGroupController.addMember(logger, ldapconnection, basedn, ldapgroupofuniquenames, ldapinetorgperson);
            // Log the addition
            logger.info("Person " + ldapinetorgperson.getUid() + " was added to LDAP group " + ldapgroupname + ".");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ldapinetorgperson = null;
            ldapgroupofuniquenames = null;
            role = null;
        }
    }

    public static void addAdminToLDAPGroup(Logger logger, Connection connection, LDAPConnection ldapconnection, String basedn, String ldapgroupname) {
        LDAPgroupOfUniqueNames ldapgroupofuniquenames = new LDAPgroupOfUniqueNames();
        try {
            LDAPinetOrgPerson ldapinetorgperson = LDAPController.constructLDAPAdmin(logger, ldapconnection);
            String uid = ldapinetorgperson.getUid();
            // Check if person is already listed in LDAP directory
            LDAPinetOrgPerson ldapinetorgpersonfound = LDAPPersonController.read(ldapconnection, basedn, uid);
            if (ldapinetorgpersonfound == null) {
                // Person needs to be added to the LDAP directory first before it can be added to the LDAP group
                // Add person to LDAP directory
                LDAPPersonController.create(ldapconnection, basedn, ldapinetorgperson);
                // Set the password
                String password = ldapinetorgperson.getPostalcode();
                LDAPPersonController.setPassword(ldapconnection, basedn, ldapinetorgperson, password);
            }
            // Check if LDAP group is already listed in LDAP directory
            ldapgroupofuniquenames = LDAPGroupController.read(logger, ldapconnection, basedn, ldapgroupname);
            if (ldapgroupofuniquenames == null) {
                // LDAP group not yet present in LDAP directory, needs to be added first before person can be added to the group
                ldapgroupofuniquenames = new LDAPgroupOfUniqueNames();
                ldapgroupofuniquenames.setCn(ldapgroupname);
                ldapgroupofuniquenames.setDescription("LDAP CRM group");
                LDAPGroupController.create(logger, ldapconnection, basedn, ldapgroupofuniquenames);
            } else {
                // Check of admin user already part of LDAP group
                if (!ldapgroupofuniquenames.isUidPartOf(uid)) {
                    // Now add LDAP person to LDAP group
                    LDAPGroupController.addMember(logger, ldapconnection, basedn, ldapgroupofuniquenames, ldapinetorgperson);
                    // Log the addition
                    //logger.info("Person " + ldapinetorgperson.getUid() + " was added to LDAP group " + ldapgroupname + ".");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ldapgroupofuniquenames = null;
        }
    }

    /*
     * private static void addRoleToLDAP(Logger logger, Connection connection,
     * LDAPConnection ldapconnection, String basedn, String uid) {
     * LDAPinetOrgPerson ldapinetorgperson = new LDAPinetOrgPerson(); Role role
     * = new Role(); try { // Check if person is already listed in LDAP
     * directory ldapinetorgperson = LDAPPersonController.read(ldapconnection,
     * basedn, uid); if (ldapinetorgperson == null) { // Person needs to be
     * added to the LDAP directory first before it can be added to the LDAP
     * group role = RoleController.readViaEmail(connection, uid); if (role !=
     * null) { // The new Garnaal LDAP server allows for accented characters in
     * person's names if (ldapconnection.getLdapconnectionopends() != null) {
     * ldapinetorgperson = LDAPController.constructLDAPinetOrgPerson(logger,
     * ldapconnection, role); } // The old Lof LDAP server does not allow for
     * accented characters in person's names if
     * (ldapconnection.getLdapconnectionsunone() != null) { ldapinetorgperson =
     * LDAPController.constructLDAPinetOrgPersonUnaccent(logger, ldapconnection,
     * role); } if (ldapinetorgperson != null) { // Add person to LDAP directory
     * LDAPPersonController.create(ldapconnection, basedn, ldapinetorgperson);
     * // Set the password String password = ldapinetorgperson.getPostalcode();
     * LDAPPersonController.setPassword(ldapconnection, basedn,
     * ldapinetorgperson, password); // Log the addition logger.info("Person " +
     * ldapinetorgperson.getUid() + " was added to the LDAP directory."); } else
     * { return; } } else { return; } } } catch (Exception e) {
     * logger.error(e.getMessage(), e); } finally { ldapinetorgperson = null;
     * role = null; } }
     *
     */
    private static void removeRoleFromLDAPGroup(Logger logger, LDAPConnection ldapconnection, String basedn, String ldapgroupname, String uid) {
        LDAPinetOrgPerson ldapinetorgperson = new LDAPinetOrgPerson();
        LDAPgroupOfUniqueNames ldapgroupofuniquenames = new LDAPgroupOfUniqueNames();
        try {
            // Check if person is listed in LDAP directory
            ldapinetorgperson = LDAPPersonController.read(ldapconnection, basedn, uid);
            if (ldapinetorgperson != null) {
                // Check if LDAP group is listed in LDAP directory
                ldapgroupofuniquenames = LDAPGroupController.read(logger, ldapconnection, basedn, ldapgroupname);
                if (ldapgroupofuniquenames != null) {
                    // Now remove LDAP person from LDAP group
                    LDAPGroupController.removeMember(logger, ldapconnection, basedn, ldapgroupofuniquenames, ldapinetorgperson);
                    // Log the removal
                    logger.info("Person " + ldapinetorgperson.getUid() + " was removed from LDAP group " + ldapgroupname + ".");
                } else {
                    return;
                }
            } else {
                return;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ldapinetorgperson = null;
            ldapgroupofuniquenames = null;
        }
    }

    /*
     * private static void doKill(Logger logger, Connection connection,
     * LDAPConnection ldapconnection, String basedn, List<Long> roleidlist) {
     * int roleidcount = 0; boolean status = false; int count = 0; try { for
     * (long roleid : roleidlist) { roleidcount++;
     *
     * Role role = RoleController.read(connection, roleid);
     *
     * // Contruct LDAPinetOrgPerson object LDAPinetOrgPerson ldapinetorgperson
     * = LDAPController.constructLDAPinetOrgPerson(logger, ldapconnection,
     * role);
     *
     * // LDAPinetOrgPerson will be null if person does not have a valid e-mail
     * address specified. if (ldapinetorgperson != null) {
     * LDAPPersonController.delete(ldapconnection, basedn, ldapinetorgperson);
     * count = count + 1; // Log the removal logger.info("Person " +
     * ldapinetorgperson.getUid() + " (" + count + " of " + roleidlist.size() +
     * ") was removed from LDAP."); } } } catch (Exception e) {
     * logger.error(e.getMessage(), e); } }
     *
     */

    /*
     * private static List<String> transformLDAPGroupToUidlist(Logger logger,
     * LDAPConnection ldapconnection, String basedn, String ldapgroupname) {
     * List<String> uidlist = new ArrayList(); LDAPgroupOfUniqueNames
     * ldapgroupofuniquenames = new LDAPgroupOfUniqueNames(); try {
     * ldapgroupofuniquenames = LDAPGroupController.read(logger, ldapconnection,
     * basedn, ldapgroupname); if (ldapgroupofuniquenames != null) { if
     * (ldapgroupofuniquenames.getLdapinetorgpersonlist() != null) { for
     * (LDAPinetOrgPerson ldapinetorgperson :
     * ldapgroupofuniquenames.getLdapinetorgpersonlist()) { if
     * (ldapconnection.getLdapconnectionopends() != null) { // The new Garnaal
     * LDAP server uses the e-mail address as uid but we need to het the roleid
     * from the employeenumber field
     * uidlist.add(ldapinetorgperson.getEmployeenumber()); } if
     * (ldapconnection.getLdapconnectionsunone() != null) { // The old Lof LDAP
     * server uses roleid as uid and that is exactly what we need
     * uidlist.add(ldapinetorgperson.getUid()); } } } } return uidlist; } catch
     * (Exception e) { logger.error(e.getMessage(), e); return uidlist; }
     * finally { uidlist = null; ldapgroupofuniquenames = null; } }
     *
     */
    private static Collection<IdStartdateEnddate> transformLDAPGroupToCollection(Logger logger, LDAPConnection ldapconnection, String basedn, String ldapgroupname) {
        Collection<IdStartdateEnddate> collection = new ArrayList();
        LDAPgroupOfUniqueNames ldapgroupofuniquenames = new LDAPgroupOfUniqueNames();
        long roleid = 0;
        String uid = "";
        try {
            ldapgroupofuniquenames = LDAPGroupController.read(logger, ldapconnection, basedn, ldapgroupname);
            if (ldapgroupofuniquenames != null) {
                if (ldapgroupofuniquenames.getLdapinetorgpersonlist() != null) {
                    for (LDAPinetOrgPerson ldapinetorgperson : ldapgroupofuniquenames.getLdapinetorgpersonlist()) {
                        // Exclude the LDAP admin user
                        if (!LDAPinetOrgPerson.isLDAPAdmin(ldapinetorgperson)) {
                            Calendar calendar = Calendar.getInstance();
                            // Assume LDAP entries have been there for some time...
                            calendar.set(1990, Calendar.JANUARY, 1);
                            IdStartdateEnddate elementtimeperiod = null;

                            if (ldapconnection.getLdapconnectionopends() != null) {
                                uid = ldapinetorgperson.getUid();
                                if (uid != null) {
                                    elementtimeperiod = new IdStartdateEnddate(roleid, uid.toLowerCase(), calendar.getTime(), null);
                                }
                            }
                            if (ldapconnection.getLdapconnectionsunone() != null) {
                                uid = ldapinetorgperson.getUid();
                                if (uid != null) {
                                    elementtimeperiod = new IdStartdateEnddate(roleid, uid.toLowerCase(), calendar.getTime(), null);
                                }
                            }

                            if (elementtimeperiod != null) {
                                collection.add(elementtimeperiod);

                                // DEBUG
                                //System.out.println("element id: " + elementtimeperiod.getId());
                            }
                        }
                    }
                }
            }
            return collection;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return collection;
        } finally {
            collection = null;
            ldapgroupofuniquenames = null;
        }
    }

    /*
     * private static List<String> transformRoleidlistToUidlist(Logger logger,
     * Connection connection, LDAPConnection ldapconnection, List<Long>
     * roleidlist) { List<String> uidlist = new ArrayList(); RoleController
     * rolecontroller = new RoleController(); Role role = new Role(); String
     * mail = ""; try { for (long roleid : roleidlist) { role =
     * rolecontroller.read(connection, roleid); if
     * (ldapconnection.getLdapconnectionopends() != null) { // The new Garnaal
     * LDAP server uses the e-mail address as uid but we need to het the roleid
     * from the employeenumber field if (role.getUrllist() != null) { List<URL>
     * urllist = role.getUrllist(); if (urllist.size() > 0) { URL url =
     * urllist.get(0); mail =
     * url.getUsername().concat("@").concat(url.getDomain()).toLowerCase().trim();
     * uidlist.add(mail); } } } if (ldapconnection.getLdapconnectionsunone() !=
     * null) { // The old Lof LDAP server uses roleid as uid and that is exactly
     * what we need uidlist.add(String.valueOf(role.getRoleid())); } } return
     * uidlist; } catch (Exception e) { logger.error(e.getMessage(), e); return
     * uidlist; } finally { uidlist = null; rolecontroller = null; role = null;
     * } }
     *
     */
    private static Collection<IdStartdateEnddate> transformRoleidlistToCollection(Logger logger, Connection connection, LDAPConnection ldapconnection, List<Long> roleidlist) {
        Collection<IdStartdateEnddate> collection = new ArrayList();
        Role role = new Role();
        String mail = "";
        try {
            EntityManager em = new EMRole();

            for (long roleid : roleidlist) {
                role = em.find(roleid);
                if (ldapconnection.getLdapconnectionopends() != null) {
                    // The new Garnaal LDAP server uses the e-mail address as uid
                    if (role.getUrllist() != null) {
                        List<URL> urllist = role.getUrllist();
                        if (urllist.size() > 0) {
                            URL url = urllist.get(0);
                            mail = url.getUsername().concat("@").concat(url.getDomain()).toLowerCase().trim();
                            Calendar calendar = Calendar.getInstance();
                            IdStartdateEnddate elementtimeperiod = new IdStartdateEnddate(roleid, mail, calendar.getTime(), null);
                            collection.add(elementtimeperiod);
                        }
                    }
                }
                if (ldapconnection.getLdapconnectionsunone() != null) {
                    // The old Lof LDAP server uses roleid as uid
                    Calendar calendar = Calendar.getInstance();
                    IdStartdateEnddate elementtimeperiod = new IdStartdateEnddate(roleid, String.valueOf(roleid), calendar.getTime(), null);
                    collection.add(elementtimeperiod);
                }
            }
            return collection;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return collection;
        } finally {
            collection = null;
            role = null;
        }
    }
}
