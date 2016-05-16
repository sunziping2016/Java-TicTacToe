import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * Created by sun on 5/13/16.
 *
 * Game panel.
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, WindowListener, Runnable {
    private static final Image logo = ImageLoader.getImage("logo.png");
    private static final Image background = ImageLoader.getImage("background.jpg");
    private static final Image cross = ImageLoader.getImage("cross.png");
    private static final Image circle = ImageLoader.getImage("circle.png");
    private static final Image diagonal1 = ImageLoader.getImage("diagonal1.png");
    private static final Image diagonal2 = ImageLoader.getImage("diagonal2.png");
    private static final Image hline = ImageLoader.getImage("hline.png");
    private static final Image vline = ImageLoader.getImage("vline.png");
    private static final Image crossTransparent = ImageLoader.getImageTransparent("cross.png", 0.3f);
    private static final Image circleTransparent = ImageLoader.getImageTransparent("circle.png", 0.3f);

    private static final int[] blockX = new int[]{20, 111, 204, 300};
    private static final int[] blockY = new int[]{98, 190, 283, 378};

    private String host = "tencent.sunziping.com";
    private String port = "9797";

    private int player = 0;
    private int map[][] = new int[3][3];
    private String message = "Welcome!";
    private int line = 0;

    private int mouseX = -1, mouseY = -1;

    private Thread thread = new Thread(this);
    private Socket socket;
    private ObjectOutputStream outputStream;
    private final Object outputLock = new Object();
    private ObjectInputStream inputStream;

    public GamePanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        thread.start();
    }

    public void reset() {
        player = 0;
        map = new int[3][3];
        message = "Welcome!";
        line = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int x, y;
        g = g.create();
        g.drawImage(background, 0, 0, null);
        g.drawImage(logo, (background.getWidth(null) - logo.getWidth(null)) / 2, 0, null);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                x = blockX[j] + (blockX[j + 1] - blockX[j] - 70) / 2;
                y = blockY[i] + (blockY[i + 1] - blockY[i] - 70) / 2;
                switch (map[i][j]) {
                    case 1:
                        g.drawImage(cross, x, y, null);
                        break;
                    case 2:
                        g.drawImage(circle, x, y, null);
                        break;
                    default:
                        break;
                }
            }
        }
        if (mouseX >= 0 && mouseY >= 0 && map[mouseY][mouseX] == 0 && player != 0) {
            x = blockX[mouseX] + (blockX[mouseX + 1] - blockX[mouseX] - 70) / 2;
            y = blockY[mouseY] + (blockY[mouseY + 1] - blockY[mouseY] - 70) / 2;
            switch (player) {
                case 1:
                    g.drawImage(crossTransparent, x, y, null);
                    break;
                case 2:
                    g.drawImage(circleTransparent, x, y, null);
                    break;
                default:
                    break;
            }
        }
        if (!message.isEmpty()) {
            g.setFont(new Font("Comic Sans MS", 0, 32));
            g.drawString(message, (background.getWidth(null) - g.getFontMetrics().stringWidth(message)) / 2, 444);
        }
        if (line != 0) {
            int left = 0, right = 0, up = 0, down = 0;
            Image image = null;
            switch (line) {
                case 1:case 2:
                    left = blockX[0]; right = blockX[3];
                    up = blockY[0]; down = blockY[3];
                    if (line == 1)
                        image = diagonal1;
                    else
                        image = diagonal2;
                    break;
                case 3:case 4:case 5:
                    left = blockX[0]; right = blockX[3];
                    up = blockY[line - 3]; down = blockY[line - 2];
                    image = hline;
                    break;
                case 6:case 7:case 8:
                    left = blockX[line - 6]; right = blockX[line - 5];
                    up = blockY[0]; down = blockY[3];
                    image = vline;
                    break;
                default:
                    break;
            }
            if (image != null) {
                x = left + (right - left - image.getWidth(null)) / 2;
                y = up + (down - up - image.getHeight(null)) / 2;
                g.drawImage(image, x, y, null);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(background.getWidth(null), background.getHeight(null));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        if (e.getX() < blockX[0] || e.getX() >= blockX[3] || e.getY() < blockY[0] || e.getY() >= blockY[3])
            return;
        int x = -Arrays.binarySearch(blockX, e.getX()) - 2;
        int y = -Arrays.binarySearch(blockY, e.getY()) - 2;
        if (x < 0 || y < 0)
            return;
        synchronized (outputLock) {
            if (outputStream != null) {
                try {
                    outputStream.writeObject(x);
                    outputStream.writeObject(y);
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
        }
        if (!thread.isAlive()) {
            reset();
            thread = new Thread(this);
            thread.start();
        }
        repaint();
    }

    @Override
    public void run() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        LoginDialog loginDialog = new LoginDialog(topFrame);
        loginDialog.setHost(host);
        loginDialog.setPort(port);
        loginDialog.setInformationColor(Color.RED);
        loginDialog.addActionListener(e -> {
            try {
                if (loginDialog.getHost().isEmpty()) {
                    loginDialog.setInformation("Host must not be empty.");
                    return;
                }
                if (loginDialog.getPort().isEmpty()) {
                    loginDialog.setInformation("Port must not be empty.");
                    return;
                }
                socket = new Socket();
                socket.setSoTimeout(1000);
                socket.connect(new InetSocketAddress(loginDialog.getHost(), Integer.parseInt(loginDialog.getPort())), 1000);
                synchronized (outputLock) {
                    outputStream = new ObjectOutputStream(socket.getOutputStream());
                }
                inputStream = new ObjectInputStream(socket.getInputStream());
                inputStream.readObject();
                socket.setSoTimeout(0);
                loginDialog.close();
                host = loginDialog.getHost();
                port = loginDialog.getPort();
            } catch (EOFException | StreamCorruptedException error) {
                loginDialog.setInformation("Unrecognized host.");
            } catch (ConnectException error) {
                if (error.getMessage().equals("Invalid argument"))
                    loginDialog.setInformation("Invalid host.");
                else
                    loginDialog.setInformation(error.getMessage() + ".");
            } catch (SocketTimeoutException error) {
                loginDialog.setInformation("Connection timeout.");
            } catch (UnknownHostException error) {
                loginDialog.setInformation("Unknown host.");
            } catch (NumberFormatException error) {
                loginDialog.setInformation("Invalid port.");
            } catch (IllegalArgumentException error) {
                loginDialog.setInformation("Port out of range.");
            } catch (Exception error) {
                loginDialog.setInformation(error.getClass().getName());
            }
        });
        loginDialog.setVisible(true);
        if (socket == null || !socket.isConnected())
            return;
        try {
            setTitle("Tic-Tac-Toe (Connected)");
            player = (int) inputStream.readObject();
            while (true) {
                Object object = inputStream.readObject();
                if (object == null) {
                    break;
                }
                else if (object instanceof String)
                    message = (String) object;
                else if (object instanceof int[][])
                    map = (int[][]) object;
                else if (object instanceof Integer)
                    line = (Integer) object;
                else
                    System.err.println(object);
                repaint();
            }
            socket.close();
            synchronized (outputLock) {
                outputStream = null;
            }
            inputStream = null;
            socket = null;
        } catch (EOFException error) {
            // Do nothing
        } catch (IOException | ClassNotFoundException error) {
            error.printStackTrace();
        } finally {
            setTitle("Tic-Tac-Toe (Disconnected)");
        }
    }

    public Dimension getMaximumSize() { return getPreferredSize(); }
    public Dimension getMinimumSize() { return getPreferredSize(); }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = -Arrays.binarySearch(blockX, e.getX()) - 2;
        int y = -Arrays.binarySearch(blockY, e.getY()) - 2;
        if (x < 0 || x >= 3 || y < 0 || y >= 3)
            x = y = -1;
        if (mouseX != x || mouseY != y) {
            mouseX = x;
            mouseY = y;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) { }
    @Override
    public void mouseEntered(MouseEvent e) { }
    @Override
    public void mouseExited(MouseEvent e) { }
    @Override
    public void mousePressed(MouseEvent e) { }
    @Override
    public void mouseReleased(MouseEvent e) { }

    public void close() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispatchEvent(new WindowEvent(topFrame, WindowEvent.WINDOW_CLOSING));
    }

    public void setTitle(String title) {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.setTitle(title);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (socket != null && socket.isConnected()) {
            try {
                outputStream.writeObject(null);
                thread.join();
            } catch (IOException | InterruptedException error) {
                error.printStackTrace();
            }
        }
    }

    @Override
    public void windowIconified(WindowEvent e) { }
    @Override
    public void windowDeiconified(WindowEvent e) { }
    @Override
    public void windowOpened(WindowEvent e) { }
    @Override
    public void windowClosed(WindowEvent e) { }
    @Override
    public void windowActivated(WindowEvent e) { }
    @Override
    public void windowDeactivated(WindowEvent e) { }
}
