package app.tez.com.takes.block.socket;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import app.tez.com.takes.R;
import app.tez.com.takes.block.CheckForSDCard;

public class ServerSide extends Activity {

	TextView infoIp, infoPort;

	static final int SocketServerPORT = 8080;
	ServerSocket serverSocket;

	ServerSocketThread serverSocketThread;

	private static File fileDirectory = null;//Main Directory File
	private static final String DirectoryName = "TakES";//Main Directory Name
	private static final String FileName = "veritabani.json";//Text File Name

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_server_side);
		infoIp = (TextView) findViewById(R.id.infoip);
		infoPort = (TextView) findViewById(R.id.infoport);

		if (new CheckForSDCard().isSDCardPresent()) {
			fileDirectory = new File(
					Environment.getExternalStorageDirectory() + "/"
							+ DirectoryName);
		} else
			Toast.makeText(ServerSide.this, "SD Card is not present.", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted


		infoIp.setText(getIpAddress());

		serverSocketThread = new ServerSocketThread();
		serverSocketThread.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String getIpAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (enumNetworkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = enumNetworkInterfaces
						.nextElement();
				Enumeration<InetAddress> enumInetAddress = networkInterface
						.getInetAddresses();
				while (enumInetAddress.hasMoreElements()) {
					InetAddress inetAddress = enumInetAddress.nextElement();

					if (inetAddress.isSiteLocalAddress()) {
						ip += "SiteLocalAddress: "
								+ inetAddress.getHostAddress() + "\n";
					}

				}

			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ip += "Something Wrong! " + e.toString() + "\n";
		}

		return ip;
	}

	public class ServerSocketThread extends Thread {

		@Override
		public void run() {
			Socket socket = null;

			try {
				serverSocket = new ServerSocket(SocketServerPORT);
				ServerSide.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						infoPort.setText("I'm waiting here: "
								+ serverSocket.getLocalPort());
					}});

				while (true) {
					socket = serverSocket.accept();
					FileTxThread fileTxThread = new FileTxThread(socket);
					fileTxThread.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (socket != null) {
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

	public class FileTxThread extends Thread {
		Socket socket;

		FileTxThread(Socket socket){
			this.socket= socket;
		}

		@Override
		public void run() {

			File file = new File(fileDirectory.getAbsolutePath() + "/" + FileName);

			byte[] bytes = new byte[(int) file.length()];
			BufferedInputStream bis;
			OutputStream os;
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				bis.read(bytes, 0, bytes.length);
				os = socket.getOutputStream();
				os.write(bytes, 0, bytes.length);
				os.flush();
				socket.close();

				final String sentMsg = "File sent to: " + socket.getInetAddress();
				ServerSide.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(ServerSide.this,
								sentMsg,
								Toast.LENGTH_LONG).show();
					}});

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
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