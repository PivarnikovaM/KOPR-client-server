import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final int SERVER_PORT = 9067;
    public static final int KOMUNIKACNY_PORT = 5000;
    public static final String FILE_PATH = "/Users/martinapivarnikova/Downloads/Flatliners.2017.720p.BRRip.999MB.MkvCage.mkv";
    public static final File FILE = new File(FILE_PATH);

    public static ExecutorService executor;

    public static void main(String[] args) {

        ServerSocket socket = null;
        try {
            socket = new ServerSocket(KOMUNIKACNY_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true) {

            Socket clientSocket = null;
            try {

                clientSocket = socket.accept();


                InputStream is = clientSocket.getInputStream();
                DataInputStream dis = new DataInputStream(is);
                int numberOfThreads = 0;
                ServerSocket serverSocket = null;
                while (clientSocket.isConnected()) {

                    String sprava = dis.readUTF();
                    int[] offsety = null;

                    if (sprava.equals("ZACIATOK")) {
                        System.out.println("Začínam posielať");

                        numberOfThreads = dis.readInt();
                        offsety = new int[numberOfThreads];
                        serverSocket = new ServerSocket(SERVER_PORT);
                    }



                    if (sprava.equals("CONTINUE")) {
                        if(serverSocket != null) {
                            serverSocket.close();
                        }
                        serverSocket = new ServerSocket(SERVER_PORT);
                        numberOfThreads = dis.readInt();
                        offsety = new int[numberOfThreads];
                        System.out.println("Pokračujem v posielaní");
                        for (int i = 0; i < numberOfThreads; i++) {
                            offsety[i] = dis.readInt();
                        }

                    }

                    Socket[] clientSockets = new Socket[numberOfThreads];
                    executor = Executors.newFixedThreadPool(numberOfThreads);

                    for (int i = 0; i < numberOfThreads; i++) {
                        clientSockets[i] = (serverSocket.accept());
                        executor.execute(new OdosielacieVlakno(clientSockets[i], i, numberOfThreads, offsety[i]));
                    }

                    serverSocket.close();


                }


            } catch (IOException e) {
                executor.shutdownNow();
            }

        }
    }
}
