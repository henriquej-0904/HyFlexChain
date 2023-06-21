package pt.unl.fct.di.blockmess.wrapper.server.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import pt.unl.fct.di.blockmess.wrapper.server.BlockmessWrapperServer;

public class BlockmessWrapperServerTCP {
    public static void main(String[] args) {

        String[] blockmessProps = Arrays.copyOfRange(args, 1, args.length, String[].class);
        BlockmessWrapperServer blockmess = new BlockmessWrapperServer(blockmessProps);
        
        try (ServerSocket server = new ServerSocket(Integer.parseInt(args[0])))
        {
            while (true) {
                try (Socket client = server.accept()) {
                    client.setKeepAlive(true);
                    blockmess.start(client.getInputStream(), client.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class TestWrapper {

        public static void main(String[] args) {

            try (Socket s = new Socket(args[0], Integer.parseInt(args[1])))
            {
                final DataInputStream blockmessOutput = new DataInputStream(s.getInputStream());
                final DataOutputStream blockmessInput = new DataOutputStream(s.getOutputStream());

                boolean sendInput = false;

                try (Scanner sc = new Scanner(System.in)) {
                    sendInput = Boolean.parseBoolean(sc.nextLine());
                }

                if (sendInput) {
                    new Thread(() -> {
                        Random r = new Random(0);
                        byte[] b = new byte[200];

                        while (true) {
                            r.nextBytes(b);
                            try {
                                blockmessInput.writeInt(b.length);
                                blockmessInput.write(b);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, "write blockmes input")
                            .start();
                }

                long iter = 0;
                while (true) {
                    int count;
                    try {
                        count = blockmessOutput.readInt();
                        blockmessOutput.skipNBytes(count);

                        System.out.println("Received operation " + iter + " with " + count + " size");
                        iter++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
