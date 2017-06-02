package com.instanza.i18nmanager.service.model;

/**
 * Created by luanhaipeng on 16/12/9.
 */
public class I18nExportFormatMO {
    private String fileName;
    private byte[] content;

    public I18nExportFormatMO(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
