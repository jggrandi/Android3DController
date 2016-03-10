package br.ufrgs.inf.control.control;

import android.content.SharedPreferences;
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient extends Thread {

    public MainActivity activity;
    public volatile int count = 0;

    public class SendThread extends Thread {
        public Socket socket;
        public DataOutputStream out;
        public BufferedReader in;
        public void run() {
            try {
                activity.setConnected(false);
                Log.d("TCP", "Conectando...");
                InetAddress serverAddr = InetAddress.getByName(MainActivity.config.getString("ip", ""));
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddr, MainActivity.config.getInt("port", 0)), 1000);
                out = new DataOutputStream(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                activity.setConnected(true);
                Log.d("TCP", "Conectado");
                while(true) {
                    out.write(activity.dataToSend());
                    //Log.d("TCPasd", "recebendo");
                    //Log.d("TCPasd", in.readLine());
                    count++;
                    //Log.d("TCP", "Enviado");
                    sleep(5);
                }

            } catch (Exception e) {

                // Log.e("TCP", "C: Error", e);

            }
        }
    }

    SendThread sendThread;

    public void run() {
        try {
            while (true) {
                sendThread = new SendThread();
                sendThread.start();
                sleep(1000);
                while(true){
                    int c = count;
                    sleep(1000);
                    if(c == count) break;
                }
                Log.d("TCP", "Desconectado");
                if(sendThread.socket != null) sendThread.socket.close();
                sendThread.interrupt();
                sleep(100);
            }
        } catch (Exception e){

        }

    }

}