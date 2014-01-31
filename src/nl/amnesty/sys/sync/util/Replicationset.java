/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.sync.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.amnesty.crm.collection.IdStartdateEnddate;
import nl.amnesty.crm.util.DateUtil;

/**
 *
 * @author evelzen
 */
public class Replicationset {

    private Collection<IdStartdateEnddate> collection_a;
    private Collection<IdStartdateEnddate> collection_b;
    private Set set;

    public Replicationset() {
    }

    public Replicationset(Collection<IdStartdateEnddate> collection_a, Collection<IdStartdateEnddate> collection_b) {
        this.collection_a = collection_a;
        this.collection_b = collection_b;
        this.set = new HashSet();
        build();
    }

    public Set getSet() {
        return set;
    }

    /**
     *
     * @param collection_a
     * @param collection_b
     * @return
     */
    private void build() {
        this.set.clear();
        Set<String> set_a = new HashSet();
        Set<String> set_a_temp = new HashSet();
        Set<String> set_b = new HashSet();
        Set<String> set_b_temp = new HashSet();

        int size_a = 0;
        int size_b = 0;
        try {
            for (IdStartdateEnddate element_a : this.collection_a) {
                set_a.add(element_a.getId());
                if (set_a.size() == size_a) {
                    Logger.getLogger(Replicationset.class.getName()).log(Level.WARNING, MessageFormat.format("Duplicate element in set A (id {0}, startdate {1}, enddate {2})", new Object[]{element_a.getId(), DateUtil.formattedDate(element_a.getStartdate()), DateUtil.formattedDate(element_a.getEnddate())}));
                }
                size_a = set_a.size();
                // Set set_a will be altered by the removeAll operation so we need another instance
                set_a_temp.add(element_a.getId());
            }
            for (IdStartdateEnddate element_b : this.collection_b) {
                set_b.add(element_b.getId());
                if (set_b.size() == size_b) {
                    Logger.getLogger(Replicationset.class.getName()).log(Level.WARNING, MessageFormat.format("Duplicate element in set B (id {0}, startdate {1}, enddate {2})", new Object[]{element_b.getId(), DateUtil.formattedDate(element_b.getStartdate()), DateUtil.formattedDate(element_b.getEnddate())}));
                }
                size_b = set_b.size();
                // Set set_b will be altered by the retainAll operation so we need another instance
                set_b_temp.add(element_b.getId());
            }
            // Removes from set_a all of its elements that are contained in set_b. This leaves us with elements only present in set_a
            set_a.removeAll(set_b);
            // Removes from set_b all of its elements that are contained in set_a. This leaves us with elements only present in set_b
            // Set set_a was altered by the removeAll operation so we need to use the temp set_a_temp which is unaltered
            set_b.removeAll(set_a_temp);
            // Retains only the elements in set_a_temp that are contained in set_b_temp. In other words: intersect the two sets.
            set_a_temp.retainAll(set_b_temp);
            // Now add all the elements to one set: the intersection of A and B, those only in A and those only in B
            this.set.addAll(set_a_temp);
            this.set.addAll(set_a);
            this.set.addAll(set_b);
        } catch (Exception e) {
            Logger.getLogger(Replicationset.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
