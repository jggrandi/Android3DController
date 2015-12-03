package br.ufrgs.inf.control.control;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private TextView text;
    private SensorManager mSensorManager;
    private TCPClient tcp;

    private float[] originMatrix = new float[16];
    private float[] directionMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    private float[] rotationMatrixPrev = new float[16];
    private float[] rotationMatrixStep = new float[16];

    float[] translation = new float[4];
    private float[] translationMatrix = new float[16];

    private float[] touchPosition = new float[2];

    private float[] scaleMatrix = new float[16];
    private float scale = 1.f;

    boolean translationActive;
    boolean rotationActive;

    private ScaleGestureDetector mScaleDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.text);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
        new connectTask().execute("");

        Matrix.setIdentityM(originMatrix, 0);
        Matrix.setIdentityM(directionMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(rotationMatrixPrev,0);
        Matrix.setIdentityM(rotationMatrixStep,0);
        Matrix.setIdentityM(translationMatrix,0);
        Matrix.setIdentityM(scaleMatrix,0);
        translation = new float[]{0,0,0,1};

        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing.
    }


    public byte [] float2ByteArray (float matrix[])
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 * matrix.length);
        for(int i=0; i<matrix.length; i++) buffer.putFloat(matrix[i]);

        byte r[] = buffer.array();
        for(int i=0; i<matrix.length; i++) {
            byte b = r[i*4 + 0];
            r[i*4 + 0] = r[i*4 + 3];
            r[i*4 + 3] = b;
            b = r[i*4 + 1];
            r[i*4 + 1] = r[i*4 + 2];
            r[i*4 + 2] = b;
        }
        return r;
    }

    public String matrix2String(float m[]){
        return String.format("%.2f %.2f %.2f %.2f \n%.2f %.2f %.2f %.2f \n%.2f %.2f %.2f %.2f \n%.2f %.2f %.2f %.2f \n",
                m[0], m[4], m[8], m[12], m[1], m[5], m[9], m[13], m[2], m[6], m[10], m[14], m[3], m[7], m[11], m[15]);
    }
    int asd = 10;
    public void sendData(){
        text.setText("");

        if(rotationActive) {
            Matrix.invertM(rotationMatrixStep, 0, rotationMatrixPrev, 0);
            Matrix.multiplyMM(rotationMatrixStep, 0, rotationMatrix, 0, rotationMatrixStep.clone(), 0);
        }else{
            Matrix.setIdentityM(rotationMatrixStep,0);
        }

        Matrix.setIdentityM(translationMatrix, 0);
        translationMatrix[12] = translation[0] * 0.003f;
        translationMatrix[13] = translation[1] * 0.003f;
        translationMatrix[14] = translation[2] * 0.003f;
        translation[0] = translation[1] = translation[2] = 0;


        Matrix.setIdentityM(scaleMatrix, 0);
        scale = (scale + 1.0f)/2.0f;
        scaleMatrix[0] = scaleMatrix[5] = scaleMatrix[10] = scale;
        scale = 1.0f;

        text.append(matrix2String(rotationMatrixStep)+"\n");
        text.append(matrix2String(translationMatrix)+"\n");
        text.append(matrix2String(scaleMatrix)+"\n");
        text.append(matrix2String(rotationMatrix)+"\n");

        byte a[] = float2ByteArray(rotationMatrixStep);
        byte b[] = float2ByteArray(translationMatrix);
        byte c[] = float2ByteArray(scaleMatrix);
        byte d[] = float2ByteArray(rotationMatrix);
        byte[] packet = new byte[a.length + b.length + c.length + d.length];
        System.arraycopy(a, 0, packet, 0, a.length);
        System.arraycopy(b, 0, packet, a.length, b.length);
        System.arraycopy(c, 0, packet, a.length + b.length, c.length);
        System.arraycopy(d, 0, packet, a.length + b.length + c.length, d.length);

        try {
            if(asd--<0){
                tcp.sendBytes(packet);
                if(tcp.socket == null || !tcp.socket.isConnected()){
                    text.append("Conectando...\n");
                }else{
                    text.append("Conectado\n");
                }
                if(tcp.dos != null){
                    text.append("Tamanho Saída TCP: "+tcp.dos.size()+"\n");
                }
            }
        }catch (IOException e) {

        }
        rotationMatrixPrev = rotationMatrix.clone();

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;

        SensorManager.getRotationMatrixFromVector(directionMatrix, event.values);
        Matrix.invertM(directionMatrix, 0, directionMatrix.clone(), 0);

        float [] r = new float[]{
                -1,0,0,0,
                0,0,-1,0,
                0,-1,0,0,
                0,0,0,1
        };

        Matrix.multiplyMM(rotationMatrix,0,directionMatrix.clone(),0,r,0);

        Matrix.multiplyMM(rotationMatrix,0,originMatrix,0,directionMatrix,0);

        sendData();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            Matrix.invertM(originMatrix, 0, directionMatrix,0);
            return true;
        }

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            switch (action) {
                case KeyEvent.ACTION_DOWN:
                    rotationMatrixPrev = rotationMatrix.clone();
                    rotationActive = true;
                    return true;
                case KeyEvent.ACTION_UP:
                    rotationActive = false;
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        mScaleDetector.onTouchEvent(event);
        if(mScaleDetector.isInProgress()) return true;

        int action = MotionEventCompat.getActionMasked(event);
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :

                touchPosition[0] = event.getX();
                touchPosition[1] = event.getY();
                translation[0] = translation[1] = translation[2] = 0;
                translationActive = true;

                return true;
            case (MotionEvent.ACTION_MOVE) :
                if(event.getPointerCount() > 1) {
                    touchPosition[0] = event.getX();
                    touchPosition[1] = event.getY();
                    translationActive = false;
                    return true;
                }
                if(translationActive){
                    float[] t = new float[]{-(event.getX()-touchPosition[0]),event.getY()-touchPosition[1],0,1};
                    Matrix.multiplyMV(t,0,rotationMatrix,0,t,0);
                    translation[0] += t[0];
                    translation[1] += t[1];
                    translation[2] += t[2];
                    touchPosition[0] = event.getX();
                    touchPosition[1] = event.getY();
                }
                return true;
            case (MotionEvent.ACTION_UP) :
                translationActive = false;
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }

    public class connectTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            tcp = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            tcp.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            Log.d(values[0], values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
        }
    }




    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();


            //invalidate();
            return true;
        }
    }

}
