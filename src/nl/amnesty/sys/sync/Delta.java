/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.sync;

import java.util.ArrayList;
import java.util.Collection;
import nl.amnesty.crm.collection.IdStartdateEnddate;

/**
 *
 * @author evelzen
 */
public class Delta {

    private Collection<IdStartdateEnddate> increase_collection_a;
    private Collection<IdStartdateEnddate> decrease_collection_a;
    private Collection<IdStartdateEnddate> increase_collection_b;
    private Collection<IdStartdateEnddate> decrease_collection_b;
    private int count_a_active;
    private int count_a_passive;
    private int count_b_active;
    private int count_b_passive;

    public Delta() {
        increase_collection_a = new ArrayList();
        decrease_collection_a = new ArrayList();
        increase_collection_b = new ArrayList();
        decrease_collection_b = new ArrayList();
        count_a_active = 0;
        count_a_passive = 0;
        count_b_active = 0;
        count_b_passive = 0;
    }

    public Delta(Collection<IdStartdateEnddate> increase_collection_a, Collection<IdStartdateEnddate> decrease_collection_a, Collection<IdStartdateEnddate> increase_collection_b, Collection<IdStartdateEnddate> decrease_collection_b) {
        this.increase_collection_a = increase_collection_a;
        this.decrease_collection_a = decrease_collection_a;
        this.increase_collection_b = increase_collection_b;
        this.decrease_collection_b = decrease_collection_b;
    }

    public Collection<IdStartdateEnddate> getDecrease_collection_a() {
        return decrease_collection_a;
    }

    public void setDecrease_collection_a(Collection<IdStartdateEnddate> decrease_collection_a) {
        this.decrease_collection_a = decrease_collection_a;
    }

    public Collection<IdStartdateEnddate> getDecrease_collection_b() {
        return decrease_collection_b;
    }

    public void setDecrease_collection_b(Collection<IdStartdateEnddate> decrease_collection_b) {
        this.decrease_collection_b = decrease_collection_b;
    }

    public Collection<IdStartdateEnddate> getIncrease_collection_a() {
        return increase_collection_a;
    }

    public void setIncrease_collection_a(Collection<IdStartdateEnddate> increase_collection_a) {
        this.increase_collection_a = increase_collection_a;
    }

    public Collection<IdStartdateEnddate> getIncrease_collection_b() {
        return increase_collection_b;
    }

    public void setIncrease_collection_b(Collection<IdStartdateEnddate> increase_collection_b) {
        this.increase_collection_b = increase_collection_b;
    }

    public int getCount_a_active() {
        return count_a_active;
    }

    public void setCount_a_active(int count_a_active) {
        this.count_a_active = count_a_active;
    }

    public int getCount_a_passive() {
        return count_a_passive;
    }

    public void setCount_a_passive(int count_a_passive) {
        this.count_a_passive = count_a_passive;
    }

    public int getCount_b_active() {
        return count_b_active;
    }

    public void setCount_b_active(int count_b_active) {
        this.count_b_active = count_b_active;
    }

    public int getCount_b_passive() {
        return count_b_passive;
    }

    public void setCount_b_passive(int count_b_passive) {
        this.count_b_passive = count_b_passive;
    }
}
