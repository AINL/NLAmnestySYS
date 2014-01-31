/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.sys.crm;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author evelzen
 */
public class Action {
    public static final int ACTIONSTATUS_REQUESTED = 1;
    public static final int ACTIONSTATUS_NO_ACTION = 2;
    public static final int ACTIONSTATUS_ABORTED = 3;
    public static final int ACTIONSTATUS_COMPLETED = 4;
    //
    private long roleid;
    private Date daterequest;
    private Date datecomplete;
    private int status;
    private String message;

    public Action() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timezone = TimeZone.getDefault();
        calendar.setTimeZone(timezone);
        this.daterequest = calendar.getTime();
        this.datecomplete = null;
        this.status = ACTIONSTATUS_REQUESTED;
        this.message = "";
    }

    public Action(long roleid, Date daterequest, Date datecomplete, int status, String message) {
        this.roleid = roleid;
        this.daterequest = daterequest;
        this.datecomplete = datecomplete;
        this.status = status;
        this.message = message;
    }

    public long getRoleid() {
        return roleid;
    }

    public void setRoleid(long roleid) {
        this.roleid = roleid;
    }

    public Date getDatecomplete() {
        return datecomplete;
    }

    public void setDatecomplete(Date datecomplete) {
        this.datecomplete = datecomplete;
    }

    public Date getDaterequest() {
        return daterequest;
    }

    public void setDaterequest(Date daterequest) {
        this.daterequest = daterequest;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        Calendar calendar = Calendar.getInstance();
        TimeZone timezone = TimeZone.getDefault();
        calendar.setTimeZone(timezone);
        this.datecomplete = calendar.getTime();
        this.status = status;
    }
    
    public boolean isNoaction() {
        if (this.status == ACTIONSTATUS_NO_ACTION) {
            return true;
        } else {
            return false;
        }
    }
    
}
