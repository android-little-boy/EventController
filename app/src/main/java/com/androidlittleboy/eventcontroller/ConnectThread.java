package com.androidlittleboy.eventcontroller;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;

/**
 * 连接线程
 */
public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private Socket socket;
    private Handler handler;
    private DataInputStream mmInStream;
    private DataOutputStream mmOutStream;

    public ConnectThread(Socket socket, Handler handler) {
        setName("ConnectThread");
        Log.i("ConnectThread", "ConnectThread");
        Log.d(TAG, "socket Ip: " + socket.getInetAddress());
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
/*        if(activeConnect){
//            socket.c
        }*/
        if (socket == null) {
            return;
        }
        handler.sendEmptyMessage(WifiHotspot.DEVICE_CONNECTED);
        int length;
        try {
            //获取数据流
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            mmInStream = new DataInputStream(inputStream);
            mmOutStream = new DataOutputStream(outputStream);

            while (true) {

                if (isInterrupted()) {
                    cancel();
                    break;
                }

                //读取数据
                length = mmInStream.readInt();
                byte[] data = new byte[length];
                int len = 0;
                while (len < length) {
                    len = len + mmInStream.read(data, len, length - len);
                }
                Message message = Message.obtain();
                message.what = WifiHotspot.GET_MSG;
                Bundle bundle = new Bundle();
                bundle.putInt("length", length);
                bundle.putByteArray("data", data);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
        handler = null;
        mmInStream = null;
        mmOutStream = null;
    }

    void sendEvent(int type, int x, int y) {
        String event = "" + type + ";" + x + ";" + y;
        try {
            // type 蓝牙数据的类别 length：数据长度 buffer 数据
            mmOutStream.writeInt(2);
            mmOutStream.writeUTF(event);
            // Share the sent message back to the UI Activity
            Message message = Message.obtain();
            message.what = WifiHotspot.SEND_MSG_SUCCSEE;
            Bundle bundle = new Bundle();
            bundle.putString("MSG", "type:2");
            message.setData(bundle);
            handler.sendMessage(message);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write:" + e.toString());
        }
    }

    /**
     * 发送数据
     */
    void write(byte[] buffer) {
        try {
            // type 蓝牙数据的类别 length：数据长度 buffer 数据
            mmOutStream.writeInt(1);
            mmOutStream.writeInt(buffer.length);
            mmOutStream.write(buffer);

            // Share the sent message back to the UI Activity
            Message message = Message.obtain();
            message.what = WifiHotspot.SEND_MSG_SUCCSEE;
            Bundle bundle = new Bundle();
            bundle.putString("MSG", "type：1");
            message.setData(bundle);
            handler.sendMessage(message);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write:" + e.toString());
        }
    }

}

