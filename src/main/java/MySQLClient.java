/**
 * Created by y.golota on 02.03.2017.
 */
import sun.util.calendar.CalendarUtils;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.*;
import java.util.Date;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MySQLClient extends JFrame{

    String url;
    String webAddr = "www.db4free.net";
    String databaseName = "instrumentation";
    String useSSLcommand = "?useSSL=false";
    String user = "joshimo";
    String password = "joshimo@list.ru";
    String query = ";";

    Connection connection;
    Statement statement;
    ResultSet result;
    int updateResult;

    JLabel serverNameLabel;
    JLabel databaseNameLabel;
    JLabel userNameLabel;

    JTextPane requestField;
    JTextPane answerField;
    JTextPane consoleField;

    JScrollPane requestPanel;
    JScrollPane answerPanel;
    JScrollPane consolePanel;

    JPanel buttonPanel;

    JButton connectButton;
    JButton submitButton;
    JButton clearRequestButton;
    JButton clearAnswerButton;
    JButton disconnectButton;


    TitledBorder requestBorder = new TitledBorder("Request to SQL database:");
    TitledBorder answerBorder = new TitledBorder("Answer of SQL server:");
    TitledBorder consoleBorder = new TitledBorder("Console");

    JTable dataBaseTable;

    public MySQLClient() {
        super("SQL Client");

        if (JOptionPane.showConfirmDialog(null, "Current database settings:" + "\n\n" +
                "Server: " + webAddr + "\n" +
                "Data base name: " + databaseName +  "\n" +
                "User: " + user + "\n\n" +
                "Do you want to change?") == JOptionPane.OK_OPTION) {
            webAddr = JOptionPane.showInputDialog("Server address:");
            databaseName = JOptionPane.showInputDialog("Data base name:");
            user = JOptionPane.showInputDialog("User name:");
            password = JOptionPane.showInputDialog("Password:");
        }

        url = "jdbc:mysql://" + webAddr + "/" + databaseName + useSSLcommand;
        Init();
    }

    private void Init() {
        int L = 1366;
        int H = 768;

        this.setSize(L, H);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);

        CreateLabels(L);
        CreateRequestPanel(L);
        CreateAnswerPanel(L);
        CreateConsolePanel(L);
        CreateButtonPanel(L);

        this.add(serverNameLabel);
        this.add(databaseNameLabel);
        this.add(userNameLabel);
        this.add(requestPanel);
        this.add(buttonPanel);
        this.add(answerPanel);
        this.add(consolePanel);
        this.setVisible(true);
    }

    private void CreateLabels(int windowWidth){
        serverNameLabel = new JLabel("Server: " + webAddr);
        serverNameLabel.setBounds(10, 10, windowWidth, 16);
        serverNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        databaseNameLabel = new JLabel("Data base: " + databaseName);
        databaseNameLabel.setBounds(10, 30, windowWidth, 16);
        databaseNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        userNameLabel = new JLabel("User: " + user);
        userNameLabel.setBounds(10, 50, windowWidth, 16);
        userNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    private void CreateRequestPanel(int windowWidth) {
        requestField = new JTextPane();
        requestField.setText(query);
        requestField.setCaretPosition(0);
        requestPanel = new JScrollPane(requestField);
        requestPanel.setBorder(requestBorder);
        requestPanel.setBounds(10, 80, windowWidth - 20, 100);
    }

    private void CreateAnswerPanel(int windowWidth) {
        answerField = new JTextPane();
        answerField.setText("");
        answerField.setEditable(false);
        answerPanel = new JScrollPane(answerField);
        answerPanel.setBounds(10, 220, windowWidth - 20, 400);
        answerPanel.setBorder(answerBorder);
    }

    private void CreateConsolePanel(int windowWidth) {
        consoleField = new JTextPane() {
            @SuppressWarnings("deprecation")
            @Override
            public void setText(String t) {
                Date date = new Date() {
                    @Override
                    public String toString() {
                        StringBuilder sb = new StringBuilder(28);
                        CalendarUtils.sprintf0d(sb, super.getDay(), 2).append('-'); //day
                        CalendarUtils.sprintf0d(sb, super.getMonth(), 2).append('-'); //month
                        sb.append(super.getYear() + 1900 + " ");  // yyyy
                        CalendarUtils.sprintf0d(sb, super.getHours(), 2).append(':');   // HH
                        CalendarUtils.sprintf0d(sb, super.getMinutes(), 2).append(':'); // mm
                        CalendarUtils.sprintf0d(sb, super.getSeconds(), 2); // ss
                        return sb.toString();
                    }
                };
                if (super.getText().isEmpty())
                    super.setText(date.toString() + ": " + t);
                else
                    super.setText(super.getText() + "\n" + date.toString() + ": " + t);
            }
        };
        consoleField.setText("ok");
        consoleField.setEditable(false);
        consoleField.setFont(new Font("Arial", Font.ITALIC, 12));
        consoleField.setBackground(new Color(236, 236, 236));
        consolePanel = new JScrollPane(consoleField);
        consolePanel.setBorder(consoleBorder);
        consolePanel.setBounds(10, 620, windowWidth - 20, 120);
    }

    private void CreateButtonPanel(int windowWidth) {
        buttonPanel = new JPanel();
        buttonPanel.setBounds(windowWidth / 2 + 10, 180, windowWidth / 2 - 20, 40);

        connectButton = new JButton("Connect to DB");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleField.setText("Connecting to " + url);
                if (ConnectToDB()) {
                    consoleField.setText("Connected!");
                    connectButton.setEnabled(false);
                }
                else connectButton.setEnabled(true);
            }
        });
        submitButton = new JButton("Submit Request");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleField.setText("Sending query '" + requestField.getText() + "', wait...");
                SendQueryToDB(requestField.getText());
            }
        });
        clearRequestButton = new JButton("Clear Request");
        clearRequestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestField.setText(";");
            }
        });
        clearAnswerButton = new JButton("Clear Answer");
        clearAnswerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                answerField.setText("");
            }
        });
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connection.close();
                    statement.close();
                    if (connection.isClosed()) consoleField.setText("Connection closed...");
                    connectButton.setEnabled(true);
                }
                catch(SQLException sqle) {
                    consoleField.setText("\nSQL closing exception:\n" + sqle.toString());
                }
                catch (NullPointerException npe) {
                    consoleField.setText("\nNull Pointer Exception:\n" + npe.toString());
                }
            }
        });

        buttonPanel.add(connectButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(clearRequestButton);
        buttonPanel.add(clearAnswerButton);
        buttonPanel.add(disconnectButton);
    }

    private boolean ConnectToDB() {
        boolean b = false;
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            answerField.setText(connection.getCatalog());
            b = !connection.isClosed();
        }
        catch (SQLException se) {
            try {
                connection.close();
                statement.close();
                consoleField.setText("Connection closed...");
            }
            catch(SQLException sqle) {
                consoleField.setText("\nSQL closing exception:\n" + sqle.toString());
            }
            catch (NullPointerException npe) {
                consoleField.setText("\nNull Pointer Exception:\n" + npe.toString());
            }
            consoleField.setText("SQL Exception!\n" + se.getMessage());
        }
        finally {
            return b;
        }
    }

    private boolean CheckRequest(String request) {
        return (request.toLowerCase().contains("create") |
                request.toLowerCase().contains("drop") |
                request.toLowerCase().contains("add") |
                request.toLowerCase().contains("insert") |
                request.toLowerCase().contains("update") |
                request.toLowerCase().contains("change") |
                request.toLowerCase().contains("modify") |
                request.toLowerCase().contains("delete"));
    }

    private void SendQueryToDB(String request) {
        String s = "";
        int j = 1;
        try {
            if (CheckRequest(request))
                answerField.setText("Server return = " + statement.executeUpdate(request));
            else
                result = statement.executeQuery(request);

            if (result != null) {
                if (!result.next())
                    s = "null";
                else
                    result.absolute(0);

                while (result.next()) {
                    j = 1;
                    while (true) {
                        try {
                            try {
                                s = s + result.getString(j) + " | ";
                            }
                            catch (NullPointerException npe) {
                                System.out.println("Null pointer exception");
                            }
                        }
                        catch (SQLException x1) {
                            break;
                        }
                        j++;
                    }
                    s = s + "\n" + "---" + "\n";
                }

                answerField.setText(s);
            }

        }
        catch (SQLException sqle) {
            consoleField.setText("SQL Exception: " + sqle.toString());
            answerField.setText(s);
        }
    }

    public static void main (String... args) {
        new MySQLClient();
    }
}
