package com.awsapi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.springframework.util.FileCopyUtils.BUFFER_SIZE;

public class ZipUtil {

    private static final Logger log = LoggerFactory.getLogger(ZipUtil.class);

    /**
     * zip解压
     * @param srcFile        zip源文件
     * @param destDirPath     解压后的目标文件夹
     */
    public static void unZip(File srcFile, String destDirPath) {
        long start = System.currentTimeMillis();
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "The file does not exist");
        }
        try (ZipFile zipFile = new ZipFile(srcFile)) {
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                System.out.println("unzip:" + entry.getName());
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    Boolean isSuccess = dir.mkdirs();
                } else {
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    if(!targetFile.getParentFile().exists()){
                        Boolean isSuccess = targetFile.getParentFile().mkdirs();
                    }
                    Boolean isSuccess = targetFile.createNewFile();
                    try (InputStream is = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(targetFile)) {
                        int len;
                        byte[] buf = new byte[BUFFER_SIZE];
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("unZip>>>>>>>>>>>>FileNotFoundException:"+e);
                    }
                }
            }
            long end = System.currentTimeMillis();
            log.info("the complete，elapsed time: " + (end - start) +" ms");
        } catch (IOException e) {
            throw new RuntimeException("unZip>>>>>>>>>>>>IOException:"+e);
        }
    }
}