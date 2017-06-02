package com.instanza.i18nmanager.utils;

import com.instanza.i18nmanager.service.model.I18nExportFormatMO;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by luanhaipeng on 16/12/9.
 */
public class ExportFileUtil {



    public static void doExportTextFileZip(HttpServletResponse response, List<I18nExportFormatMO> moList, String exportFileName) throws IOException {

        setResponseHeader(response,exportFileName,calculateContentLength(moList));

        ServletOutputStream servletOutputStream = response.getOutputStream();

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(servletOutputStream));

        for (I18nExportFormatMO mo:moList){
            ZipEntry entry = new ZipEntry(mo.getFileName());
            out.putNextEntry(entry);
            out.write(mo.getContent());
        }

        out.close();

    }

    private static long calculateContentLength(List<I18nExportFormatMO> moList) {
        long contentLength = 0;
        for (I18nExportFormatMO mo:moList){
            contentLength = contentLength + mo.getContent().length;
        }
        return contentLength;
    }


    private static void setResponseHeader(HttpServletResponse response,String fileName,long fileLength ){
        response.addHeader("content-type", "application/x-msdownload");
        //适应中文名字下载
        try {
            response.addHeader("Content-disposition", "attachment;filename=" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("application/vnd.ms-excel");
        String length = String.valueOf(fileLength);
        response.setHeader("Content_Length", length);
    }




    public static void doExportTextFile(HttpServletResponse response, String fileName, byte[] contentBytes) {

        long fileLength = contentBytes.length;
        setResponseHeader(response,fileName,fileLength);


        // 下载
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(contentBytes);

            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
