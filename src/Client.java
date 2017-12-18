import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    public static File file = new File("test.mkv");
    public static int[] prijate;
    public static ExecutorService executor;
    public static int[] offsety;


    public static CountDownLatch pocitadlo;
    public static Socket socket;
    public static OutputStream os;
    public static DataOutputStream dos;
    private static File offsetyFile = new File("offsety.txt");


    public static void main(String[] args) {


        SwingUtilities.invokeLater(() -> {
            GUI form = new GUI();
            form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            form.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    System.exit(0);
                }
            });
            form.setVisible(true);


        });


    }

    public static void posielaj(int numberOfThreads) {

        offsety = new int[numberOfThreads];

        System.out.println("Počet vlákien: " + numberOfThreads);
        prijate = new int[numberOfThreads];

        try {

            pocitadlo = new CountDownLatch(numberOfThreads);

            socket = new Socket("localhost", Server.KOMUNIKACNY_PORT);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            dos.writeUTF("ZACIATOK");
            dos.writeInt(numberOfThreads);

            dos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {


            Socket[] sockets = new Socket[numberOfThreads];

            for (int i = 0; i < numberOfThreads; i++) {
                sockets[i] = new Socket("localhost", Server.SERVER_PORT);
            }

            executor = Executors.newFixedThreadPool(numberOfThreads);


            for (int i = 0; i < numberOfThreads; i++) {
                int off = i * (int) Math.ceil((double) Server.FILE.length() / numberOfThreads);
                ;
                offsety[i] = off;
                PrijmacieVlakno pv = new PrijmacieVlakno(sockets[i], i, file, prijate, numberOfThreads, offsety);
                executor.execute(pv);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pause() {
        System.out.println("Prerušenie sťahovania");
        executor.shutdownNow();


        PrintWriter pw = null;
        try {
            pw = new PrintWriter(offsetyFile);
            for (int i = 0; i < offsety.length; i++) {
                pw.println(offsety[i]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

    }

    public static void continueDownloading(int numberOfThreads) {

        if (prijate == null) {
            prijate = new int[numberOfThreads];
        }

        if (pocitadlo == null) {
            pocitadlo = new CountDownLatch(numberOfThreads);
        }

        File offsetyFile = new File("offsety.txt");
        Scanner sc;
        try {
            sc = new Scanner(offsetyFile);

            if (offsety == null) {
                offsety = new int[numberOfThreads];
            }

            if (socket == null) {
                socket = new Socket("localhost", Server.KOMUNIKACNY_PORT);
                os = socket.getOutputStream();
                dos = new DataOutputStream(os);
            }

            for (int i = 0; i < numberOfThreads; i++) {
                offsety[i] = sc.nextInt();
            }

            try {

                dos.writeUTF("CONTINUE");
                dos.writeInt(numberOfThreads);
                for (int i = 0; i < numberOfThreads; i++) {
                    dos.writeInt(offsety[i]);
                }

                dos.flush();

                Socket[] sockets = new Socket[numberOfThreads];

                for (int i = 0; i < numberOfThreads; i++) {
                    sockets[i] = new Socket("localhost", Server.SERVER_PORT);
                }

                executor = Executors.newFixedThreadPool(numberOfThreads);

                for (int i = 0; i < numberOfThreads; i++) {
                    PrijmacieVlakno pv = new PrijmacieVlakno(sockets[i], i, file, prijate, numberOfThreads, offsety);
                    executor.execute(pv);

                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
