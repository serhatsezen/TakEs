package app.tez.com.takes.block;

/**
 * Created by serhat on 28.01.2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


import app.tez.com.takes.R;
import app.tez.com.takes.block.socket.ClientAct;

public class Register extends Activity {

    EditText name,mail;
    Button registerBtn;
    Button decrype,main;
    static final int READ_BLOCK_SIZE = 100;
    byte[] textEncrypted;
    byte[] textDecrypted;
    String laswrtie;
    String writeText;

    String edName;
    String edMail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerlay);

        name=(EditText)findViewById(R.id.etFirstName);
        mail=(EditText)findViewById(R.id.etMail);
        registerBtn=(Button) findViewById(R.id.registerBtn);
        decrype = (Button) findViewById(R.id.decrype);
        main = (Button) findViewById(R.id.mainact);

        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainAct = new Intent(Register.this, ClientAct.class);
                startActivity(mainAct);
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edName = name.getText().toString();
                edMail = mail.getText().toString();

                ReadBtn();

                if(laswrtie!=null ){
                    if(laswrtie.contains(edName)||laswrtie.contains(edMail)) {
                        Toast.makeText(Register.this, "Kullanıcı adı ve ya Mail Adresi Kullanlıyor", Toast.LENGTH_LONG).show();
                    }else{
                        WriteBtn();

                    }
                }else{
                    WriteBtn();
                }
            }
        });
    }

    // write text to file
    public void WriteBtn() {
        // add-write text into file
        try {

            encrypData(edName);

            FileOutputStream fileout=openFileOutput("database.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);

            if(laswrtie != null){
                writeText = laswrtie + " , "+ edName;
                writeText = laswrtie+"\n"+ textEncrypted+":\n{ \n \"name\":" + "\"" +edName+"\",\n"+" \"mail\": \""+ edMail +"\"\n},";
                outputWriter.write(writeText);
                outputWriter.close();
            }else{
                writeText = textEncrypted + ":\n{ \n \"name\":" + "\"" +edName+"\",\n"+" \"mail\": \""+ edMail +"\"\n},";
                outputWriter.write(writeText);
                outputWriter.close();

            }

            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read text from file
    public void ReadBtn() {
        //reading text from file
        try {
            FileInputStream fileIn=openFileInput("database.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            laswrtie = s;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encrypData(String name){
        try{

            KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
            SecretKey myDesKey = keygenerator.generateKey();

            Cipher desCipher;

            // Create the cipher
            desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            // Initialize the cipher for encryption
            desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);

            //sensitive information
            byte[] text = name.getBytes();

            // Encrypt the text
            textEncrypted = desCipher.doFinal(text);

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch(NoSuchPaddingException e){
            e.printStackTrace();
        }catch(InvalidKeyException e){
            e.printStackTrace();
        }catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }catch(BadPaddingException e){
            e.printStackTrace();
        }


    }
}