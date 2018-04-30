package app.tez.com.takes.block;

/**
 * Created by serhat on 22.04.2018.
 */


import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    public String mail;
    public String adsoyad;
    public String ipaddress;
    public long timeStamp; //as number of milliseconds since 1/1/1970.
    public int nonce;


    //Block Constructor.
    public Block(String mail, String adsoyad, String ipaddress , String previousHash ) {
        this.mail = mail;
        this.adsoyad = adsoyad;
        this.ipaddress = ipaddress;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash(); //Making sure we do this after we set the other values.
    }

    //Calculate new hash based on blocks contents
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        mail +
                        adsoyad +
                        ipaddress
        );
        return calculatedhash;
    }

    //Increases nonce value until hash target is reached.
    public void mineBlock(int difficulty) {
        String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
    }

}