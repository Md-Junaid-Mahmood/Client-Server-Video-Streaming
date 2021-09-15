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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


public class Stream{
    static DatagramSocket serverSocket;
    private static final ArrayList<String> list_Videos = new ArrayList<String>();
    int client_id;
    int frameSize = 256;
    String options;
    

    public Stream(DatagramSocket serverSocket, int client_id){
        Stream.serverSocket = serverSocket;
        this.client_id = client_id;
        list_Videos.add("/home/md/NetBeansProjects/Video Streaming/src/Server/Video/L1.mp4");
        list_Videos.add("/home/md/NetBeansProjects/Video Streaming/src/Server/Video/L2.mp4");
        options = new String("Welcome to the Server\nPress 1 for seeing L1.mp4\nPress 2 for seeing L2.mp4");
    }
    
    
    public void startStreaming(int portAddr, InetAddress inetAddr) throws IOException{
        byte dataReceived[] = new byte[frameSize]; 
        

        sendOptions(portAddr, inetAddr);
        
        
        DatagramPacket choice = new DatagramPacket(dataReceived, dataReceived.length);
        serverSocket.receive(choice);
        
                
        String path = processPath(choice);
        
               
        sendDirInfo(portAddr, inetAddr);
        
        
        ReplyingThread streamVideo = new ReplyingThread(portAddr, inetAddr, path, serverSocket);
        streamVideo.start();
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
        dataSent = Integer.toString(client_id).getBytes();
        

        DatagramPacket dirResponse;
        dirResponse = new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(dirResponse);
    }
    
    
    private String processPath(DatagramPacket choice) throws IOException{
        int chosen = Integer.parseInt(new String(choice.getData()).trim());
        chosen--;
        
        
        if(chosen >= list_Videos.size() || chosen < 0){
            chosen = 0;
        }
        
        
        System.out.println("Choice given by the User" + client_id + ": " + chosen);
        String path = new String(list_Videos.get(chosen));
        return(path);
    }
}




class ReplyingThread extends Thread{
    private final int portAddr;
    private final InetAddress inetAddr;
    private final String path;
    private final DatagramSocket serverSocket;

    private static final int frameSize = 256;
    private int frameSent = 0;
    private static final int sleepingTime = 100;
    private static final int frameStreamSize = 50;
    private final int initialFrames = 2500;
    
    
    
    public ReplyingThread(int portAddr, InetAddress inetAddr, String path, DatagramSocket serverSocket){
        this.portAddr = portAddr;
        this.inetAddr = inetAddr;
        this.path = path;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        super.run();
        
        InputStream input = createInputStream();
        
        try{
            startSending(input);
        }catch(IOException ex){
            System.out.println("IOException in startSending() method");
        }catch(InterruptedException ex) {
            System.out.println("InterruptedException in startSending() method");
        }
        
        try{
            terminate();
        }catch(IOException ex) {
            System.out.println("IOException in startSending() method");
        }
        
        try{
            input.close();
        }catch(IOException ex) {
            System.out.println("IOException in startSending() method");
        }
    }
    
    
    private InputStream createInputStream(){
        InputStream input = null;
        
        
        try{
            input = new FileInputStream(new File(path));
        }catch(FileNotFoundException ex) {
            System.out.println("Error in creating Input File Stream");
        }
        
        return(input);
    }
    
    
    private void startSending(InputStream input) throws IOException, InterruptedException{
        byte dataSent[] = new byte[frameSize];
        
        
        int count = 0;
        count = input.read(dataSent, 0, frameSize);
        
        
        while(count != -1){
            frameSent++;
            System.out.println("Sending Frame: " + frameSent);
            

            DatagramPacket toSent = new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
            serverSocket.send(toSent);
           
           
            dataSent = new byte[frameSize];
            count = input.read(dataSent, 0, frameSize);
            
            regulate();
        }

        regulate();
        DatagramPacket toSent = new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(toSent);
    }
    
    
    private void regulate() throws InterruptedException{
        if(frameSent % frameStreamSize == 0){
            Thread.sleep(sleepingTime);
        }
    }
    
    
    private void terminate() throws IOException{
        String finalMessage = new String("EXIT");
        
        byte dataSent[];
        dataSent = new byte[frameSize];
        dataSent = finalMessage.getBytes();
        
        
        DatagramPacket finalSent = new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(finalSent);
    }
}