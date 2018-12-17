package tw.edu.tut.mis.demo20181210socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    UDPReceiveThread xxx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xxx = new UDPReceiveThread();
        xxx.start();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xxx.getHandler().obtainMessage(1097).sendToTarget();
            }
        });

        UDPSendRunnable ooo = new UDPSendRunnable();
        ooo.IP = "192.168.64.255";
        ooo.Port = 9527;
        ooo.Message = "我要發出去的訊息";
        new Thread(ooo).start();
    }

    //
    static class MainActivityHandler extends Handler {
        //----------------------------------------
        private WeakReference<MainActivity> activity;

        MainActivityHandler(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }
        //----------------------------------------
        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = activity.get();

            if (msg.what==1988) { //編號是1988要做的事情
                //留意，要透過 mainActivity 取用 MainActivity的東西
                String s = (String)(msg.obj);
                ((TextView)(mainActivity.findViewById(R.id.textView))).setText(s);
            }
        }
    }

    Handler mMainHandler = new MainActivityHandler(this);





    //android要求網路傳輸不能在主要執行緒中
    //建立一個用來接收UDP封包的執行緒
    class UDPReceiveThread extends Thread {
        Handler mReceiveHandler;
        boolean keepLoop = true;

        Handler getHandler(){
            return mReceiveHandler;
        }

        @Override
        public void run() {
            super.run();

            Looper.prepare();
            mReceiveHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==1097) {  //如果收到 1097 號
                        keepLoop = false;
                    }
                }
            };
            Looper.loop();

            try( DatagramSocket socket = new DatagramSocket(9527) ){
                byte[] buffer = new byte[1024]; //大小自己訂
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(keepLoop) {
                    socket.receive(packet);  //阻塞式等待
                    String s = new String(packet.getData(), 0, packet.getLength());
                    Log.d("DEMO UDP", s);
                    mMainHandler.obtainMessage(1988, s).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class UDPSendRunnable implements Runnable {
        String Message;
        String IP;
        int Port;
        @Override
        public void run() {
            try( DatagramSocket socket = new DatagramSocket() ){
                byte[] buffer = Message.getBytes();
                InetAddress ip = InetAddress.getByName(IP);  //自己換IP
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, Port);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}//MainActivity

