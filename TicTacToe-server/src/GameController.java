import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by sun on 5/14/16.
 *
 * Game controller.
 */
public class GameController {
    private PlayerHandler players[] = new PlayerHandler[2];
    private int state = 2;
    private int[][] map = new int[3][3];

    private class PlayerHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private final Object outputLock = new Object();
        private int player;
        private Thread thread;

        public PlayerHandler(Socket socket, int player) {
            this.socket = socket;
            this.player = player;
            try {
                this.outputStream = new ObjectOutputStream(socket.getOutputStream());
                this.inputStream = new ObjectInputStream(socket.getInputStream());
                thread = new Thread(this);
                thread.start();
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                System.out.println(String.format("Game %#x: Player %d connected from %s.", GameController.this.hashCode(), player, socket.getRemoteSocketAddress().toString()));
                PlayerHandler other;
                synchronized (outputLock) {
                    outputStream.writeObject(null);
                    outputStream.writeObject(player + 1);
                }
                if (players[1 - player] != null && !players[1 - player].isAlive()) {
                    synchronized (outputLock) {
                        outputStream.writeObject("The other leaves.");
                        outputStream.writeObject(null);
                        socket.close();
                        return;
                    }
                }
                if (GameController.this.isFull()) {
                    state = 0;
                    synchronized (players[0].outputLock) {
                        players[0].outputStream.writeObject("Your turn.");
                    }
                    synchronized (players[1].outputLock) {
                        players[1].outputStream.writeObject("The other's turn.");
                    }
                } else {
                    outputStream.writeObject("Waiting...");
                }
                while (true) {
                    Integer x = (Integer) inputStream.readObject();
                    if (x == null) {
                        other = players[1 - player];
                        if (other != null && other.isAlive()) {
                            synchronized (other.outputLock) {
                                other.outputStream.writeObject("The other leaves.");
                                other.outputStream.writeObject(null);
                                other.socket.close();
                            }
                        }
                        socket.close();
                        break;
                    }
                    Integer y = (Integer) inputStream.readObject();
                    if (state == player) {
                        if (map[y][x] != 0) continue;
                        map[y][x] = player + 1;
                        state = 1 - state;
                        synchronized (players[0].outputLock) {
                            players[0].outputStream.reset();
                            players[0].outputStream.writeObject(map);
                        }
                        synchronized (players[1].outputLock) {
                            players[1].outputStream.reset();
                            players[1].outputStream.writeObject(map);
                        }
                        // Check for finish
                        int line = 0;
                        if (map[0][0] == player + 1 && map[1][1] == player + 1 && map[2][2] == player + 1)
                            line = 1;
                        else if (map[0][2] == player + 1 && map[1][1] == player + 1 && map[2][0] == player + 1)
                            line = 2;
                        else {
                            for (int i = 0; i < 3; ++i) {
                                if (map[i][0] == player + 1 && map[i][1] == player + 1 && map[i][2] == player + 1)
                                    line = 3 + i;
                                else if (map[0][i] == player + 1 && map[1][i] == player + 1 && map[2][i] == player + 1)
                                    line = 6 + i;
                            }
                        }
                        if (line != 0) {
                            synchronized (players[player].outputLock) {
                                players[player].outputStream.writeObject(line);
                                players[player].outputStream.writeObject("You win!");
                                players[player].outputStream.writeObject(null);
                                players[player].socket.close();
                            }
                            synchronized (players[1 - player].outputLock) {
                                players[1 - player].outputStream.writeObject(line);
                                players[1 - player].outputStream.writeObject("You lose!");
                                players[1 - player].outputStream.writeObject(null);
                                players[1 - player].socket.close();
                            }
                            break;
                        } else {
                            boolean draw = true;
                            for (int i = 0; i < 3; ++i)
                                for (int j = 0; j < 3; ++j)
                                    if (map[i][j] == 0)
                                        draw = false;
                            if (draw) {
                                synchronized (players[player].outputLock) {
                                    players[player].outputStream.writeObject("Draw!");
                                    players[player].outputStream.writeObject(null);
                                    players[player].socket.close();
                                }
                                synchronized (players[1 - player].outputLock) {
                                    players[1 - player].outputStream.writeObject("Draw!");
                                    players[1 - player].outputStream.writeObject(null);
                                    players[1 - player].socket.close();
                                }
                                break;
                            }
                        }
                        synchronized (players[state].outputLock) {
                            players[state].outputStream.writeObject("Your turn.");
                        }
                        synchronized (players[1 - state].outputLock) {
                            players[1 - state].outputStream.writeObject("The other's turn.");
                        }
                    }
                }
            } catch (SocketException error) {
                // Do nothing
            } catch (IOException | ClassNotFoundException error) {
                error.printStackTrace();
            } finally {
                System.out.println(String.format("Game %#x: Player %d disconnected from %s.", GameController.this.hashCode(), player, socket.getRemoteSocketAddress().toString()));
            }
        }

        public boolean isAlive() {
            return socket.isConnected() && thread.isAlive();
        }
    }

    public void addPlayer(Socket socket) {
        if (players[0] == null) {
            players[0] = new PlayerHandler(socket, 0);
        } else {
            players[1] = new PlayerHandler(socket, 1);
        }
    }

    public boolean needPLayer() {
        return players[0] == null || players[0].isAlive() && players[1] == null;
    }

    public boolean isFull() {
        return players[0] != null && players[1] != null;
    }
}
