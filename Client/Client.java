package Client;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javafx.event.ActionEvent;
import javafx.application.Application;  
import javafx.scene.Group;  
import javafx.scene.Scene;  
import javafx.scene.media.Media;  
import javafx.scene.media.MediaPlayer;  
import javafx.scene.media.MediaView;  
import javafx.stage.Stage;  
import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import static javafx.application.Application.launch;
import javafx.beans.binding.*;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


public class Client extends Application{  
    private static File fileOutput;
    private Media media;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    
    private String directory;

    private static final int serverSendingPort = 8000;
    private static final int serverReceivingPort = 8080;
    private static final String serverAddress = "localHost";

    private static final int frameSize = 256;
    DatagramSocket clientSocket;

    public static void main(String[] args) throws SocketException, UnknownHostException, IOException { 
      Client client = new Client();  
        
      client.clientSocket = new DatagramSocket();
      byte dataReceived[] = new byte[frameSize];

      
      client.sendConnectionRequest();
      
      
      DatagramPacket optionPack = new DatagramPacket(dataReceived, dataReceived.length);
      client.clientSocket.receive(optionPack);
      System.out.println(new String(optionPack.getData()).trim());
      
      
      int choice = client.sendChoice();
    
      
      dataReceived = new byte[frameSize];
      DatagramPacket dirInfo = new DatagramPacket(dataReceived, dataReceived.length);
      client.clientSocket.receive(dirInfo);
      
      
      String dir = client.createDirectory(dirInfo);
      String path = client.getPath(dir, choice);
    
      
      client.fileOutput = new File(path);
      FileOutputStream output = new FileOutputStream(client.fileOutput);  
      
      
      Receive newReceive = new Receive(client.clientSocket, output);
      boolean flag = newReceive.startReceiving();
      
      
      launch(args);
    }  
    
    
    private void sendConnectionRequest() throws UnknownHostException, IOException{
        String initialMessage = new String("Connection Request");
        
        byte dataSent[] = new byte[frameSize];
        dataSent = initialMessage.getBytes();


        InetAddress address = InetAddress.getByName(serverAddress);
        DatagramPacket initialSentPacket = new DatagramPacket(dataSent, dataSent.length, address, serverSendingPort);
        clientSocket.send(initialSentPacket);
    }
    
    
    private int sendChoice() throws UnknownHostException, IOException{
        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();
        String choice_String = Integer.toString(choice);

        
        byte dataSent[] = new byte[frameSize];
        dataSent = choice_String.getBytes();

        InetAddress address = InetAddress.getByName(serverAddress);
        DatagramPacket choice_Packet = new DatagramPacket(dataSent, dataSent.length, address, serverSendingPort);
        clientSocket.send(choice_Packet);
        
        return(choice);
    }
    
    
    private String createDirectory(DatagramPacket dirInfo){
        String dir = new String(dirInfo.getData());
        dir = dir.trim();
      
        directory = new String("/home/md/NetBeansProjects/Video Streaming/src/User");
        directory = directory.concat(dir);
        directory = directory.concat("/");
        new File(directory).mkdir();
        
        return(directory);
    }

    
    private String getPath(String dir, int choice) {
        String path = new String("L");
        path = path.concat(Integer.toString(choice));
        path = path.concat(".mp4");
        
        
        path = directory.concat(path);
        return(path);
    }
    
    
    @Override  
    public void start(Stage primaryStage) throws Exception {  
        media = new Media(fileOutput.toURI().toString());   
        mediaPlayer = new MediaPlayer(media);    
        mediaView = new MediaView(mediaPlayer);  
            
        // mediaPlayer.setAutoPlay(true);  
        
        DoubleProperty mvw = mediaView.fitWidthProperty();
        DoubleProperty mvh = mediaView.fitHeightProperty();
        
        mvw.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        mvh.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
        
        mediaView.setPreserveRatio(true);

        Group root = new Group();  
        root.getChildren().add(mediaView);  
        
        HBox paneForButtons = new HBox(40);
        Button btLeft = new Button("Play");
        Button btRight = new Button("Pause");
        Button exit = new Button("Exit");
        
        paneForButtons.getChildren().addAll(btLeft, btRight,exit);
        paneForButtons.setAlignment(Pos.CENTER);
        paneForButtons.setStyle("-fx-border-color: black; -fx-border-width: 2");
        
        BorderPane pane = new BorderPane();
        pane.setBottom(paneForButtons);
        
        btLeft.setOnAction((ActionEvent e) -> {
            play(e);
        });
        
        btRight.setOnAction((ActionEvent e) -> {
            pause(e);
        });
        
        exit.setOnAction((ActionEvent e) -> {
            System.exit(0);
        });
        
        Pane paneForText = new Pane();
        paneForText.getChildren().add(root);
        pane.setCenter(paneForText);
        
        Scene scene = new Scene(pane, 300, 400);  
        primaryStage.setScene(scene);  
        primaryStage.setTitle("Welcome");  
        primaryStage.show();  
    }

    
    public void play(ActionEvent event){
        mediaPlayer.play();
    }
    
    
    public void pause(ActionEvent event){
        mediaPlayer.pause();
    }
} 