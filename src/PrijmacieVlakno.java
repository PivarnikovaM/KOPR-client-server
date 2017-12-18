import java.io.*;
import java.net.Socket;


public class PrijmacieVlakno implements Runnable {

    private int poradie;
    private RandomAccessFile raf;
    private Socket prijimatel;
    public int precitane;
    private int[] prijate;
    private int numberOfThreads;
    private int[] offsety;

    public PrijmacieVlakno(Socket socket, int poradie, File file, int[] prijate, int numberOfThreads, int[] offsety) {
        this.prijimatel = socket;
        this.poradie = poradie;
        this.prijate = prijate;
        this.offsety = offsety;
        this.numberOfThreads = numberOfThreads;
        try {
            this.raf = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            System.out.println("Vlákno č." + poradie + " prijíma");

            InputStream is = prijimatel.getInputStream();

            byte[] bytearray = new byte[OdosielacieVlakno.chunkSize];
            int read = 0;

            int len = (int) Math.ceil((double) Server.FILE.length() / numberOfThreads);
            this.precitane = offsety[poradie] - poradie*len;
            raf.seek(offsety[poradie]);

            while ((read = is.read(bytearray, 0, bytearray.length)) > 0) {

                raf.write(bytearray, 0, read);

                if(Thread.currentThread().isInterrupted()){
                    return;
                }

                offsety[poradie] += read;

                precitane += read;
                prijate[poradie] = precitane;

            }

            raf.close();
            prijimatel.close();



        } catch (IOException e) {
            e.printStackTrace();
        }


        Client.pocitadlo.countDown();


        if (Client.pocitadlo.getCount() == 0) {
            System.out.println("Vsetky vlakna úspešne dokončili sťahovanie");
            File file = new File("offsety.txt");
            file.delete();
            Client.executor.shutdownNow();

        }
    }
}
