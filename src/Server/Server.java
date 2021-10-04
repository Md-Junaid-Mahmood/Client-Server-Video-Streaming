package Server;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author md
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;


public class Server implements Network.NetworkInterface{
    private static int frameSize = Network.NetworkInterface.frameSize;
    private static int numUser = 0;
    
    private static final int receiving_port = Network.NetworkInterface.receiving_port; 
    private static final int replying_port = Network.NetworkInterface.replying_port;
    private static final String serverAddress = Network.NetworkInterface.serverAddress;
    
    private DatagramSocket serverSocket;
    private DatagramSocket replyingSocket;
    
    private final String options;
    private HashMap<InetAddress, HashMap<Integer, Stream>> map;
    private HashMap<Stream, Integer> choiceMap;
    
    
    
    public Server() throws SocketException, UnknownHostException{
        serverSocket = new DatagramSocket(receiving_port);
        replyingSocket = new DatagramSocket(replying_port);
        
        /*
        InetAddress address = InetAddress.getByName(serverAddress);
        serverSocket.connect(address, receiving_port);
        replyingSocket.connect(address, replying_port);
        */
        
        map = new HashMap<InetAddress, HashMap<Integer, Stream>>();
        choiceMap = new HashMap<Stream, Integer>();
     
        options = buildOptions();
    }
    
    
    public static void main(String args[]) throws SocketException, IOException, InterruptedException{
        Server server = new Server();
        System.out.println("Server has started..." + "\n");
        byte dataReceived[];

        
        String code_1 = new String("Connection Request");
        String code_2 = new String("Start Sending");
        String code_3 = new String("Next ");
        String code_4 = new String("Choice ");
        String code_5 = new String("Terminate");
        
        
        while(true){
            dataReceived = new byte[frameSize];
            DatagramPacket initialRecvPack;
            initialRecvPack = new DatagramPacket(dataReceived, dataReceived.length);
            server.serverSocket.receive(initialRecvPack);
            
            
            String message = new String(initialRecvPack.getData()).trim();
  
            
            InetAddress inetAddr = initialRecvPack.getAddress();
            int portAddr = initialRecvPack.getPort();
            
            
            if(message.equals(code_1)){
                System.out.print("Adding User->");
                System.out.println(" Request From:" + inetAddr + " " + portAddr + "\n");
                
                server.addUser(portAddr, inetAddr);    
            }
            else if(message.equals(code_2)){
                System.out.print("Starting Stream->");
                System.out.println(" Request From:" + inetAddr + " " + portAddr + "\n");
                
                server.startSharing(inetAddr, portAddr);
            }
            else if(message.substring(0, 5).equals(code_3)){
                System.out.print("Streaming Next Video->");
                System.out.println(" Request From:" + inetAddr + " " + portAddr + "\n");
                
                server.processNextRequest(message, portAddr, inetAddr);
                server.sendConfirmation(portAddr, inetAddr);
            }
            else if(message.substring(0, 7).equals(code_4)){
                System.out.print("Accepting Choice->");
                System.out.println(" Request From:" + inetAddr + " " + portAddr + "\n");
                
                int choice = server.acceptChoice(message);
                server.putChoice(choice, portAddr, inetAddr);
                server.sendConfirmation(portAddr, inetAddr);
            }
            else if(message.equals(code_5)){
                System.out.print("Terminating Connection->");
                System.out.println(" Request From:" + inetAddr + " " + portAddr + "\n");
                server.terminateRequest(portAddr, inetAddr);
            }
        }
        
        // serverSocket.close();
        
    }
    
    
    private void addUser(int portAddr, InetAddress inetAddr) throws IOException{
        numUser++;
        Stream stream = null;
                
                
        HashMap<Integer, Stream> userGroup;
        if(map.containsKey(inetAddr)){
            userGroup = map.get(inetAddr);
        }else{
            userGroup = new HashMap<Integer, Stream>();
        }
        
        
        userGroup.put(portAddr, stream);
        map.put(inetAddr, userGroup);
        
        
        sendOptions(portAddr, inetAddr);
        sendDirInfo(portAddr, inetAddr);
    }
    
    
    private void sendOptions(int portAddr, InetAddress inetAddr) throws IOException{
        byte dataSent[] = new byte[frameSize]; 
        dataSent = options.getBytes();
        

        DatagramPacket initialResponse;
        initialResponse= new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(initialResponse);
    }
    
    
    private void sendDirInfo(int portAddr, InetAddress inetAddr) throws IOException{
        byte dataSent[] = new byte[frameSize]; 
        dataSent = Integer.toString(numUser).getBytes();
        

        DatagramPacket dirResponse;
        dirResponse = new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(dirResponse);
    }
    
    
    private void sendConfirmation(int portAddr, InetAddress inetAddr) throws IOException{
        byte dataSent[] = new byte[frameSize];
        String confirm = new String("OKAY");
        dataSent = confirm.getBytes();
        

        DatagramPacket confirmation;
        confirmation = new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(confirmation);
    }
    
    
    private void startSharing(InetAddress inetAddr, int portAddr) throws IOException, InterruptedException{
        HashMap<Integer, Stream> userGroup = map.get(inetAddr);
       
        
        Stream stream = userGroup.get(portAddr);
        int choice = choiceMap.get(stream);
        
        
        if(stream != null){
            stream.interrupt();
            stream = null;
            System.gc();
            
        }
        stream = new Stream(portAddr, inetAddr, choice, replyingSocket);
        
        
        userGroup.put(portAddr, stream);
        map.put(inetAddr, userGroup);

        
        stream.start();
    }

    
    private String buildOptions() {
        String options = new String("Welcome to the Server\n" + 
                                    "Press 1 for seeing L1.mp4\n" +
                                    "Press 2 for seeing L2.mp4\n" +
                                    "Press 3 for seeing L3.mp4\n");
        return options;
    }
    
    
    private int acceptChoice(String choice) throws IOException{
        int chosen = Integer.parseInt(choice.substring(7));
        return(chosen);
    }
    
    
    private void putChoice(int choice, int portAddr, InetAddress inetAddr){
        HashMap<Integer, Stream> userGroup;
        userGroup = map.get(inetAddr);
        
        
        Stream stream = userGroup.get(portAddr);
        choiceMap.put(stream, choice);
    }
    
    
    private void processNextRequest(String message, int portAddr, InetAddress inetAddr){
        int choice = Integer.parseInt(message.substring(5));
        
        
        HashMap<Integer, Stream> userGroup;
        userGroup = map.get(inetAddr);
        
        
        Stream stream = userGroup.get(portAddr);
        choiceMap.remove(stream);
        
        try{
            stream.interrupt();
        }catch(Exception e){
            // Do Nothing
        }
        
        Stream newStream = null;
        choiceMap.put(newStream, choice);
        userGroup.put(portAddr, newStream);
        map.put(inetAddr, userGroup);
    }
    
    
    private void terminateRequest(int portAddr, InetAddress inetAddr){
        HashMap<Integer, Stream> userGroup;
        userGroup = map.get(inetAddr);
        
        
        Stream stream = userGroup.get(portAddr);
        choiceMap.remove(stream);
        stream.interrupt();
        
        
        userGroup.remove(portAddr);
        map.put(inetAddr, userGroup);
    }
}
