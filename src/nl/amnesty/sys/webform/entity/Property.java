/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.webform.entity;

/**
 *
 * @author evelzen
 */
public class Property {

    private String key;
    private String value;

    public Property() {
    }

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    public void setValue(String value) {
        this.value = value;
    }
}
