package com.androidlittleboy.eventcontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;

public class WifiHotspot {
    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED = 2;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE = 3;//发送消息成功
    public static final int SEND_MSG_ERROR = 4;//发送消息失败
    public static final int GET_MSG = 6;//获取新消息
    private WifiManager wifiManager;
    private final Handler workerHandler;
    private static final String TAG = "WifiHotspot";
    private Handler handler;
    private Callback mCallback;
    private final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTING:
                    connectThread = new ConnectThread(listenerThread.getSocket(), handler);
                    connectThread.start();
                    break;
                case DEVICE_CONNECTED:
                    Log.d(TAG, "设备连接成功");
                    break;
                case SEND_MSG_SUCCSEE:
                    Log.d(TAG, "发送消息成功:" + msg.getData().getString("MSG"));
                    break;
                case SEND_MSG_ERROR:
                    Log.d(TAG, "发送消息失败:" + msg.getData().getString("MSG"));
                    break;
                case GET_MSG:
                    byte[] b = msg.getData().getByteArray("data");
//                    Log.d(TAG, "handleMessage: "+b.length);
//                    ByteBuffer buf = ByteBuffer.wrap(b);
//                    Bitmap mBitmap = Bitmap.createBitmap(width ,height, Bitmap.Config.ARGB_8888);
//                    mBitmap.copyPixelsFromBuffer(buf);
                    Bitmap mBitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                    if (mCallback != null)
                        mCallback.onPreview(mBitmap);
                    Log.d(TAG, "收到消息:" + msg.getData().getInt("length"));
                    break;
            }
            return false;
        }
    };

    private WifiHotspot() {
        HandlerThread handlerThread = new HandlerThread("worker");
        handlerThread.start();
        workerHandler = new Handler(handlerThread.getLooper());
    }

    public static WifiHotspot getWifiHotspot() {
        return WifiHotspotFactory.wifiHotspot;
    }

    private static class WifiHotspotFactory {
        static WifiHotspot wifiHotspot = new WifiHotspot();
    }

    /**
     * 连接线程
     */
    private ConnectThread connectThread;

    /**
     * 监听线程
     */
    private ListenerThread listenerThread;

    /**
     * 热点名称
     */
    private static final String WIFI_HOTSPOT_SSID = "被控制者";
    /**
     * 端口号
     */
    private static final int PORT = 54321;

    public void init(Context context, Callback mCallback) {
        this.mCallback = mCallback;
        handler = new Handler(callback);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //检查Wifi状态
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(getWifiRouteIPAddress(context), PORT);
                    connectThread = new ConnectThread(socket, handler);
                    Log.d(TAG, "run: asdasdads");
                    connectThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "通信连接失败");

                }
            }
        }).start();

//        listenerThread = new ListenerThread(PORT, handler);
//        listenerThread.start();
    }

    public void unInit() {
        connectThread.interrupt();
        listenerThread.interrupt();
        workerHandler.getLooper().quit();
        connectThread = null;
        listenerThread = null;
        mCallback = null;
    }


    public void sendEvent(int type, int x, int y) {
        if (connectThread != null) {
            workerHandler.post(() -> connectThread.sendEvent(type, x, y));
        }
    }

    /**
     * wifi获取 已连接网络路由  路由ip地址
     *
     * @param context
     * @return
     */
    private static String getWifiRouteIPAddress(Context context) {
        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        //        WifiInfo wifiinfo = wifi_service.getConnectionInfo();
        //        System.out.println("Wifi info----->" + wifiinfo.getIpAddress());
        //        System.out.println("DHCP info gateway----->" + Formatter.formatIpAddress(dhcpInfo.gateway));
        //        System.out.println("DHCP info netmask----->" + Formatter.formatIpAddress(dhcpInfo.netmask));
        //DhcpInfo中的ipAddress是一个int型的变量，通过Formatter将其转化为字符串IP地址
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.i("route ip", "wifi route ip：" + routeIp);
        Log.d(TAG, "getWifiRouteIPAddress: " + routeIp);

        return routeIp;
    }

    public interface Callback {
        void onPreview(Bitmap bitmap);
    }

}
