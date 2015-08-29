package org.thezero.applist.model;

import java.io.*;

public abstract class Apk
{
    private long date;
    private int id;
    private String name;
    private String packageName;
    private String path;
    private long size;
    private int versionCode;
    private String versionName;
    
    public abstract File getApkFile();
    
    public long getDate() {
        return this.date;
    }
    
    public int getId() {
        return this.id;
    }
    
    public abstract File getMainObbFile();
    
    public String getName() {
        return this.name;
    }
    
    public String getPackageName() {
        return this.packageName;
    }
    
    public abstract File getPatchObbFile();
    
    public String getPath() {
        return this.path;
    }
    
    public long getSize() {
        return this.size;
    }
    
    public int getVersionCode() {
        return this.versionCode;
    }
    
    public String getVersionName() {
        return this.versionName;
    }
    
    public void setDate(final long date) {
        this.date = date;
    }
    
    public void setId(final int id) {
        this.id = id;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }
    
    public void setPath(final String path) {
        this.path = path;
    }
    
    public void setSize(final long size) {
        this.size = size;
    }
    
    public void setVersionCode(final int versionCode) {
        this.versionCode = versionCode;
    }
    
    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }
}
