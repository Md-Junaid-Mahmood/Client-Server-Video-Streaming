package Client;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javafx.application.Application;  
import javafx.scene.Group;  
import javafx.scene.Scene;  
import javafx.scene.media.Media;  
import javafx.scene.media.MediaPlayer;  
import javafx.stage.Stage;  
import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import javafx.application.Platform;
import javafx.util.Duration;
        

public class Client extends Application implements Network.NetworkInterface{
    private static Media media;
    private static MediaPlayer mediaPlayer;
    static Stage primaryStage;
    private static File fileOutput;
    private static String directory;

    
    private static final int serverSendingPort = Network.NetworkInterface.receiving_port;
    private static final int serverReceivingPort = Network.NetworkInterface.replying_port;
    private static final String serverAddress = Network.NetworkInterface.serverAddress;
    private static final String pathProg = Network.NetworkInterface.pathProgram;
    
    
    private static String clientID = new String("");
    private static final int numOfVideo = 3;

    
    private static final int frameSize = Network.NetworkInterface.frameSize;
    private static final int sleepingTime = Network.NetworkInterface.sleepingTime;
    private static DatagramSocket clientSocket;
    

    private static int choice;
    private static String path;
    private static long videoSize;
    
    
    private static Semaphore launchVideo = new Semaphore(1);
    private static Semaphore nextInterrutFlag = new Semaphore(1);
    
    
    public static void main(String[] args) throws SocketException, UnknownHostException, IOException, InterruptedException{ 
      clientSocket = new DatagramSocket();
      byte dataReceived[] = new byte[frameSize];
      System.out.println("Client has started...\n");
      DatagramPacket dirInfo;
      
      System.out.println("Sending Connection Request");
      while(true){
          String initMessage;
          try{
                sendConnectionRequest();
                
                DatagramPacket optionPack = new DatagramPacket(dataReceived, dataReceived.length);
                
                clientSocket.setSoTimeout(Network.NetworkInterface.waitingTime);
                clientSocket.receive(optionPack);
                
                initMessage = new String(optionPack.getData());
                initMessage = initMessage.trim();


                dataReceived = new byte[frameSize];
                dirInfo = new DatagramPacket(dataReceived, dataReceived.length);
                
                clientSocket.setSoTimeout(Network.NetworkInterface.waitingTime);
                clientSocket.receive(dirInfo);
          }catch(Exception e){
              System.out.println("TimeOut Occurred! Sending Connection Request Again");
              continue;
          }
          
          System.out.println(initMessage + "\n");
          break;
      }
      
      
      createDirectory(dirInfo);
      
      
      int choice = sendChoice();
      Client.choice = choice;
      String path = getPath();
      Client.path = path;
      
      
      Display display = new Display(path, clientSocket, clientID, media, mediaPlayer);
      display.start();
      Thread.sleep(sleepingTime);
      
      
      Client.launchVideo.acquire();
      System.out.println("Main Video Player at Client Side is launched\n");
      launch(args);
       
      
      Client.terminateConnection();
      deleteDirectory();
      System.out.println("Client has terminated");
      System.exit(0);
    }
    
    
    private static void sendConnectionRequest() throws UnknownHostException, IOException{
        String initialMessage = new String("Connection Request");
        
        byte dataSent[] = new byte[frameSize];
        dataSent = initialMessage.getBytes();


        InetAddress address = InetAddress.getByName(serverAddress);
        DatagramPacket initialSentPacket = new DatagramPacket(dataSent, dataSent.length, address, serverSendingPort);
        clientSocket.send(initialSentPacket);
    }
    
    
    private static int sendChoice() throws UnknownHostException, IOException{
        int choice;
        while(true){
            try{
                Scanner sc = new Scanner(System.in);

                
                System.out.print("Enter Your Choice: ");
                choice = sc.nextInt();
                String choice_String = Integer.toString(choice);
                choice_String = "Choice " + choice_String;


                byte dataSent[] = new byte[frameSize];
                dataSent = choice_String.getBytes();

                
                InetAddress address = InetAddress.getByName(serverAddress);
                DatagramPacket choice_Packet = new DatagramPacket(dataSent, dataSent.length, address, serverSendingPort);
                clientSocket.send(choice_Packet);
        
        
                byte dataReceived[] = new byte[frameSize];
                DatagramPacket confirmation = new DatagramPacket(dataReceived, dataReceived.length);
                
                
                clientSocket.setSoTimeout(Network.NetworkInterface.waitingTime);
                clientSocket.receive(confirmation);
                
                
                String testString = new String("OKAY");
                boolean flag_exit = !new String(confirmation.getData()).trim().equals(testString.trim());
                if(flag_exit){
                    System.out.println("Erroneous Confirmation Received\n You need to enter the choice again");
                    continue;
                }else{
                    System.out.println("Choice was accepted successfully\n");
                    break;
                }
            }catch(Exception ex){
                System.out.println("TimeOut Occurred! You need to enter the choice again");
                continue;
            }
        }
        
        return(choice);
    }
    
    
    private static void createDirectory(DatagramPacket dirInfo){
        String dir = new String(dirInfo.getData());
        dir = dir.trim();
      
        
        directory = new String(pathProg.concat("/User"));
        directory = directory.concat(dir);
        directory = directory.concat("/");
        new File(directory).mkdir();
        
        
        clientID = new String("User ").concat(dir);
    }
    
    
    private static void terminateConnection() throws UnknownHostException, IOException{
        byte dataSent[] = new byte[frameSize];
        String exit = new String("Terminate");
        dataSent = exit.getBytes();
        
        InetAddress address = InetAddress.getByName(serverAddress);
        DatagramPacket closePacket = new DatagramPacket(dataSent, dataSent.length, address, serverSendingPort);
        clientSocket.send(closePacket);
        
    } 
    
    
    public static void sendReadyAck() throws UnknownHostException, IOException{
        byte dataSent[] = new byte[frameSize];
        String ack = new String("Start Sending");
        dataSent = ack.getBytes();
        
        InetAddress address = InetAddress.getByName(serverAddress);
        DatagramPacket ackPacket = new DatagramPacket(dataSent, dataSent.length, address, serverSendingPort);
        clientSocket.send(ackPacket);
    }
    
    
    public static void performNext(){
        choice = choice % numOfVideo;
        choice = choice + 1;
        
        String videoChoice = new String("L" + choice + ".mp4");
        path = directory.concat(videoChoice);
    }
    
    
    public static void performPrev(){
        choice = choice - 1;
        choice = (choice == 0) ? numOfVideo : choice;
        
        String videoChoice = new String("L" + choice + ".mp4");
        path = directory.concat(videoChoice);
    }
    
    
    public static void sendNext() throws UnknownHostException, IOException{
        while(true){
            try{
                byte dataSent[] = new byte[frameSize];


                String next = new String("Next ");
                next = next.concat(Integer.toString(choice));
                dataSent = next.getBytes();

                
                InetAddress address = InetAddress.getByName(serverAddress);
                DatagramPacket nextVideo = new DatagramPacket(dataSent, dataSent.length, address, serverSendingPort);
                clientSocket.send(nextVideo);
                
                
                byte dataReceived[] = new byte[frameSize];
                DatagramPacket confirmation = new DatagramPacket(dataReceived, dataReceived.length);
                
                
                clientSocket.setSoTimeout(Network.NetworkInterface.waitingTime);
                clientSocket.receive(confirmation);
                
                
                String testString = new String("OKAY");
                boolean flag_exit = !new String(confirmation.getData()).trim().equals(testString.trim());
                if(flag_exit){
                    System.out.println("Erroneous Confirmation Received\nSending Request Again");
                    continue;
                }else{
                    System.out.println("Request was accepted successfully\n");
                    break;
                }
            }catch(Exception e){
                System.out.println("TimeOut Occurred! Sending Request Again");
                continue;
            }
        }
    }
    
    
    public static void acquireLaunchVideo() throws InterruptedException{
        launchVideo.acquire();
    }
    
    
    public static void releaseLaunchVideo(){
        launchVideo.release();
    }
    
    
    public static void acquireNextInterrupt() throws InterruptedException{
        nextInterrutFlag.acquire();
    }
    
    
    public static void releaseNextInterrupt(){
        nextInterrutFlag.release();
    }

    
    public static String getClientID(){
        return clientID;
    }
    
    
    public static String getPath(){
        String videoChoice = new String("L" + choice + ".mp4");
        path = directory.concat(videoChoice);
        return path;
    }
    
    
    public static int getChoice(){
        return choice;
    }
    
    
    public static void deleteDirectory() throws IOException{
    Path dirPath = Paths.get(directory);

    Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {
            // delete directories or folders
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException{
                Files.delete(dir);
                System.out.printf("Directory is deleted : %s%n", dir);
                return FileVisitResult.CONTINUE;
            }

            // delete files
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException{
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        }
    );
}
    
    
    static void updateFileOutput(File fileOutput){
        Client.fileOutput = fileOutput;
    }
    
    
    static void setVideoSize(long size){
        Client.videoSize = size;
    }
    
    
    static long getVideoSize(){
        return Client.videoSize;
    }
    
    
    @Override  
    public void start(Stage primaryStage) throws Exception{
        media = new Media(fileOutput.toURI().toString());   
        mediaPlayer = new MediaPlayer(media);     
        
        
        this.primaryStage = primaryStage;
        Platform.setImplicitExit(false);
        
        
        mediaPlayer.setOnReady(new Runnable(){    
            @Override
            public void run() {
                    // Add Pane to scene
                Duration duration = media.getDuration();
                
                MediaControl mediaControl = new MediaControl(mediaPlayer, duration);
                primaryStage.setTitle(clientID);
                Group root = new Group();
                
                
                Scene scene = new Scene(root, Math.max(media.getWidth(), 700), media.getHeight() + 35);
                scene.setRoot(mediaControl);
                
                
                primaryStage.setScene(scene);
                primaryStage.sizeToScene();
                primaryStage.show();
            }
        });
    }
}