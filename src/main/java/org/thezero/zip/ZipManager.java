package org.thezero.zip;

import org.thezero.blackhole.app.AppLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipManager {
    private static final int BUFFER = 80000;

    public void zip(List<String> _files, String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (String _file : _files) {
                FileInputStream fi = new FileInputStream(_file);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_file.substring(_file.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zipFolder(String _inputFolder, String _targetLocation) {
        try {
            FileOutputStream fos = new FileOutputStream(_targetLocation);
            ZipOutputStream zos = new ZipOutputStream(fos);

            addFolder(zos, _inputFolder, _inputFolder);

            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unzip(String _zipFile, String _targetLocation) {
        //create target location folder if not exist
        dirChecker(_targetLocation);

        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {

                //create dir if required while unzipping
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    private static void addFolder(ZipOutputStream zos,String folderName,String baseFolderName)throws Exception{
        File f = new File(folderName);
        if(f.exists()){
            if(f.isDirectory()){
                //Thank to peter
                //For pointing out missing entry for empty folder
                if(!folderName.equalsIgnoreCase(baseFolderName)){
                    String entryName = folderName.substring(baseFolderName.length()+1,folderName.length()) + File.separatorChar;
                    ZipEntry ze= new ZipEntry(entryName);
                    zos.putNextEntry(ze);
                }
                File f2[] = f.listFiles();
                for(int i=0;i<f2.length;i++){
                    addFolder(zos,f2[i].getAbsolutePath(),baseFolderName);
                }
            }else{
                //add file
                //extract the relative name for entry purpose
                String entryName = folderName.substring(baseFolderName.length()+1,folderName.length());
                AppLog.i("Adding file entry " + entryName + "...");
                ZipEntry ze= new ZipEntry(entryName);
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(folderName);
                int len;
                byte buffer[] = new byte[1024];
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
                zos.closeEntry();
            }
        }
    }

}
