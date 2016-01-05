package br.ufrgs.inf.control.control;

import android.content.SharedPreferences;
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient extends Thread {

    public Socket socket;
    public DataOutputStream out;
    public BufferedReader in;
    public MainActivity activity;

    public void run() {
        try {
            Log.d("TCP", "Conectando...");
            BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            InetAddress serverAddr = InetAddress.getByName(MainActivity.config.getString("ip", ""));
            socket = new Socket(serverAddr, MainActivity.config.getInt("port", 0));
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Log.d("TCP", "Conectado");
            while(true) {

                byte b[] = activity.dataToSend();
                Log.d("tcp", "asd");
                out.write(b);

                //out.flush();
                //activity.dataToSend();
                Log.d("tcp", "1"+socket.isClosed());
                Log.d("tcp", "2"+socket.isConnected());
                Log.d("tcp", "3"+socket.isBound());
                Log.d("tcp", "4"+socket.isOutputShutdown());
                Log.d("tcp", "5"+socket.isInputShutdown());

                sleep(5);
            }
            //modifiedSentence = inFromServer.readLine();
            //socket.close();

        } catch (Exception e) {

            // Log.e("TCP", "C: Error", e);

        }
    }

}