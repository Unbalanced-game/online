import java.io.*;
import java.net.*;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

public class Transfer{
    Steuerung dieSteuerung;UDPClient send;boolean failed;boolean close=false;
    public Transfer(String mode, Steuerung pDieSteuerung){
        dieSteuerung=pDieSteuerung;
        send = new UDPClient(mode);
        Transfer transf=this;
        Thread udpServerThread = new Thread() {
                public void run() {
                    try{
                        UDPServer receive = new UDPServer(transf);
                    }catch(Exception e){
                        System.out.println(e);
                    }
                }  
            };
        udpServerThread.start();
        try{udpServerThread.sleep(200);}catch(Exception e){}
    }

    public void sendToServer(String message){
        send.send(message);
    }

    public void sendToClient(String ip, String message){
        send.send(ip,message);
    }

    public void receive(String message, String ip){
        message=message.replace("\u0000", "");
        dieSteuerung.recieve(ip,message);
    }

    public void waitMilliSeconds(int time){
        try{TimeUnit.MILLISECONDS.sleep(time);}catch(Exception e){System.out.println(e);}
    }

    public void close(){
        close=true;
    }

    public void scheduleSendToClient(String ip, String message,int ms){
        Thread sendThread = new Thread() {
                public void run() {
                    waitMilliSeconds(ms);
                    send.send(ip,message);
                }  
            };
        sendThread.start();
    }

    class UDPServer{
        private Transfer transf;
        public UDPServer(Transfer pTransf){
            transf=pTransf;DatagramSocket serverSocket;
            try{
                serverSocket = new DatagramSocket(9878);ready(serverSocket);
            }catch(Exception e){
                failed=true;System.out.println(e);
            }
        }

        private void ready(DatagramSocket serverSocket){
            byte[] receiveData = new byte[65000];
            transf.waitMilliSeconds(1000);
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try{serverSocket.receive(receivePacket);}catch(Exception e1){}
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                if(sentence!=null){
                    transf.receive(sentence,String.valueOf(receivePacket.getAddress()).replaceAll("/", ""));
                }
                receiveData = new byte[65000];
                if(transf.close){serverSocket.close();}
            }
        }
    }
    class UDPClient{
        String serverip=getIP();
        public UDPClient(String mode){
            tryToConnect(mode);
        }

        public void send(String message){
            try{
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(serverip);
                byte[] sendData = new byte[65000];
                sendData = message.getBytes();
                DatagramPacket sendPacket;
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9878);
                clientSocket.send(sendPacket);
                clientSocket.close();
            }catch(Exception e){System.out.println(e);}
        }

        public void send(String clientip,String message){
            try{
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(clientip);
                byte[] sendData = new byte[65000];
                sendData = message.getBytes();
                DatagramPacket sendPacket;
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9878);
                clientSocket.send(sendPacket);
                clientSocket.close();
            }catch(Exception e){System.out.println(e);}
        }

        private String getIP(){
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                return socket.getLocalAddress().getHostAddress();
            }catch(Exception e){}
            return "";
        }

        public void tryToConnect(String mode){
            serverip=getIP();
            if((mode.toLowerCase().equals("id"))){
                String input=textInput("Your ID: "+serverip.split("\\.")[3]+"\nEnter the ID of the partner-computer:");
                serverip=serverip.split("\\.")[0]+"."+serverip.split("\\.")[1]+"."+serverip.split("\\.")[2]+"."+input;
                try{
                    if(!input.matches("\\d\\d\\d|\\d\\d|\\d")){
                        waitMilliSeconds(4000);failed=true;
                    }
                }catch(Exception e){failed=true;}
            }else if(mode.toLowerCase().equals("ip")){
                serverip=textInput("Your IP: "+serverip+"\nEnter the IP of the partner-computer:");
                try{
                    if(!serverip.matches("\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")){
                        waitMilliSeconds(4000);failed=true;
                    }
                }catch(Exception e){failed=true;}
            }else if(mode.toLowerCase().equals("auto")){
                String ip[]=serverip.split("\\.");
                for(int i=0;i<256;i++){
                    serverip=ip[0]+"."+ip[1]+"."+ip[2]+"."+i;
                    send("gm--connect"+dieSteuerung.username);
                }
            }
        }

        public String textInput(String message){
            return JOptionPane.showInputDialog(message);
        }
    }
}
