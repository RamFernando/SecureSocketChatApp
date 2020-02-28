import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Handles the Server
 */
public class Server {

    private static final int HTTPS_PORT = 8282;
    private static final String KEYSTORE_LOCATION = "/home/ramesha/Documents/Keys/Security/Keys/ServerKeyStore.jks";
    private static final String KEYSTORE_PASSWORD = "123456";

    /*
     * Stores all the users logged in. Removes when logged out.
     */
    static ConcurrentHashMap<String, ClientHandler> registry = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        /*
         * Setting message encryption keys and certificate authority
         */
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(HTTPS_PORT);
        System.out.println("Chat Server Started on socket:127.0.0.1:" + HTTPS_PORT );

        /*
         * Introducing the socket
         */
        Socket socket;

        /*
         * Running infinite loop for getting client request
         */
        while (true) {

            /*
             * Accept the incoming request
             */
            socket = serverSocket.accept();

            /*
             * Obtain input and output streams
             */
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            /*
             * Create a new handler object for handling this request.
             */
            ClientHandler clientHandler = new ClientHandler(socket, dis, dos);

            /*
             * Create a new Thread with this object.
             */
            Thread t = new Thread(clientHandler);

            /*
             * Add this client to active clients list
             */
            t.start();
        }
    }
}

