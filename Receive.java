package Client;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author md
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.*;


class RecvingThread extends Thread{
    DatagramSocket clientSocket;
    private int frameReceived;
    private final int frameSize = 256;
    FileOutputStream output;
    
    
    public RecvingThread(DatagramSocket clientSocket, int frameReceived, FileOutputStream output){
        this.clientSocket = clientSocket;
        this.frameReceived = frameReceived;
        this.output = output;
    }

    
    @Override
    public void run(){
        super.run();

        try{
            startReceiving();
        }catch (IOException ex) {
            System.out.println("IOException in startReceiving() method");
        }
        
        try{
            output.close();
        }catch (IOException ex) {
            System.out.println("IOException in startReceiving() method");
        }
    }
    
    
    private void startReceiving() throws IOException{
        byte dataReceived[] = new byte[frameSize];

        
        DatagramPacket recv = new DatagramPacket(dataReceived, dataReceived.length);
        clientSocket.receive(recv);
       
            
        byte testArray[] = new byte[frameSize];
        String testString = new String("EXIT");
        
        
        while(!new String(recv.getData()).trim().equals(testString.trim())){
            frameReceived++; 
            System.out.println("Receiving Frame: " + frameReceived); 

            
            output.write(recv.getData(), 0, recv.getLength());
            
            
            dataReceived = new byte[frameSize];
            recv = new DatagramPacket(dataReceived, dataReceived.length);
            clientSocket.receive(recv);
        }
    }
}

public class Receive{
    static DatagramSocket clientSocket;
    private static int frameReceived = 0;
    private static final int frameSize = 256;
    private static final int initialFrames = 2000;
    FileOutputStream output;
    
    
    public Receive(DatagramSocket clientSocket, FileOutputStream output){
        Receive.clientSocket = clientSocket;
        this.output = output;
    }
    
    
    public boolean startReceiving() throws IOException{
        byte dataReceived[];
        dataReceived = new byte[frameSize];
        
        
        byte testArray[] = new byte[frameSize];
        String testString = new String("EXIT");
      
    
        dataReceived = new byte[frameSize];
        DatagramPacket recv = new DatagramPacket(dataReceived, dataReceived.length);
        clientSocket.receive(recv);

        
        boolean flag_regulate = true;
        boolean flag_exit = !new String(recv.getData()).trim().equals(testString.trim());
        
        
        while(flag_regulate && flag_exit){
            frameReceived++; 
            System.out.println("Receiving Frame: " + frameReceived); 

            
            output.write(recv.getData(), 0, recv.getLength());
            
            
            flag_regulate = regulate();

            
            dataReceived = new byte[frameSize];
            recv = new DatagramPacket(dataReceived, dataReceived.length);
            clientSocket.receive(recv);    
            
            
            flag_exit = !new String(recv.getData()).trim().equals(testString.trim());
        }
        
        return(true);
    }

    
    private boolean regulate() {
        if(frameReceived == initialFrames){
            RecvingThread recvThd = new RecvingThread(clientSocket, frameReceived, output);
            recvThd.start();    
            return(false);
        }else{
            return(true);
        }
    }
}
