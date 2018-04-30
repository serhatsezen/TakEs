package app.tez.com.takes.block.Models;

import android.os.Build;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by serhat on 28.04.2018.
 */

public class DeviceDTO implements Serializable {

    private String ip;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }


    @Override
    public String toString() {
        String stringRep = (new Gson()).toJson(this);
        return stringRep;
    }

    public static DeviceDTO fromJSON(String jsonRep) {
        Gson gson = new Gson();
        DeviceDTO deviceDTO = gson.fromJson(jsonRep, DeviceDTO.class);
        return deviceDTO;
    }
}
