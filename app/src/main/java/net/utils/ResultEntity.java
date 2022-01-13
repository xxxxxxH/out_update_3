package net.utils;

import java.io.Serializable;

public class ResultEntity implements Serializable {
    String status;
    String ukey;
    String pkey;
    String ikey;
    String path;
    String oPack;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUkey() {
        return ukey;
    }

    public void setUkey(String ukey) {
        this.ukey = ukey;
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public String getIkey() {
        return ikey;
    }

    public void setIkey(String ikey) {
        this.ikey = ikey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getoPack() {
        return oPack;
    }

    public void setoPack(String oPack) {
        this.oPack = oPack;
    }
}
