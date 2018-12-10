package tw.edu.tut.mis.demo20181210socket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UDPReceiveThread xxx = new UDPReceiveThread();
        xxx.start();

        UDPSendRunnable ooo = new UDPSendRunnable();
        ooo.IP = "192.168.64.255";
        ooo.Port = 9527;
        ooo.Message = "我要發出去的訊息";
        new Thread(ooo).start();
    }

    //android要求網路傳輸不能在主要執行緒中
    //建立一個用來接收UDP封包的執行緒
    class UDPReceiveThread extends Thread {
        boolean keepLoop = true;
        @Override
        public void run() {
            super.run();

            try( DatagramSocket socket = new DatagramSocket(9527) ){
                byte[] buffer = new byte[1024]; //大小自己訂
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(keepLoop) {
                    socket.receive(packet);  //阻塞式等待
                    String s = new String(packet.getData(), 0, packet.getLength());
                    Log.d("DEMO UDP", s);
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

