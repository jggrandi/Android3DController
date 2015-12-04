package br.ufrgs.inf.control.control;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage;
    public static final String SERVERIP = "143.54.13.40"; //your computer IP address
    public static final int SERVERPORT = 8002;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    BufferedReader in;

    public OutputStream out;
    public DataOutputStream dos;

    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

   /* public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }
*/
    public void sendBytes(byte[] myByteArray) throws IOException {
        sendBytes(myByteArray, 0, myByteArray.length);
    }

    public void sendBytes(byte[] myByteArray, int start, int len) throws IOException {
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        if(dos == null || out == null) return;
        //dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }
    public Socket socket;
    public void stopClient(){
        mRun = false;
    }

    public void run() {
        while(true) {
            mRun = true;

            try {
                //here you must put your computer's IP address.
                InetAddress serverAddr = InetAddress.getByName(SERVERIP);

                Log.e("TCP Client", "C: Connecting...");

                //create a socket to make the connection with the server
                socket = new Socket(serverAddr, SERVERPORT);

                try {

                    //send the message to the server
                    out = socket.getOutputStream();
                    dos = new DataOutputStream(out);

                    Log.e("TCP Client", "C: Sent.");

                    Log.e("TCP Client", "C: Done.");

                    //receive the message which the server sends back
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    //in this while the client listens for the messages sent by the server
                    while (mRun) {
                        serverMessage = in.readLine();
                        if(serverMessage == null) break;

                        if (serverMessage != null && mMessageListener != null) {
                            //call the method messageReceived from MyActivity class
                            mMessageListener.messageReceived(serverMessage);
                        }
                        serverMessage = null;

                    }

                    Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

                } catch (Exception e) {

                    //Log.e("TCP", "S: Error", e);

                } finally {
                    //the socket must be closed. It is not possible to reconnect to this socket
                    // after it is closed, which means a new socket instance has to be created.
                    socket.close();
                }

            } catch (Exception e) {

               // Log.e("TCP", "C: Error", e);

            }
            try {
                Thread.sleep(100);
            }catch(InterruptedException e){

            }
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}