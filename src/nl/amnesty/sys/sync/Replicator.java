/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.sync;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.amnesty.crm.collection.IdStartdateEnddate;
import nl.amnesty.sys.sync.util.Elementpair;
import nl.amnesty.sys.sync.util.Replicationset;

/**
 *
 * @author evelzen
 */
public class Replicator {

    final private static boolean DEBUG = false;

    /**
     *
     * @param delta
     * @param collection_a
     * @param collection_b
     * @param collection_a_is_leading
     * @return
     */
    public static Delta replicate(Delta delta, Collection<IdStartdateEnddate> collection_a, Collection<IdStartdateEnddate> collection_b) {
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Replicator.class);
        try {
            if (DEBUG) {
                System.out.println("Size of collection A is " + collection_a.size());
                System.out.println("Size of collection B is " + collection_b.size());
            }
            Replicationset replicationset = new Replicationset(collection_a, collection_b);
            Set<String> set = replicationset.getSet();

            if (DEBUG) {
                System.out.println("Size of set is: " + set.size());
            }

            for (String id : set) {
                Elementpair elementpair = new Elementpair(id, collection_a, collection_b);
                if (DEBUG) {
                    displayStartdateEnddate(elementpair.getMostrecent_element_a(), elementpair.getMostrecent_element_b());
                }
                switch (elementpair.getMostrecent_a_or_b()) {
                    case Elementpair.MOST_RECENT_ENTRY_UNKNOWN:
                        delta = updateDeltaAMostrecent(delta, elementpair.getMostrecent_element_a(), elementpair.getMostrecent_element_b());
                        break;
                    case Elementpair.MOST_RECENT_ENTRY_IN_COLLECTION_A:
                        delta = updateDeltaAMostrecent(delta, elementpair.getMostrecent_element_a(), elementpair.getMostrecent_element_b());
                        break;
                    case Elementpair.MOST_RECENT_ENTRY_IN_COLLECTION_B:
                        delta = updateDeltaBMostrecent(delta, elementpair.getMostrecent_element_a(), elementpair.getMostrecent_element_b());
                        break;
                }

                if (elementpair.getMostrecent_element_a() != null) {
                    if (elementpair.getMostrecent_element_a().getEnddate() == null) {
                        delta.setCount_a_active(delta.getCount_a_active() + 1);
                    } else {
                        delta.setCount_a_passive(delta.getCount_a_passive() + 1);
                    }
                }
                if (elementpair.getMostrecent_element_b() != null) {
                    if (elementpair.getMostrecent_element_b().getEnddate() == null) {
                        delta.setCount_b_active(delta.getCount_b_active() + 1);
                    } else {
                        delta.setCount_b_passive(delta.getCount_b_passive() + 1);
                    }
                }
            }

            //if (DEBUG) {
                displayDeltaActivePassive(logger, delta);
            //}

            return delta;
        } catch (Exception e) {
            Logger.getLogger(Replicator.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return delta;
        }
    }

    /**
     *
     * @param delta
     * @param mostrecent_element_a
     * @param mostrecent_element_b
     * @return
     */
    private static Delta updateDeltaAMostrecent(Delta delta, IdStartdateEnddate mostrecent_element_a, IdStartdateEnddate mostrecent_element_b) {
        try {
            if (mostrecent_element_a.getEnddate() == null) {
                // Entry in Collection A is active and most recent
                if (mostrecent_element_b == null) {
                    // Entry in Collection A is active, no entry in Collection B: B should be increased
                    delta = doIncreaseB(delta, mostrecent_element_a);
                } else {
                    if (mostrecent_element_b.getEnddate() == null) {
                        // Entries in both Collection A and B are active: No further action
                    } else {
                        // Entry in Collection A is active and more recent, entry in Collection B is pasive: B should be increased
                        delta = doIncreaseB(delta, mostrecent_element_a);
                    }
                }
            } else {
                // Entry in Collection A is pasive and most recent
                if (mostrecent_element_b == null) {
                    // Entry in Collection A is pasive, no entry in Collection B: No further action
                } else {
                    if (mostrecent_element_b.getEnddate() == null) {
                        // Entry in Collection A is pasive and more recent, entry in Collection B is active: B should be decreased
                        delta = doDecreaseB(delta, mostrecent_element_a);
                    } else {
                        // Entries in both Collection A and B are pasive: No further action
                    }
                }
            }
            return delta;
        } catch (Exception e) {
            Logger.getLogger(Replicator.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return delta;
        }
    }

    /**
     *
     * @param delta
     * @param mostrecent_element_a
     * @param mostrecent_element_b
     * @return
     */
    private static Delta updateDeltaBMostrecent(Delta delta, IdStartdateEnddate mostrecent_element_a, IdStartdateEnddate mostrecent_element_b) {
        try {
            if (mostrecent_element_b.getEnddate() == null) {
                // Entry in Collection B is active and most recent
                if (mostrecent_element_a == null) {
                    // Entry in Collection B is active, no entry in Collection A: A should be increased
                    delta = doIncreaseA(delta, mostrecent_element_b);
                } else {
                    if (mostrecent_element_a.getEnddate() == null) {
                        // Entries in both Collection B and A are active: No further action
                    } else {
                        // Entry in Collection B is active and more recent, entry in Collection A is pasive: A should be increased
                        delta = doIncreaseA(delta, mostrecent_element_b);
                    }
                }
            } else {
                // Entry in Collection B is pasive and most recent
                if (mostrecent_element_a == null) {
                    // Entry in Collection B is pasive, no entry in Colelction A: No further action
                } else {
                    if (mostrecent_element_a.getEnddate() == null) {
                        // Entry in Collection B is pasive and more recent, entry in Collection A is active: A should be decreased
                        delta = doDecreaseA(delta, mostrecent_element_b);
                    } else {
                        // Entries in both Collection B and A are pasive: No further action
                    }
                }
            }
            return delta;
        } catch (Exception e) {
            Logger.getLogger(Replicator.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return delta;
        }
    }

    /**
     *
     * @param delta
     * @param idstartdateenddate
     * @return
     */
    private static Delta doIncreaseA(Delta delta, IdStartdateEnddate idstartdateenddate) {
        delta.getIncrease_collection_a().add(idstartdateenddate);

        //DEBUG
        if (DEBUG) {
            System.out.println("Collection A should be increased with " + idstartdateenddate.getId());
        }

        return delta;
    }

    /**
     *
     * @param delta
     * @param idstartdateenddate
     * @return
     */
    private static Delta doIncreaseB(Delta delta, IdStartdateEnddate idstartdateenddate) {
        delta.getIncrease_collection_b().add(idstartdateenddate);

        //DEBUG
        if (DEBUG) {
            System.out.println("Collection B should be increased with " + idstartdateenddate.getId());
        }

        return delta;
    }

    /**
     *
     * @param delta
     * @param idstartdateenddate
     * @return
     */
    private static Delta doDecreaseA(Delta delta, IdStartdateEnddate idstartdateenddate) {
        delta.getDecrease_collection_a().add(idstartdateenddate);

        //DEBUG
        if (DEBUG) {
            System.out.println("Collection A should be decreased with " + idstartdateenddate.getId());
        }

        return delta;
    }

    /**
     *
     * @param delta
     * @param idstartdateenddate
     * @return
     */
    private static Delta doDecreaseB(Delta delta, IdStartdateEnddate idstartdateenddate) {
        delta.getDecrease_collection_b().add(idstartdateenddate);

        //DEBUG
        if (DEBUG) {
            System.out.println("Collection B should be decreased with " + idstartdateenddate.getId());
        }

        return delta;
    }

    /**
     *
     * @param delta
     */
    private static void displayDeltaActivePassive(org.apache.log4j.Logger logger, Delta delta) {
        int a_active = delta.getCount_a_active();
        int a_passive = delta.getCount_a_passive();
        int b_active = delta.getCount_b_active();
        int b_passive = delta.getCount_b_passive();
        int total_a = a_active + delta.getIncrease_collection_a().size() - delta.getDecrease_collection_a().size();
        int total_b = b_active + delta.getIncrease_collection_b().size() - delta.getDecrease_collection_b().size();

        logger.info("");
        logger.info("Collection A passive: " + a_passive);
        logger.info("Collection B passive: " + b_passive);
        logger.info("");
        logger.info("Collection A active:                                   " + a_active);
        logger.info("Collection A should be increased with no of entries: + " + delta.getIncrease_collection_a().size());
        logger.info("Collection A should be decreased with no of entries: - " + delta.getDecrease_collection_a().size());
        logger.info("                                                       ----------");
        logger.info("Collection A active after replication:                 " + total_a);
        logger.info("");
        logger.info("Collection B active:                                   " + b_active);
        logger.info("Collection B should be increased with no of entries: + " + delta.getIncrease_collection_b().size());
        logger.info("Collection B should be decreased with no of entries: - " + delta.getDecrease_collection_b().size());
        logger.info("                                                       ----------");
        logger.info("Collection B active after replication:                 " + total_b);
    }

    /**
     *
     * @param element_a
     * @param element_b
     */
    private static void displayStartdateEnddate(IdStartdateEnddate element_a, IdStartdateEnddate element_b) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("");
        if (element_a != null) {
            if (element_a.getEnddate() == null) {
                System.out.println("Element A: " + element_a.getId() + " Startdate:  " + dateformat.format(element_a.getStartdate()) + ", enddate: null");
            } else {
                System.out.println("Element A: " + element_a.getId() + " Startdate:  " + dateformat.format(element_a.getStartdate()) + ", enddate: " + dateformat.format(element_a.getEnddate()));
            }
        }
        if (element_b != null) {
            if (element_b.getEnddate() == null) {
                System.out.println("Element B: " + element_b.getId() + " Startdate:  " + dateformat.format(element_b.getStartdate()) + ", enddate: null");
            } else {
                System.out.println("Element B: " + element_b.getId() + " Startdate:  " + dateformat.format(element_b.getStartdate()) + ", enddate: " + dateformat.format(element_b.getEnddate()));
            }
        }
    }
}
