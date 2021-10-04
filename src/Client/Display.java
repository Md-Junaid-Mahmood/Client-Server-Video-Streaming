/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 *
 * @author md
 */
public class Display extends Thread implements Network.NetworkInterface{
    private static Media media;
    private static MediaPlayer mediaPlayer;
    
    
    private static String path;
    private static String clientID;
    private static DatagramSocket clientSocket;
    private static boolean repeat = false;
    private static final int sleepingTime = Network.NetworkInterface.sleepingTime;
    
    
    private static int code = -1;
    
    
    public Display(String path, DatagramSocket clientSocket, String clientID, Media media, MediaPlayer mediaPlayer){
        Display.path = path;
        Display.clientID = clientID; 
        Display.clientSocket = clientSocket;
        Display.media = media;
        Display.mediaPlayer = mediaPlayer;
    }

    
    @Override
    public void run(){
        try{
            while(true){
                File fileOutput = new File(path);
                FileOutputStream output = new FileOutputStream(fileOutput);
                Client.updateFileOutput(fileOutput);


                Receive newReceive = new Receive(clientSocket, output);
                newReceive.start();


                Thread.sleep(sleepingTime);
                if(repeat){
                     Client.acquireLaunchVideo();
                     startDisplay(fileOutput);
                     System.out.println("Main Video Player has started with the correct video\n");
                 }


                Client.acquireNextInterrupt();
                System.out.println("Client acquired the semaphore nextInterruptFlag");
                System.out.println("Request for next Video is getting processed");
                
                newReceive.interrupt();
               
               
                if(code == 1)
                   Client.performNext();
                else if(code == 2)
                   Client.performPrev();
               
               
                Client.sendNext();
                path = Client.getPath();
                
                
                System.out.println("Client released the semaphore launchVideo");
                System.out.println("Client released the semaphore nextInterruptFlag\n");

                Client.releaseLaunchVideo();
                Client.releaseNextInterrupt();
                code = -1;
                repeat = true;
            }
        }catch(IOException ex){
            System.out.println("IOException has occurred inside sendNext() method");
        }catch(InterruptedException ex){
            System.out.println("InterruptedException has occurred inside acquire() method or release() method");
        }
    }
    
    
    private void startDisplay(File fileOutput){
        Platform.runLater(new Runnable(){
        @Override
        public void run(){
        media = new Media(fileOutput.toURI().toString());   
        mediaPlayer = new MediaPlayer(media);     

        mediaPlayer.setOnReady(new Runnable(){    
          @Override
          public void run() {
              Duration duration = media.getDuration();

              MediaControl mediaControl = new MediaControl(mediaPlayer, duration);
              Client.primaryStage.setTitle(clientID);
              Group root = new Group();
              Scene scene = new Scene(root, Math.max(media.getWidth(), 700), media.getHeight() + 35); 
              scene.setRoot(mediaControl);


              Platform.setImplicitExit(false);
              Client.primaryStage.setScene(scene);
              Client.primaryStage.sizeToScene();
              Client.primaryStage.show();
            }
          });
        }
        });
    }
    
    
    static void nextCode(){
        code = 1;
    }

    
    static void prevCode(){
        code = 2;
    }
}
