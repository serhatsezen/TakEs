package app.tez.com.takes.block.socket;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;

import app.tez.com.takes.R;
import app.tez.com.takes.block.CheckForSDCard;

public class ClientAct extends Activity {

	EditText editTextAddress;
	Button buttonConnect, server;
	TextView textPort;

	static final int SocketServerPORT = 8080;

	private static File fileDirectory = null;//Main Directory File

	private static final String DirectoryName = "TakES";//Main Directory Name
	private static final String FileName = "veritabani.json";//Text File Name


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_socket);

		editTextAddress = (EditText) findViewById(R.id.address);
		textPort = (TextView) findViewById(R.id.port);
		textPort.setText("port: " + SocketServerPORT);
		buttonConnect = (Button) findViewById(R.id.connect);
		server = (Button) findViewById(R.id.server);

		if (new CheckForSDCard().isSDCardPresent()) {
			fileDirectory = new File(
					Environment.getExternalStorageDirectory() + "/"
							+ DirectoryName);
		} else
			Toast.makeText(ClientAct.this, "SD Card is not present.", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

		server.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent server = new Intent(ClientAct.this, ServerSide.class);
				startActivity(server);
			}
		});
		buttonConnect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ClientRxThread clientRxThread =
						new ClientRxThread(
								editTextAddress.getText().toString(),
								SocketServerPORT);

				clientRxThread.start();
			}});
	}

	public class ClientRxThread extends Thread {
		String dstAddress;
		int dstPort;

		public ClientRxThread(String address, int port) {
			dstAddress = address;
			dstPort = port;
		}

		@Override
		public void run() {
			Socket socket = null;

			try {
				socket = new Socket(dstAddress, dstPort);

				File file = new File(fileDirectory.getAbsolutePath() + "/" + FileName);

				byte[] bytes = new byte[1024];
				InputStream is = socket.getInputStream();
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				int bytesRead;
				while ((bytesRead = is.read(bytes)) > 0) {
					bos.write(bytes, 0, bytesRead);
				}
				bos.close();
				socket.close();

				ClientAct.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(ClientAct.this,
								"Finished",
								Toast.LENGTH_LONG).show();
					}});

			} catch (IOException e) {

				e.printStackTrace();

				final String eMsg = "Something wrong: " + e.getMessage();
				ClientAct.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(ClientAct.this,
								eMsg,
								Toast.LENGTH_LONG).show();
					}});

			} finally {
				if(socket != null){
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}