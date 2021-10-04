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
import java.net.SocketException;


public class Receive extends Thread implements Network.NetworkInterface{
    private DatagramSocket clientSocket;
    private FileOutputStream output;
    
    
    private int frameReceived = 0;
    private static final int frameSize = Network.NetworkInterface.frameSize;
    private static final int initialFrames = Network.NetworkInterface.initialFrames;
    private static final int sleepingTime = Network.NetworkInterface.sleepingTime;
   
    
    private static boolean transmissionComplete = false;
    
    
    public Receive(DatagramSocket clientSocket, FileOutputStream output){
        this.clientSocket = clientSocket;
        this.output = output;
    }
    
    
    @Override
    public void run(){
        transmissionComplete = false;
        try{
            Client.acquireLaunchVideo();
            Client.acquireNextInterrupt();
            System.out.println("Receiving Thread acquired the semaphore launchVideo at " + frameReceived + " frames");
            System.out.println("Receiving Thread acquired the semaphore nextInterruptFlag at " + frameReceived + " frames");
        }catch(Exception ex){
            System.out.println("Exception in startReceiving() method");
        }
        
        while(true){
            try{
                Client.sendReadyAck();
                byte dataReceived[] = new byte[frameSize];
                DatagramPacket videoSize = new DatagramPacket(dataReceived, dataReceived.length);
                clientSocket.receive(videoSize);

                long size = Long.parseLong(new String(videoSize.getData()).trim());
                System.out.println("Length of the Video File is: " + size + " bytes");
                Client.setVideoSize(size);
            }catch(Exception ex){
                System.out.println("Error in Sending Acknowledgement");
                continue;
            }
            System.out.println("Successfully Acknowledged! Starting display in some time\n");
            break;
        }
        
        
        try{
            startReceiving();
            
            output.close();
        }catch(Exception ex){
            System.out.println("Exception in startReceiving() method");
        }
        
        
        transmissionComplete = true;
        while(true);
    }
    
    
    private void startReceiving() throws IOException, SocketException, InterruptedException{
        byte dataReceived[];
        dataReceived = new byte[frameSize];
        
        
        byte testArray[] = new byte[frameSize];
        String testString = new String("EXIT");
      
    
        dataReceived = new byte[frameSize];
        DatagramPacket recv = new DatagramPacket(dataReceived, dataReceived.length);
        clientSocket.receive(recv);

        
        boolean flag_exit = !new String(recv.getData()).trim().equals(testString.trim());
        
        
        while(flag_exit){
            frameReceived++; 
         //   System.out.println("Receiving Frame: " + frameReceived); 

            
            output.write(recv.getData(), 0, recv.getLength());

            
            dataReceived = new byte[frameSize];
            recv = new DatagramPacket(dataReceived, dataReceived.length);
            clientSocket.receive(recv);    
            
            
            flag_exit = !new String(recv.getData()).trim().equals(testString.trim());
            regulate();
        }
    }

    
    private void regulate() throws SocketException, InterruptedException {
        if(frameReceived == initialFrames){
            System.out.println("Thread released the semaphore launchVideo at " + frameReceived + " frames\n");
            Client.releaseLaunchVideo();
            Thread.sleep(sleepingTime);
        }
    }
    
    
    static boolean isTransmissionOver(){
        return transmissionComplete;
    }
}
