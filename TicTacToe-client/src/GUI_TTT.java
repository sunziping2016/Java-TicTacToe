import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by sun on 5/13/16.
 *
 * What an ugly name.
 */
public class GUI_TTT extends JFrame {
    public GUI_TTT() {
        super("Tic-Tac-Toe");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        addWindowListener(gamePanel);
        pack();
        setResizable(false);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) ((dimension.getWidth() - getWidth()) / 2), (int) ((dimension.getHeight() - getHeight()) / 2));
    }

    public static void main(String[] args) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, GUI_TTT.class.getResourceAsStream("/fonts/comicbd.ttf")));
        } catch (IOException | FontFormatException error) {
            error.printStackTrace();
        }

        GUI_TTT gui_ttt = new GUI_TTT();
        gui_ttt.setVisible(true);
    }
}
