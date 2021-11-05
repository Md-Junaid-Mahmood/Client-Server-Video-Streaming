## <div align = "center"> Client-Server-Video-Streaming </div>
In our project, we currently have three different packages:
  + **Server package** is a collection of server-side code
  + **Client package** is a collection of server-side code
  + **Network package** is a collection of all parameters that must be consistent between server and client
	
At the server-side we have two threads. Thread corresponding to the **Server.java** is responsible for carrying out various control related task, such as keeping information about the clients (IP Address & Port Number), handling choice request of the client for the video at various point of time, initiating/interrupting the stream as per the request and so on. Thread corresponding to the **Stream.java** is responsible for streaming the video to various clients. Separate ports have been used for communicating control information and streaming.

At the client-side we have four threads. Thread corresponding to the **Client.java** is responsible for setting up the necessary network connections at the client side. Thread corresponding to the **Display.java** is responsible for setting up the appropriate display for the client and thread corresponding to the **MediaControl.java** is responsible for managing the display (event handling). Thread **Receive.java** is responsible for accepting the stream from the server. A single port has only been used for communicating control information and receiving streams. We have used semaphores to ensure that actions performed by the thread are in meaningful order.

Network Package consists of interfaces that ensure smooth operation of the network, such as port number and IP Address of the server,  maintaining constant value of frame size across client and server, avoiding flooding at the client side (by defining number of frames that can be transmitted per unit time by Server in a stream) and so on.

We have used UDP (User Datagram Protocol) as the transport layer protocol in our project. Now, transmission of control information such as transmission of Initial Connection Request, choice for the video, unique client ID, request for next video, consent for starting the stream and so on requires reliability. Thus, for the transmission of control information we have used **Stop & Wait ARQ protocol** to ensure reliability. 
