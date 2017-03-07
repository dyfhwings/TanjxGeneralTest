package com.gooagoo.transport.test.mina;

import java.io.Serializable;

public class FileUploadRequest implements Serializable
{

    private static final long serialVersionUID = 1L;
    private String hostname;
    private String filename;
    private byte[] fileContent;

    public String getHostname()
    {
        return this.hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public String getFilename()
    {
        return this.filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public byte[] getFileContent()
    {
        return this.fileContent;
    }

    public void setFileContent(byte[] fileContent)
    {
        this.fileContent = fileContent;
    }
}