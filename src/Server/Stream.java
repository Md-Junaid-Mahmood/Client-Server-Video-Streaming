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


class Stream extends Thread implements Network.NetworkInterface{
    private final int portAddr;
    private final InetAddress inetAddr;
    private final String path;
    private final DatagramSocket serverSocket;

    
    private static final int frameSize = Network.NetworkInterface.frameSize;
    private int frameSent = 0;
    private static final int sleepingTime = Network.NetworkInterface.sleepingTime;
    private static final int frameStreamSize = Network.NetworkInterface.frameStreamSize;
    private final int initialFrames = Network.NetworkInterface.initialFrames;
    private static final String pathProg = Network.NetworkInterface.pathProgram;
    
    
    private static ArrayList<String> list_Videos;
    private static ArrayList<Long> video_Info;
    private final int choice;
    private final int numOfVideo = 3;
    
    public Stream(int portAddr, InetAddress inetAddr, int choice, DatagramSocket serverSocket) throws IOException{
        list_Videos = new ArrayList<String>();
        
        list_Videos.add(pathProg.concat("/Server/Video/L1.mp4"));
        list_Videos.add(pathProg.concat("/Server/Video/L2.mp4"));
        list_Videos.add(pathProg.concat("/Server/Video/L3.mp4"));
        
        video_Info = new ArrayList<Long>();
        processPathLength();
        
        this.portAddr = portAddr;
        this.inetAddr = inetAddr;
        this.choice = choice;
        this.path = processPath(this.choice);
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        super.run();
        
        InputStream input = createInputStream();
        
        try{
            sendVideoSize(choice);
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
        
        while(true);
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
       //     System.out.println("Sending Frame: " + frameSent);
            

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
        
        if(frameSent == initialFrames){
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
    
    
    private String processPath(int chosen) throws IOException{
        chosen--;
        
        
        if(chosen >= numOfVideo || chosen < 0){
            chosen = 0;
        }
        
        
        String path = new String(list_Videos.get(chosen));
        return(path);
    }
    
    
    private void sendVideoSize(int chosen) throws IOException{
        chosen--;
        
        
        if(chosen >= numOfVideo || chosen < 0){
            chosen = 0;
        }
        
        
        byte dataSent[] = new byte[frameSize];
        dataSent = Long.toString(video_Info.get(chosen)).getBytes();
        DatagramPacket videoSize = new DatagramPacket(dataSent, dataSent.length, inetAddr, portAddr);
        serverSocket.send(videoSize);
    }
    
    
    private void processPathLength(){
        for(int i = 0; i < list_Videos.size(); i++){
            File file = new File(list_Videos.get(i));
            video_Info.add((long)file.length());
        }
      //  System.out.println(video_Info);
    }
}