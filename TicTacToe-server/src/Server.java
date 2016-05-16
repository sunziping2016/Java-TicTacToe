import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by sun on 5/14/16.
 *
 * Server
 */
public class Server {
    public static final String HELP =
            "Usage: TicTacToe-server [OPTION]...\n" +
            "  -p, --port=PORT            listen to PORT\n" +
            "  -help                      print this help text\n";

    public static void main(String[] args) {
        int port = 9797;

        for (int i = 0; i < args.length; ++i) {
            if (args[i].startsWith("--port")) {
                port = Integer.parseInt(args[i].substring("--port".length() + 1));
            } else if (args[i].equals("-p")) {
                ++i;
                port = Integer.parseInt(args[i]);
            } else if (args[i].equals("--help")) {
                System.out.print(HELP);
                return;
            }
        }
        try {
            System.out.println("Listen at port " + port + ".");
            ServerSocket serverSocket = new ServerSocket(port);
            GameController gameController = new GameController();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection from " + clientSocket.getRemoteSocketAddress() + ".");
                if (!gameController.needPLayer())
                    gameController = new GameController();
                gameController.addPlayer(clientSocket);
                if (gameController.isFull())
                    gameController = new GameController();
            }
        } catch (IOException error) {
            error.printStackTrace();
        }
    }
}
