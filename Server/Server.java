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
import java.util.ArrayList;
import java.util.HashMap;


public class Server{
    static int frameSize = 256;
    private static int numUser = 0;
    
    final int receiving_port = 8000; 
    final int replying_port = 8080;
    
    DatagramSocket serverSocket;
    DatagramSocket replyingSocket;
    
    private String options;
    private HashMap<InetAddress, HashMap<Integer, StreamingThread>> map;
    
    
    
    public Server() throws SocketException{
        serverSocket = new DatagramSocket(receiving_port);
        replyingSocket = new DatagramSocket(replying_port);
        
        map = new HashMap<InetAddress, HashMap<Integer, StreamingThread>>();
       
        options = new String("Welcome to the Server\nPress 1 for seeing L1.mp4\nPress 2 for seeing L2.mp4");
    }
    
    
    public static void main(String args[]) throws SocketException, IOException, InterruptedException{
        Server server = new Server();
        byte dataReceived[];
        
        HashMap<InetAddress, HashMap<Integer, StreamingThread>> map = new HashMap<InetAddress, HashMap<Integer, StreamingThread>>();
        
        String code_1 = new String("Connection Request");
        while(true){
            dataReceived = new byte[frameSize];
            DatagramPacket initialRecvPack;
            initialRecvPack = new DatagramPacket(dataReceived, dataReceived.length);
            server.serverSocket.receive(initialRecvPack);
            
            
            String message = new String(initialRecvPack.getData()).trim();
  
            
            InetAddress inetAddr = initialRecvPack.getAddress();
            int portAddr = initialRecvPack.getPort();
            System.out.println(inetAddr + " " + portAddr);
            
            
            if(message.equals(code_1)){
                server.addUser(portAddr, inetAddr);
            }else{
                server.startSharing(message, inetAddr, portAddr);
            }
        }
        
        // serverSocket.close();
        
    }
    
    
    private void addUser(int portAddr, InetAddress inetAddr) throws IOException {
        numUser++;
        StreamingThread stream = null;
                
                
        HashMap<Integer, StreamingThread> userGroup;
        if(map.containsKey(inetAddr)){
            userGroup = map.get(inetAddr);
        }else{
            userGroup = new HashMap<Integer, StreamingThread>();
        }
        
        
        userGroup.put(portAddr, stream);
        map.put(inetAddr, userGroup);
        
        
        sendOptions(portAddr, inetAddr);
    }
    
    
    private void sendOptions(int portAddr, InetAddress inetAddr) throws IOException{
        byte dataSent[] = new byte[frameSize]; 
        dataSent = options.getBytes();
        

        DatagramPacket initialResponse;
        initialResponse= new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(initialResponse);
    }
    
    
    private void startSharing(String message, InetAddress inetAddr, int portAddr){
        HashMap<Integer, StreamingThread> userGroup = map.get(inetAddr);
        
        
        StreamingThread stream = userGroup.get(portAddr);
        stream = new StreamingThread(numUser, replyingSocket, portAddr, inetAddr, message);
        
        
        userGroup.put(portAddr, stream);
        map.put(inetAddr, userGroup);
        
        
        stream.start();
    }
}


class StreamingThread extends Thread{
    private final int client_id;
    DatagramSocket replyingSocket;
    private final InetAddress inetAddr;
    private final int portAddr;
    private String choice;
    private Stream newStream;
    
    public StreamingThread(int client_id, DatagramSocket replying_socket, int portAddr,InetAddress inetAddr, String choice){
        this.client_id = client_id;
        this.replyingSocket = replying_socket;
        this.portAddr = portAddr;
        this.inetAddr = inetAddr;
        this.choice = choice;
    }
    
    @Override
    public void run() {
        super.run(); //To change body of generated methods, choose Tools | Templates.
        newStream = new Stream(replyingSocket, client_id);
        try{
            newStream.startStreaming(portAddr, inetAddr,choice);
        }catch(IOException ex) {
            System.out.println("IOException occured while creating Streaming Thread");
        }
    } 
}
