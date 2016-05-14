/**
 * Created by sun on 5/12/16.
 *
 * Login dialog.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginDialog extends JDialog {
    private JTextField host;
    private JTextField port;
    private JLabel information;
    private JButton login;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        getRootPane().getActionMap().put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(5, 5, 5, 5);
        cs.weighty = 0;
        cs.gridx = 0;
        cs.gridy = 0;
        panel.add(new JLabel("Host: "), cs);
        host = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(host, cs);
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(new JLabel("Port: "), cs);
        port = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(port, cs);
        information = new JLabel("", SwingConstants.CENTER);
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 3;
        panel.add(information, cs);
        login = new JButton("Login");
        getRootPane().setDefaultButton(login);
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> close());
        JPanel bp = new JPanel();
        bp.add(login);
        bp.add(btnCancel);
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public String getHost() {
        return host.getText().trim();
    }

    public String getPort() {
        return port.getText().trim();
    }

    public void setHost(String host) {
        this.host.setText(host);
    }

    public void setPort(String port) {
        this.port.setText(port);
    }

    public void setInformation(String info) {
        information.setText(info);
        pack();
    }

    public void setInformationColor(Color color) {
        information.setForeground(color);
    }

    public void addActionListener(ActionListener l) {
        login.addActionListener(l);
    }

    public void removeActionListener(ActionListener l) {
        login.removeActionListener(l);
    }

    public void close() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}