/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.sync.util;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.amnesty.crm.collection.IdStartdateEnddate;

/**
 *
 * @author evelzen
 */
public class Elementpair {

    final public static int MOST_RECENT_ENTRY_UNKNOWN = 0;
    final public static int MOST_RECENT_ENTRY_IN_COLLECTION_A = 1;
    final public static int MOST_RECENT_ENTRY_IN_COLLECTION_B = 2;
    private String id;
    private Collection<IdStartdateEnddate> collection_a;
    private Collection<IdStartdateEnddate> collection_b;
    private IdStartdateEnddate mostrecent_element_a;
    private IdStartdateEnddate mostrecent_element_b;
    private int mostrecent_a_or_b;

    public Elementpair() {
    }

    public Elementpair(String id, Collection<IdStartdateEnddate> collection_a, Collection<IdStartdateEnddate> collection_b) {
        this.id = id;
        this.collection_a = collection_a;
        this.collection_b = collection_b;
        mostrecent();
    }

    public int getMostrecent_a_or_b() {
        return mostrecent_a_or_b;
    }

    public IdStartdateEnddate getMostrecent_element_a() {
        return mostrecent_element_a;
    }

    public IdStartdateEnddate getMostrecent_element_b() {
        return mostrecent_element_b;
    }

    /**
     *
     * @param id
     * @param collection_a
     * @param collection_b
     * @return
     */
    private void mostrecent() {
        try {
            this.mostrecent_a_or_b = MOST_RECENT_ENTRY_UNKNOWN;
            this.mostrecent_element_a = getMostrecentElement(this.collection_a);
            this.mostrecent_element_b = getMostrecentElement(this.collection_b);
            Calendar cal = Calendar.getInstance();
            cal.set(1900, Calendar.JANUARY, 1);

            if (this.mostrecent_element_a != null) {
                if (this.mostrecent_element_a.getStartdate().after(cal.getTime())) {
                    this.mostrecent_a_or_b = MOST_RECENT_ENTRY_IN_COLLECTION_A;
                    cal.setTime(this.mostrecent_element_a.getStartdate());
                }
                if (this.mostrecent_element_a.getEnddate() != null) {
                    if (this.mostrecent_element_a.getEnddate().after(cal.getTime())) {
                        this.mostrecent_a_or_b = MOST_RECENT_ENTRY_IN_COLLECTION_A;
                        cal.setTime(this.mostrecent_element_a.getEnddate());
                    }
                }
            } else {
                //System.out.println("this.mostrecent_element_a is null id: " + this.id);
            }
            if (this.mostrecent_element_b != null) {
                if (this.mostrecent_element_b.getStartdate().after(cal.getTime())) {
                    this.mostrecent_a_or_b = MOST_RECENT_ENTRY_IN_COLLECTION_B;
                    cal.setTime(this.mostrecent_element_b.getStartdate());
                }
                if (this.mostrecent_element_b.getEnddate() != null) {
                    if (this.mostrecent_element_b.getEnddate().after(cal.getTime())) {
                        this.mostrecent_a_or_b = MOST_RECENT_ENTRY_IN_COLLECTION_B;
                        cal.setTime(this.mostrecent_element_b.getEnddate());
                    }
                }
            } else {
                //System.out.println("this.mostrecent_element_b is null id: " + this.id);
            }
        } catch (Exception e) {
            Logger.getLogger(Elementpair.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     *
     * @param collection
     * @param id
     * @return
     *
     * Opzegging meer recent dan open aanmelding???
     */
    private IdStartdateEnddate getMostrecentElement(Collection<IdStartdateEnddate> collection) {
        IdStartdateEnddate mostrecent_element = null;
        IdStartdateEnddate active_element = null;
        Calendar cal = Calendar.getInstance();
        cal.set(1900, Calendar.JANUARY, 1);
        Calendar calendarlowvalue = Calendar.getInstance();
        Calendar calendarstart = Calendar.getInstance();
        Calendar calendarend = Calendar.getInstance();
        calendarlowvalue.set(1900, Calendar.JANUARY, 1);
        calendarstart.set(1900, Calendar.JANUARY, 1);
        calendarend.set(1900, Calendar.JANUARY, 1);
        boolean first = true;
        try {
            Iterator<IdStartdateEnddate> iterator = collection.iterator();
            while (iterator.hasNext()) {
                IdStartdateEnddate element = iterator.next();
                if (element.getId().equals(this.id)) {
                    if (first) {
                        // Initially set the first element to be the most recent
                        mostrecent_element = element;
                        first = false;
                    }
                    if (element.getStartdate() != null) {
                        calendarstart.setTime(element.getStartdate());
                        // Set this element as most recent if startdate is after most recent date until now
                        if (calendarstart.after(cal)) {
                            cal.setTime(calendarstart.getTime());
                            mostrecent_element = element;
                            // Store the active element
                            if (element.getEnddate() == null) {
                                active_element = element;
                            }
                        } else {
                            // This element is not the most recent but it might be an active one (with enddate being null)
                            if (element.getEnddate() == null) {
                                if (mostrecent_element.getEnddate() == null) {
                                    Logger.getLogger(Elementpair.class.getName()).log(Level.WARNING, MessageFormat.format("Element for id {0} (startdate {1}) is active but older then most recent element (startdate {2}, enddate null)", new Object[]{this.id, formattedDate(element.getStartdate()), formattedDate(mostrecent_element.getStartdate())}));
                                } else {
                                    Logger.getLogger(Elementpair.class.getName()).log(Level.WARNING, MessageFormat.format("Element for id {0} (startdate {1}) is active but older then most recent element (startdate {2}, enddate {3})", new Object[]{this.id, formattedDate(element.getStartdate()), formattedDate(mostrecent_element.getStartdate()), formattedDate(mostrecent_element.getEnddate())}));
                                }
                                // Store the active element
                                active_element = element;
                            }
                        }
                    } else {
                        // Set startdate to low value if it is null for some reason
                        element.setStartdate(calendarlowvalue.getTime());
                    }
                    if (element.getEnddate() != null) {
                        calendarend.setTime(element.getEnddate());
                        // Set this element as most recent if enddate if after most recent date until now
                        if (calendarend.after(cal)) {
                            cal.setTime(calendarend.getTime());
                            mostrecent_element = element;
                            // There may be an active element that is older then this passive (enddate not null) element
                            if (active_element != null) {
                                Logger.getLogger(Elementpair.class.getName()).log(Level.WARNING, MessageFormat.format("Element for id {0} (startdate {1}) is active but older then most recent element (startdate {2}, enddate {3})", new Object[]{this.id, formattedDate(active_element.getStartdate()), formattedDate(element.getStartdate()), formattedDate(element.getEnddate())}));
                            }
                        }
                    }
                }
            }
            return mostrecent_element;
        } catch (Exception e) {
            Logger.getLogger(Elementpair.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    private String formattedDate(Date date) {
        String formatteddate = "";
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (day < 10) {
                formatteddate = formatteddate.concat("0").concat(String.valueOf(day)).concat("-");
            } else {
                formatteddate = formatteddate.concat(String.valueOf(day)).concat("-");
            }
            if (month < 10) {
                formatteddate = formatteddate.concat("0").concat(String.valueOf(month)).concat("-");
            } else {
                formatteddate = formatteddate.concat(String.valueOf(month)).concat("-");
            }
            formatteddate = formatteddate.concat(String.valueOf(year));
            return formatteddate;
        } catch (Exception e) {
            Logger.getLogger(Elementpair.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }
}
