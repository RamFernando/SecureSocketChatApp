import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

/*
 * Handles clients connected with the server
 */
class ClientHandler implements Runnable {

    private final DataInputStream dis;
    private final DataOutputStream dos;
    private String name;
    private Socket socket;
    private boolean isLoggedIn;

    public ClientHandler(Socket socket,DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.socket = socket;
        this.isLoggedIn = true;
    }

    @Override
    public void run() {
        /*
         * Stores the incoming commands
         */
        String received;

        while (true) {
            try {

                received = dis.readUTF();

                if (received.contains("as:")) {

                    this.name = received.split(":")[1];

                    /*
                     * User login
                     */
                    if (Server.registry.get(this.name) == null) {

                        System.out.println(this.name + " is connected.");
                        Server.registry.put(this.name, this);
                        this.dos.writeUTF(this.name + " is logged In.");

                    } else {
                        /*
                         * Handling duplicates
                         */

                        System.out.println("This name is already exist.");
                        this.dos.writeUTF(this.name + " is already exist.");
                        socket.close();
                        break;
                    }

                } else if (received.equalsIgnoreCase("list")) {
                    /*
                     * Returns the list of all available users.
                     */
                    Server.registry.forEach((k, v) -> {
                        try {
                            this.dos.writeUTF(k);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                } else if (received.contains("->")) {

                    /*
                     * Communication between message sender and the recipient
                     */
                    StringTokenizer stringTokenizer = new StringTokenizer(received, "->");
                    String MsgToSend = stringTokenizer.nextToken().trim();
                    String recipient = stringTokenizer.nextToken().trim();

                    /*
                     * Search for the recipient in the connected devices list.
                     */
                    ClientHandler clientHandler = Server.registry.get(recipient);

                    /*
                     * If the recipient is found, write on its output stream
                     */
                    if (clientHandler != null && clientHandler.isLoggedIn) {
                        clientHandler.dos.writeUTF(this.name + " : " + MsgToSend);
                    } else {
                        this.dos.writeUTF(recipient + " is not registered");
                    }

                    this.dos.writeUTF("");

                }else if(received.contains("exit")){
                    /*
                     * User logout
                     */
                    System.out.println(this.name+" is disconnected");
                    Server.registry.remove(this.name, this);
                    this.dos.writeUTF(this.name + " is Logged Out.");
                }else {
                    this.dos.writeUTF("Nothing happened from the input " + received);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
