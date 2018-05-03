package app.tez.com.takes.block.Block;

import android.util.Log;

import java.util.Date;

import app.tez.com.takes.block.StringUtil;

/**
 * Created by serhat on 1.05.2018.
 */

public class BlockPaylasim {

    public String hash;
    public String previousHash;
    public String mail;
    public String icerik;
    public String ipaddress;
    public long timeStamp; //as number of milliseconds since 1/1/1970.
    public int nonce;
    public String sifre;


    //Block Constructor.
    public BlockPaylasim(String mail, String icerik ,String ipaddress, String previousHash) {
        this.mail = mail;
        this.ipaddress = ipaddress;
        this.icerik = icerik;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHashPaylasim(); //Making sure we do this after we set the other values.
    }

    //Calculate new hash based on blocks contents
    public String calculateHashPaylasim() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        mail +
                        icerik +
                        ipaddress
        );
        return calculatedhash;
    }

    //Increases nonce value until hash target is reached.
    public void mineBlockPaylasim(int difficulty) {
        String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHashPaylasim();
            Log.v("hash", hash);
        }
    }


}
