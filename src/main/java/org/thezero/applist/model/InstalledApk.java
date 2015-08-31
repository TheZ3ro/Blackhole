package org.thezero.applist.model;

import android.graphics.drawable.*;

import org.thezero.blackhole.Utility;

import java.io.*;

public class InstalledApk extends Apk
{
    private Drawable icon;
    private boolean isBackedUp;
    private boolean systemApp;
    
    @Override
    public File getApkFile() {
        return new File(this.getPath());
    }
    
    public Drawable getIcon() {
        return this.icon;
    }
    
    @Override
    public File getMainObbFile() {
        final File file = new File(Utility.SD_PATH + "/Android/obb/" + this.getPackageName() + "/");
        if (file.isDirectory()) {
            for (final File file2 : file.listFiles()) {
                if (file2.getName().contains("main")) {
                    return file2;
                }
            }
        }
        return null;
    }
    
    @Override
    public File getPatchObbFile() {
        final File file = new File(Utility.SD_PATH + "/Android/obb/" + this.getPackageName() + "/");
        if (file.isDirectory()) {
            for (final File file2 : file.listFiles()) {
                if (file2.getName().contains("patch")) {
                    return file2;
                }
            }
        }
        return null;
    }
    
    public boolean isBackedUp() {
        return this.isBackedUp;
    }
    
    public boolean isSystemApp() {
        return this.systemApp;
    }
    
    public void setBackedUp(final boolean isBackedUp) {
        this.isBackedUp = isBackedUp;
    }
    
    public void setIcon(final Drawable icon) {
        this.icon = icon;
    }
    
    public void setSystemApp(final boolean systemApp) {
        this.systemApp = systemApp;
    }
}
