/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Network;

/**
 *
 * @author md
 */
public interface NetworkInterface{
    int frameSize = 256;
    
    int receiving_port = 8000; 
    int replying_port = 8080;
    String serverAddress = new String("localHost");
    
    int sleepingTime = 100;
    int waitingTime = 3000;
    int frameStreamSize = 50;
    int initialFrames = 2000;
}
