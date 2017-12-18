import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUI extends JFrame {
    private JPanel contentPane;
    private JProgressBar progressBar;
    private JButton sendButton;
    private JSpinner chooseNumberofThreads;
    private JButton koniecButton;
    private JButton pauseButton;
    private JButton continueButton;


    public GUI() {

        this.setMinimumSize(new Dimension(600, 100));

        setContentPane(contentPane);
        //setModal(false);
        this.setVisible(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum((int) Server.FILE.length());

        File file = new File("offsety.txt");
        if (file.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader("offsety.txt"));
                String line;
                int progressBarValue = 0;
                List<Integer> lines = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    lines.add(Integer.parseInt(line));
                }
                int len = (int) Math.ceil((double) Server.FILE.length() / lines.size());
                for (int i = 0; i < lines.size(); i++) {
                    progressBarValue += lines.get(i) - i*len;
                }
                reader.close();

                chooseNumberofThreads.setValue(lines.size());
                progressBar.setValue(progressBarValue);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        SwingWorker<Void, Integer> w = new SwingWorker<Void, Integer>() {

            @Override
            protected Void doInBackground() throws Exception {

                int[] prijate = Client.prijate;


                while (Client.pocitadlo.getCount() > 0) {
                    //System.out.println(Arrays.toString(prijate));
                    int sum = 0;
                    for (int i = 0; i < prijate.length; i++) {
                        sum += prijate[i];
                    }

                    publish(sum);

                }
                return null;
            }

            protected void process(List<Integer> chunks) {
               progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                System.out.println("Dokončené");

                progressBar.setValue((int) Server.FILE.length());
                System.exit(0);
            }
        };


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client.posielaj((Integer) chooseNumberofThreads.getValue());
                w.execute();
            }
        });

        koniecButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client.executor.shutdownNow();

                Client.file.delete();
                File file = new File("offsety.txt");
                file.delete();
                System.exit(0);
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client.pause();
                }
        });


        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client.continueDownloading((Integer)chooseNumberofThreads.getValue());
                if(w.getState() != SwingWorker.StateValue.STARTED){
                    w.execute();
                }

            }
        });


    }


}
