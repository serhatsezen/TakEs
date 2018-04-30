package app.tez.com.takes.block.Models;

import com.google.gson.Gson;

/**
 * Created by serhat on 28.04.2018.
 */

public class KayitBilgisiDTO {

    private String email;
    private String adsoyad;
    private String fromIP;
    private int port;

    public KayitBilgisiDTO() {
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

    public static KayitBilgisiDTO fromJSON(String jsonRep) {
        Gson gson = new Gson();
        KayitBilgisiDTO kayitBilgiObjesi = gson.fromJson(jsonRep, KayitBilgisiDTO.class);
        return kayitBilgiObjesi;
    }


}
