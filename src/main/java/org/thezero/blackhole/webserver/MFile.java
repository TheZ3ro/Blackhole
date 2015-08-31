package org.thezero.blackhole.webserver;

import org.thezero.blackhole.Utility;

/**
 * Created by thezero 29/08/15.
 */
public class MFile {
    public final String name;
    public final String link;
    public final String size;
    public final String type;
    public String uploaded="";

    public MFile(String name, String link, String size) {
        this.name = name;
        this.link = link;
        this.size = size;
        this.type = Utility.getTypeForFile(name).toUpperCase();
    }
    public MFile(String name, String link, String size, String type) {
        this.name = name;
        this.link = link;
        this.size = size;
        this.type = type;
    }
    public MFile(String name, String link, String size, boolean uploaded) {
        this(name,link,size);
        this.uploaded = uploaded? "uploaded":"";
    }
}
