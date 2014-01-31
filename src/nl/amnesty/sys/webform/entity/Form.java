/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.webform.entity;

import java.util.List;
import java.util.Properties;

/**
 *
 * @author evelzen
 */
public class Form {

    /*
491	Aanmelden start uw eigen project
435	Aanmelden als collectant
7436	Win kaartjes voor een concert van Dotan!
7591	Amnesty is hard op zoek naar collecte-organisatoren 
5749	Wijzigingsformulier voor verhuizingen, tijdschriften en e-mail 
5810	Afmelden actiemailings 
5756	Aanmelden persdienst
441	Collectecoördinator 
383	Contact webredactie 
385	Contact DFG 
436	Gift via machtiging
444	Notarieel schenken 
4816    -
5821	Aanmelden als flex-vrijwilliger 
5808	klachtenformulier
5809	Aanvragen van folders 
439	Geef om vrijheid-pakket 
5100	Meld u aan voor het Rapid response action team 
5748	Wordt Vervolgd eerst op proef? 
5620	Wordt Vervolgd, voor vrije geesten
	Contact  
6740	Bedankt voor uw steun! 
5745	Aanmelden/ afmelden e-nieuws 
6021	Afmelden voor Acceptemails 
5625	Geef een abonnement op Wordt Vervolgd cadeau 
5757	Vraag een actiepakket aan 
7702	9 december: Laat de Arabische Lente geen illusie worden! 
5160	Eenmalige gift
     */
    public final static int FORM_ID_238_WRITE_FOR_RIGHTS = 238;
    public final static int FORM_ID_259_UNKNOWN = 259;
    public final static int FORM_ID_279_RSVP = 279;
    public final static int FORM_ID_280_SMS_ACTIE = 280;
    public final static int FORM_ID_281_BLIKSEMACTIES = 281;
    public final static int FORM_ID_283_EMAIL_ACTIE = 283;
    public final static int FORM_ID_283_EMAIL_ACTIE_ALIAS = 7412;
    public final static int FORM_ID_295_CONTACT = 295;
    public final static int FORM_ID_345_JONGEREN_ACTIE_NETWERK = 345;
    public final static int FORM_ID_383_CONTACT_WEBREDACTIE = 383;
    public final static int FORM_ID_385_CONTACT_DFG = 385;
    public final static int FORM_ID_435_AANMELDEN_COLLECTANT = 435;
    public final static int FORM_ID_436_GIFT_VIA_MACHTIGING = 436;
    public final static int FORM_ID_439_GEEF_OM_VRIJHEID = 439;
    public final static int FORM_ID_441_COLLECTECOORDINATOR = 441;
    public final static int FORM_ID_444_NOTARIEEL_SCHENKEN = 444;
    public final static int FORM_ID_4816_GROETENLIJST = 4816;
    public final static int FORM_ID_491_START_UW_EIGEN_PROJECT = 491;
    public final static int FORM_ID_500_WORD_LID = 500;
    public final static int FORM_ID_5100_RAPID_RESPONSE  = 5100;
    public final static int FORM_ID_5160_EENMALIGE_GIFT  = 5160;
    public final static int FORM_ID_5620_WORDT_VERVOLGD = 5620;
    public final static int FORM_ID_5625_WORDT_VERVOLGD_CADEAU = 5625;
    public final static int FORM_ID_5748_WORDT_VERVOLGD_PROEF = 5748;
    public final static int FORM_ID_5749_WIJZIGINGSFORMULIER = 5749;
    public final static int FORM_ID_5754_AAN_AFMELDEN_ENIEUWS = 5754;
    public final static int FORM_ID_5756_AANMELDEN_PERSDIENST = 5756;
    public final static int FORM_ID_5757_AANVRAGEN_ACTIEPAKKET = 5757;
    public final static int FORM_ID_5808_KLACHTENFORMULIER = 5808;
    public final static int FORM_ID_5809_AANVRAGEN_FOLDERS = 5809;
    public final static int FORM_ID_5810_AFMELDEN_ACTIEMAILINGS = 5810;
    public final static int FORM_ID_5821_AANMELDEN_FLEXVRIJWILLIGER = 5821;
    public final static int FORM_ID_5951_UNKNOWN = 5951;
    public final static int FORM_ID_6021_AFMELDEN_ACCEPTEMAILS = 6021;
    public final static int FORM_ID_6740_BEDANKT_VOOR_STEUN = 6740;
    public final static int FORM_ID_7034_UNKNOWN = 7034;
    public final static int FORM_ID_7061_UNKNOWN = 7061;
    public final static int FORM_ID_7064_UNKNOWN = 7064;
    public final static int FORM_ID_7066_UNKNOWN = 7066;
    public final static int FORM_ID_7068_UNKNOWN = 7068;
    public final static int FORM_ID_7436_KAARTJES_VOOR_DOTAN = 7436;
    public final static int FORM_ID_7591_AANMELDEN_COLLECTE_ORGANISATOR = 7591;
    public final static int FORM_ID_7702_ARABISCHE_LENTE = 7702;
    public final static int FORM_ID_7870_UNKNOWN = 7870;
    //
    private final static String FORM_NAME_238_WRITE_FOR_RIGHTS = "Write for rights";
    private final static String FORM_NAME_259_UNKNOWN = "";
    private final static String FORM_NAME_279_RSVP = "RSVP-netwerk";
    private final static String FORM_NAME_280_SMS_ACTIE = "SMS-acties";
    private final static String FORM_NAME_281_BLIKSEMACTIES = "Bliksemacties";
    private final static String FORM_NAME_283_EMAIL_ACTIE = "E-mailacties";
    private final static String FORM_NAME_283_EMAIL_ACTIE_ALIAS = "E-mailacties";
    private final static String FORM_NAME_295_CONTACT = "Contactformulier";
    private final static String FORM_NAME_345_JONGEREN_ACTIE_NETWERK = "Jongeren actie netwerk";
    private final static String FORM_NAME_383_CONTACT_WEBREDACTIE = "Contact webredactie";
    private final static String FORM_NAME_385_CONTACT_DFG = "Contact DFG";
    private final static String FORM_NAME_435_AANMELDEN_COLLECTANT = "Aanmelden als collectant";
    private final static String FORM_NAME_436_GIFT_VIA_MACHTIGING = "Gift via machtiging";
    private final static String FORM_NAME_439_GEEF_OM_VRIJHEID = "Geef om vrijheid-pakket";
    private final static String FORM_NAME_441_COLLECTECOORDINATOR = "Collectecoördinator";
    private final static String FORM_NAME_444_NOTARIEEL_SCHENKEN = "Collectecoördinator";
    private final static String FORM_NAME_4816_GROETENLIJST = "";
    private final static String FORM_NAME_491_START_UW_EIGEN_PROJECT = "Start uw eigen project";
    private final static String FORM_NAME_500_WORD_LID = "Word lid";
    private final static String FORM_NAME_5100_RAPID_RESPONSE = "Rapid response action team ";
    private final static String FORM_NAME_5620_WORDT_VERVOLGD = "Wordt Vervolgd";
    private final static String FORM_NAME_5625_WORDT_VERVOLGD_CADEAU = "Geef abonnement op Wordt Vervolgd cadeau";
    private final static String FORM_NAME_5748_WORDT_VERVOLGD_PROEF = "Wordt Vervolgd op proef";
    private final static String FORM_NAME_5749_WIJZIGINGSFORMULIER = "Wijzigingsformulier voor verhuizingen, tijdschriften en e-mail";
    private final static String FORM_NAME_5754_AAN_AFMELDEN_ENIEUWS = "Aanmelden of afmelden voor e-nieuws";
    private final static String FORM_NAME_5756_AANMELDEN_PERSDIENST = "Aanmelden persdienst";
    private final static String FORM_NAME_5757_AANVRAGEN_ACTIEPAKKET = "Aanvragen actiepakket";
    private final static String FORM_NAME_5808_KLACHTENFORMULIER = "Klachten formulier";
    private final static String FORM_NAME_5809_AANVRAGEN_FOLDERS = "Aanvragen van folders";
    private final static String FORM_NAME_5810_AFMELDEN_ACTIEMAILINGS = "Afmelden actiemailings";
    private final static String FORM_NAME_5821_AANMELDEN_FLEXVRIJWILLIGER = "Aanmelden als flex-vrijwilliger";
    private final static String FORM_NAME_5951_UNKNOWN = "";
    private final static String FORM_NAME_6021_AFMELDEN_ACCEPTEMAILS = "Afmelden voor acceptemails";
    private final static String FORM_NAME_6740_BEDANKT_VOOR_STEUN = "Bedankt voor uw steun";
    private final static String FORM_NAME_7034_UNKNOWN = "";
    private final static String FORM_NAME_7061_UNKNOWN = "";
    private final static String FORM_NAME_7064_UNKNOWN = "";
    private final static String FORM_NAME_7066_UNKNOWN = "";
    private final static String FORM_NAME_7068_UNKNOWN = "";
    private final static String FORM_NAME_7436_KAARTJES_VOOR_DOTAN = "Win kaartjes voor Dotan";
    private final static String FORM_NAME_7591_AANMELDEN_COLLECTE_ORGANISATOR = "Op zoek naar collecte organisatoren";
    private final static String FORM_NAME_7702_ARABISCHE_LENTE = "Laat de Arabische Lente geen illusie worden";
    private final static String FORM_NAME_7870_UNKNOWN = "";
            //
    private final static Property[] formlist = {
    new Property(String.valueOf(FORM_ID_238_WRITE_FOR_RIGHTS), FORM_NAME_238_WRITE_FOR_RIGHTS),
    new Property(String.valueOf(FORM_ID_259_UNKNOWN), FORM_NAME_259_UNKNOWN),
    new Property(String.valueOf(FORM_ID_279_RSVP), FORM_NAME_279_RSVP),
    new Property(String.valueOf(FORM_ID_280_SMS_ACTIE), FORM_NAME_280_SMS_ACTIE),
    new Property(String.valueOf(FORM_ID_281_BLIKSEMACTIES), FORM_NAME_281_BLIKSEMACTIES),
    new Property(String.valueOf(FORM_ID_283_EMAIL_ACTIE), FORM_NAME_283_EMAIL_ACTIE),
    new Property(String.valueOf(FORM_ID_283_EMAIL_ACTIE_ALIAS), FORM_NAME_283_EMAIL_ACTIE_ALIAS),
    new Property(String.valueOf(FORM_ID_295_CONTACT), FORM_NAME_295_CONTACT),
    new Property(String.valueOf(FORM_ID_345_JONGEREN_ACTIE_NETWERK), FORM_NAME_345_JONGEREN_ACTIE_NETWERK),
    new Property(String.valueOf(FORM_ID_383_CONTACT_WEBREDACTIE), FORM_NAME_383_CONTACT_WEBREDACTIE),
    new Property(String.valueOf(FORM_ID_385_CONTACT_DFG), FORM_NAME_385_CONTACT_DFG),
    new Property(String.valueOf(FORM_ID_435_AANMELDEN_COLLECTANT), FORM_NAME_435_AANMELDEN_COLLECTANT),
    new Property(String.valueOf(FORM_ID_436_GIFT_VIA_MACHTIGING), FORM_NAME_436_GIFT_VIA_MACHTIGING),
    new Property(String.valueOf(FORM_ID_439_GEEF_OM_VRIJHEID), FORM_NAME_439_GEEF_OM_VRIJHEID),
    new Property(String.valueOf(FORM_ID_441_COLLECTECOORDINATOR), FORM_NAME_441_COLLECTECOORDINATOR),
    new Property(String.valueOf(FORM_ID_444_NOTARIEEL_SCHENKEN), FORM_NAME_444_NOTARIEEL_SCHENKEN),
    new Property(String.valueOf(FORM_ID_4816_GROETENLIJST), FORM_NAME_4816_GROETENLIJST),
    new Property(String.valueOf(FORM_ID_491_START_UW_EIGEN_PROJECT), FORM_NAME_491_START_UW_EIGEN_PROJECT),
    new Property(String.valueOf(FORM_ID_500_WORD_LID), FORM_NAME_500_WORD_LID),
    new Property(String.valueOf(FORM_ID_5100_RAPID_RESPONSE), FORM_NAME_5100_RAPID_RESPONSE),
    new Property(String.valueOf(FORM_ID_5620_WORDT_VERVOLGD), FORM_NAME_5620_WORDT_VERVOLGD),
    new Property(String.valueOf(FORM_ID_5625_WORDT_VERVOLGD_CADEAU), FORM_NAME_5625_WORDT_VERVOLGD_CADEAU),
    new Property(String.valueOf(FORM_ID_5748_WORDT_VERVOLGD_PROEF), FORM_NAME_5748_WORDT_VERVOLGD_PROEF),
    new Property(String.valueOf(FORM_ID_5749_WIJZIGINGSFORMULIER), FORM_NAME_5749_WIJZIGINGSFORMULIER),
    new Property(String.valueOf(FORM_ID_5754_AAN_AFMELDEN_ENIEUWS), FORM_NAME_5754_AAN_AFMELDEN_ENIEUWS),
    new Property(String.valueOf(FORM_ID_5756_AANMELDEN_PERSDIENST), FORM_NAME_5756_AANMELDEN_PERSDIENST),
    new Property(String.valueOf(FORM_ID_5757_AANVRAGEN_ACTIEPAKKET), FORM_NAME_5757_AANVRAGEN_ACTIEPAKKET),
    new Property(String.valueOf(FORM_ID_5808_KLACHTENFORMULIER), FORM_NAME_5808_KLACHTENFORMULIER),
    new Property(String.valueOf(FORM_ID_5809_AANVRAGEN_FOLDERS), FORM_NAME_5809_AANVRAGEN_FOLDERS),
    new Property(String.valueOf(FORM_ID_5810_AFMELDEN_ACTIEMAILINGS), FORM_NAME_5810_AFMELDEN_ACTIEMAILINGS),
    new Property(String.valueOf(FORM_ID_5821_AANMELDEN_FLEXVRIJWILLIGER), FORM_NAME_5821_AANMELDEN_FLEXVRIJWILLIGER),
    new Property(String.valueOf(FORM_ID_5951_UNKNOWN), FORM_NAME_5951_UNKNOWN),
    new Property(String.valueOf(FORM_ID_6021_AFMELDEN_ACCEPTEMAILS), FORM_NAME_6021_AFMELDEN_ACCEPTEMAILS),
    new Property(String.valueOf(FORM_ID_6740_BEDANKT_VOOR_STEUN), FORM_NAME_6740_BEDANKT_VOOR_STEUN),
    new Property(String.valueOf(FORM_ID_7034_UNKNOWN), FORM_NAME_7034_UNKNOWN),
    new Property(String.valueOf(FORM_ID_7061_UNKNOWN), FORM_NAME_7061_UNKNOWN),
    new Property(String.valueOf(FORM_ID_7064_UNKNOWN), FORM_NAME_7064_UNKNOWN),
    new Property(String.valueOf(FORM_ID_7066_UNKNOWN), FORM_NAME_7066_UNKNOWN),
    new Property(String.valueOf(FORM_ID_7068_UNKNOWN), FORM_NAME_7068_UNKNOWN),
    new Property(String.valueOf(FORM_ID_7436_KAARTJES_VOOR_DOTAN), FORM_NAME_7436_KAARTJES_VOOR_DOTAN),
    new Property(String.valueOf(FORM_ID_7591_AANMELDEN_COLLECTE_ORGANISATOR), FORM_NAME_7591_AANMELDEN_COLLECTE_ORGANISATOR),
    new Property(String.valueOf(FORM_ID_7702_ARABISCHE_LENTE), FORM_NAME_7702_ARABISCHE_LENTE),
    new Property(String.valueOf(FORM_ID_7870_UNKNOWN), FORM_NAME_7870_UNKNOWN)};
    //
    //public final static String FORM_SYS_PROPERTY_MSG = "sys_msg";
    //public final static String FORM_SYS_PROPERTY_ROLEID = "sys_roleid";
    //public final static String FORM_SYS_PROPERTY_STATUS = "sys_status";
    //
    private long id;
    private long submissionid;
    private List<Property> propertylist;
    //private Properties properties;
    //private long roleid; //weg
    //private String msg; // weg
    //private int status; // weg

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Property> getPropertylist() {
        return propertylist;
    }

    public void setPropertylist(List<Property> propertylist) {
        this.propertylist = propertylist;
    }

    public long getSubmissionid() {
        return submissionid;
    }

    public void setSubmissionid(long submissionid) {
        this.submissionid = submissionid;
    }

    public static String getFormname(long id) {
        Properties properties = new Properties();
        for (Property form : formlist) {
            properties.put(form.getKey(), form.getValue());
        }
        String name = properties.getProperty(String.valueOf(id));
        if (name == null) {
            return "Unsupported form id ".concat(String.valueOf(id));
        } else {
            if (name.isEmpty()) {
                return "Unknown form id ".concat(String.valueOf(id));
            } else {
                return name;
            }
        }
    }

    /*
    public void setMsgProperty(String msg) {
        try {
            Property property = new Property(FORM_SYS_PROPERTY_MSG, msg);
            propertylist.add(property);
        } catch (Exception e) {
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, e);
        }
    }
     * 
     */

    /*
    public void setRoleidProperty(long roleid) {
        try {
            Property property = new Property(FORM_SYS_PROPERTY_ROLEID, String.valueOf(roleid));
            propertylist.add(property);
        } catch (Exception e) {
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, e);
        }
    }
     * 
     */

    /*
    public void setStatusProperty(int status) {
        try {
            Property property = new Property(FORM_SYS_PROPERTY_STATUS, String.valueOf(status));
            propertylist.add(property);
        } catch (Exception e) {
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, e);
        }
    }
     * 
     */

    /*
    public String getMsgProperty() {
        try {
            for (Property property : propertylist) {
                if (property.getKey().equals(FORM_SYS_PROPERTY_MSG)) {
                    return property.getValue();
                }
            }
            return "";
        } catch (Exception e) {
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, e);
            return "";
        }
    }
     * 
     */

    /*
    public long getRoleidProperty() {
        try {
            for (Property property : propertylist) {
                if (property.getKey().equals(FORM_SYS_PROPERTY_ROLEID)) {
                    if (isLong(property.getValue())) {
                        return Long.valueOf(property.getValue());
                    }
                }
            }
            return 0;
        } catch (Exception e) {
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, e);
            return 0;
        }
    }
     * 
     */

    /*
    public int getStatusProperty() {
        try {
            for (Property property : propertylist) {
                if (property.getKey().equals(FORM_SYS_PROPERTY_STATUS)) {
                    if (isInteger(property.getValue())) {
                        return Integer.valueOf(property.getValue());
                    }
                }
            }
            return 0;
        } catch (Exception e) {
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, e);
            return 0;
        }
    }
     * 
     */

    /*
    private boolean isInteger(String value) {
        try {
            Integer valueOf = Integer.valueOf(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
     * 
     */

    /*
    private boolean isLong(String value) {
        try {
            Long valueOf = Long.valueOf(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
     * 
     */
}
