package org.thezero.blackhole;

import org.thezero.blackhole.utility.Utility;

/**
 * Created by thezero 29/08/15.
 */
public class FileL {
    public final String name;
    public final String link;
    public final String size;
    public final String type;
    public FileL (String name, String link, String size) {
        this.name = name;
        this.link = link;
        this.size = size;
        this.type = Utility.getTypeForFile(name).toUpperCase();
    }
    public FileL (String name, String link, String size, String type) {
        this.name = name;
        this.link = link;
        this.size = size;
        this.type = type;
    }
}
