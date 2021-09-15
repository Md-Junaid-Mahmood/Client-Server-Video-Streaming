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


public class Server{
    static int frameSize = 256;
    static final int receiving_port = 8000; 
    static final int replying_port = 8080;
    private static int numUser = 0;
            
    public static void main(String args[]) throws SocketException, IOException, InterruptedException{
        DatagramSocket serverSocket = new DatagramSocket(receiving_port);
        byte dataReceived[] = new byte[frameSize];
        byte dataSent[] = new byte[frameSize];
       
        
        DatagramSocket replying_Socket;
        replying_Socket = new DatagramSocket(replying_port);
        
        
        while(true){
            DatagramPacket initialRecvPack;
            initialRecvPack = new DatagramPacket(dataReceived, dataReceived.length);
            serverSocket.receive(initialRecvPack);
            
            
            System.out.println(new String(initialRecvPack.getData()).trim());
  
            
            InetAddress inetAddr = initialRecvPack.getAddress();
            int portAddr = initialRecvPack.getPort();
            
            
            numUser++;
           
            
            Stream newStream;
            newStream = new Stream(replying_Socket, numUser);
            newStream.startStreaming(portAddr, inetAddr);
        }
        
        // serverSocket.close();
        
    }
}
