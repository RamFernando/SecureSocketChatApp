import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/*
 * Handles clients connection with the server
 */
public class MyClient {

    private static final String TRUSTSTORE_LOCATION = "/home/ramesha/Documents/Keys/Security/CA/ClientKeyStore.jks";
    private static final String TRUSTSTORE_PASSWORD = "123456";

    public static void main(String[] a) throws IOException {

        /*
         * Setting message encryption keys and certificate authority
         */
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_LOCATION);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);

        System.out.println("Client started");
        Scanner scn = new Scanner(System.in);

        String input = scn.nextLine();

        String[] args = input.trim().split(" ");

        if (args.length != 4) {
            System.out.println("Invalid Input : " + input);
            main(a);
        }

        String command = args[0];
        String socket = args[1];
        String action = args[2];
        String client = args[3];

        if (!command.equalsIgnoreCase("connect")) {
            System.out.println("Invalid command : " + command);
            main(a);
        }

        String host = socket.split(":")[0];

        int port = 0;
        try {
           port = Integer.valueOf(socket.split(":")[1]);
        } catch (NullPointerException | NumberFormatException e) {
            System.out.println("Invalid Port : " + socket.split(":")[1]);
            main(a);
        }
        /*
         * Getting localhost ip
         */
        InetAddress ip = InetAddress.getByName(host);
        SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket c = (SSLSocket) f.createSocket(ip, port);

        /*
         * Establish the connection
         */
        c.startHandshake();

        /*
         * Obtaining input and out streams
         */
        DataInputStream dis = new DataInputStream(c.getInputStream());
        DataOutputStream dos = new DataOutputStream(c.getOutputStream());

        /*
         * Register Client
         */
        dos.writeUTF(action + ":" + client);

        /*
         * SendMessage thread
         */
        Thread sendMessage = new MessageSender(scn, dos);

        /*
         * ReadMessage thread
         */
        Thread readMessage = new MessageReader(dis, client);

        sendMessage.start();
        readMessage.start();
    }
}
