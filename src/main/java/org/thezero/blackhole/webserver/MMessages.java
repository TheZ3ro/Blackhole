package org.thezero.blackhole.webserver;

/**
 * Created by thezero on 31/08/15.
 */
public class MMessages {
    public String title="";
    public final String text;
    public final String status;

    public MMessages(String title, String text, String status) {
        this.title = title;
        this.text = text;
        this.status = status;
    }
    public MMessages(String text, String status) {
        this.text = text;
        this.status = status;
    }
}
