package app.tez.com.takes.block.Models;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by serhat on 3.05.2018.
 */

public class OturumuAcan implements Serializable {
    private String email;
    private String adsoyad;
    private String fromIP;
    private int port;

    public OturumuAcan() {
        this.email = email;
        this.adsoyad = adsoyad;
        this.fromIP = fromIP;
        this.port = port;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdsoyad() {
        return adsoyad;
    }

    public void setAdsoyad(String adsoyad) {
        this.adsoyad = adsoyad;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFromIP() {
        return fromIP;
    }

    public void setFromIP(String fromIP) {
        this.fromIP = fromIP;
    }

    @Override
    public String toString() {
        String stringRep = (new Gson()).toJson(this);
        return stringRep;
    }

    public static OturumuAcan fromJSON(String jsonRep) {
        Gson gson = new Gson();
        OturumuAcan oturumuAcan = gson.fromJson(jsonRep, OturumuAcan.class);
        return oturumuAcan;
    }

}
