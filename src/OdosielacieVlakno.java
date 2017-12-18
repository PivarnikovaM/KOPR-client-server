import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;


public class OdosielacieVlakno implements Runnable {

    private int count;
    private Socket clientSocket;
    private RandomAccessFile raf = new RandomAccessFile(Server.FILE, "rw");
    public static final int chunkSize = 3 * 4096;
    private int numberOfThreads;
    private int offset;

    public OdosielacieVlakno(Socket clientSocket, int count, int numberOfThreads, int offset) throws FileNotFoundException {
        this.offset = offset;
        this.count = count;
        this.clientSocket = clientSocket;
        this.numberOfThreads = numberOfThreads;
    }

    @Override
    public void run() {

        try {
            OutputStream os = clientSocket.getOutputStream();


            int len = (int) Math.ceil((double) Server.FILE.length() / numberOfThreads);

            int off = offset;


            //dlzka po kade ma ist cyklus
            int dlzka = (int) Math.ceil((double) len / chunkSize);


            raf.seek(off);
            byte[] data = new byte[chunkSize];

            for (int i = 0; i < dlzka; i++) {


                off += chunkSize;

                int read = raf.read(data, 0, data.length);

                if (read <= 0) {
                    break;
                }

                os.write(data, 0, read);
                os.flush();

            }

            clientSocket.close();

        } catch(SocketException se) {
            //se.printStackTrace();
            System.err.println("Prerušenie posielania na vlákne č." + count);
        } catch (IOException e) {
            e.printStackTrace();

        }

    }
}
