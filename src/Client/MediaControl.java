package Client;

import java.io.File;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;


public class MediaControl extends BorderPane{
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    
    
    private boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private double size = 0.0;
    private Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Slider volumeSlider;
    private HBox mediaBar;
    

    public MediaControl(final MediaPlayer mediaPlayer, Duration duration){
        this.mediaPlayer = mediaPlayer;
        this.duration = duration;
        size = Client.getVideoSize();
        
        setStyle("-fx-background-color: #ffffff;");
        mediaView = new MediaView(mediaPlayer);
        
        
        StackPane mvPane = new StackPane();
        mvPane.getChildren().add(mediaView);
        mvPane.setStyle("-fx-background-color: black;");
        setCenter(mvPane);

        
        mediaBar = new HBox();
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);

        
        final Button prevButton = new Button("<<|");
        final Button playButton = new Button("|>");
        final Button nextButton = new Button("|>>");
        final Button forward = new Button(">>");
        final Button rewind = new Button("<<");
        final Button exit = new Button("Quit");
        
        
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Status status = mediaPlayer.getStatus();

                
                if(status == Status.UNKNOWN || status == Status.HALTED){
                    // don't do anything in these states
                    return;
                }else if(status == Status.PAUSED || status == Status.READY || status == Status.STOPPED){
                    // rewind the movie if we're sitting at the end
                    if(atEndOfMedia){
                        mediaPlayer.seek(mediaPlayer.getStartTime());
                        atEndOfMedia = false;
                    }
                    mediaPlayer.play();
                }else{
                    mediaPlayer.pause();
                }
            }
        });
        
        
        nextButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Status status = mediaPlayer.getStatus();
                if(!(status == Status.PAUSED || status == Status.READY || status == Status.STOPPED)){
                    mediaPlayer.pause();
                }
                
                System.out.println("Main Video Player is hidden from the View");
                System.out.println("Media Controller released the semaphore nextInterruptFlag\n");
                
                Client.primaryStage.hide();
                Display.nextCode();
                Client.releaseNextInterrupt();
            }
        });
        
        
        prevButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Status status = mediaPlayer.getStatus();
                if(!(status == Status.PAUSED || status == Status.READY || status == Status.STOPPED)){
                    mediaPlayer.pause();
                }
                
                System.out.println("Main Video Player is hidden from the View");
                System.out.println("Media Controller released the semaphore nextInterruptFlag\n");
                
                Client.primaryStage.hide();
                Display.prevCode();
                Client.releaseNextInterrupt();
            }
        });
        
        
        forward.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Duration currentTime = mediaPlayer.getCurrentTime();
                currentTime = currentTime.add(duration.multiply(0.1));
                
                
                double presentPercentage = (double)((double)new File(Client.getPath()).length()/size);
                Duration availableTime = duration.multiply(presentPercentage*0.80);
                
                
                currentTime = (currentTime.compareTo(availableTime) > 0 && !Receive.isTransmissionOver()) ? availableTime : currentTime;
                currentTime = (currentTime.compareTo(duration) > 0) ? duration : currentTime;
                
                
                timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                playTime.setText(formatTime(currentTime, duration));
                mediaPlayer.seek(currentTime);
            }
        });
        
        
        rewind.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Duration currentTime = mediaPlayer.getCurrentTime();
                currentTime = currentTime.subtract(duration.multiply(0.1));
                
                
                currentTime = (currentTime.compareTo(Duration.ZERO) <= 0) ? Duration.ZERO : currentTime;
                
                
                timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                playTime.setText(formatTime(currentTime, duration));
                mediaPlayer.seek(currentTime);
            }
        });
        
        
        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Platform.exit();
            }
        });
        
        
        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });

        
        mediaPlayer.setOnPlaying(new Runnable(){
            public void run(){
                if(stopRequested){
                    mediaPlayer.pause();
                    stopRequested = false;
                }else{
                    playButton.setText("||");
                }
            }
        });
        
        
        mediaPlayer.setOnPaused(new Runnable() {
            public void run(){
                playButton.setText("|>");
            }
        });

        
        mediaPlayer.setOnReady(new Runnable() {
            public void run(){
                updateValues();
            }
        });
        
        
        mediaPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        
        
        mediaPlayer.setOnStopped(new Runnable() {
            public void run() {
                if (!repeat) {
                    playButton.setText("|>");
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
        });
        
        
        mediaBar.getChildren().add(prevButton);
        Label spacer1 = new Label(" ");
        mediaBar.getChildren().add(spacer1);
        
        mediaBar.getChildren().add(rewind);
        Label spacer2 = new Label(" ");
        mediaBar.getChildren().add(spacer2);
        
        mediaBar.getChildren().add(playButton);
        Label spacer3 = new Label(" ");
        mediaBar.getChildren().add(spacer3);
        
        mediaBar.getChildren().add(forward);
        Label spacer4 = new Label(" ");
        mediaBar.getChildren().add(spacer4);
        
        mediaBar.getChildren().add(nextButton);
        Label spacer5 = new Label(" ");
        mediaBar.getChildren().add(spacer5);


        // Add time slider
        timeSlider = new Slider();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        
        
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov){
                if (timeSlider.isValueChanging()){
                    // multiply duration by percentage calculated by slider position
                    mediaPlayer.seek(duration.multiply(timeSlider.getValue() / 100.0));
                }
            }
        });
        
        
        
        mediaBar.getChildren().add(timeSlider);

        // Add Play label
        playTime = new Label();
        playTime.setPrefWidth(130);
        playTime.setMinWidth(50);
        mediaBar.getChildren().add(playTime);

        
        // Add the volume label
        Label volumeLabel = new Label("Vol ");
        mediaBar.getChildren().add(volumeLabel);

        
        // Add Volume slider
        volumeSlider = new Slider();
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setMinWidth(30);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (volumeSlider.isValueChanging()) {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
                }
            }
        });
        mediaBar.getChildren().add(volumeSlider);
        
        
        Label spacer6 = new Label(" ");
        mediaBar.getChildren().add(spacer6);
        mediaBar.getChildren().add(exit);
        
        setBottom(mediaBar);
    }

    
    protected void updateValues(){
        if (playTime != null && timeSlider != null && volumeSlider != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mediaPlayer.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled()
                            && duration.greaterThan(Duration.ZERO)
                            && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration).toMillis()
                                * 100.0);
                    }
                    if (!volumeSlider.isValueChanging()) {
                        volumeSlider.setValue((int) Math.round(mediaPlayer.getVolume()
                                * 100));
                    }
                }
            });
        }
    }

    
    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)){
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if(durationHours > 0){
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            }else{
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        }else{
            if(elapsedHours > 0){
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            }else{
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
}