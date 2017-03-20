/**
 * Created by y.golota on 02.03.2017.
 */
import sun.util.calendar.CalendarUtils;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;
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
    String query = "SELECT * FROM instruments;";

    Connection connection;
    Statement statement;
    ResultSet result;

    JLabel serverNameLabel;
    JLabel databaseNameLabel;
    JLabel userNameLabel;

    JTextPane requestField;
    JTextPane answerField;
    JTextPane consoleField;

    JTable requestResultTable;

    JScrollPane requestPanel;
    JScrollPane answerPanel;
    JScrollPane consolePanel;

    JPanel buttonPanel;

    JButton connectButton;
    JButton submitButton;
    JButton clearRequestButton;
    JButton clearAnswerButton;
    JButton disconnectButton;
    JButton saveToXlsButton;

    boolean connectButtonStatus = true;
    boolean isFirstRun = true;
    boolean isConnectedToDB = false;

    Vector<Vector<String>> resultTable;
    Vector<String> resultTableHeader;

    TitledBorder requestBorder = new TitledBorder("Request to SQL database:");
    TitledBorder answerBorder = new TitledBorder("Answer of SQL server:");
    TitledBorder consoleBorder = new TitledBorder("Console");

    public MySQLClient() {
        super("MySQL Client");

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
        answerPanel = new JScrollPane(null, 20, 30);
        answerPanel.setBounds(10, 220, windowWidth - 20, 400);
        answerPanel.setBorder(answerBorder);
    }

    private void RefreshAnswerPanel(Vector<Vector<String>> resultList, Vector<String> resultHeader) {
        requestResultTable = new JTable(resultList, resultHeader);
        Vector<Integer> columnSizeArray = DetectColumnSize(resultList, resultHeader);
        requestResultTable.setAutoResizeMode(0);
        for (int i = 0; i < resultHeader.size(); i ++)
            requestResultTable.getColumnModel().getColumn(i).setPreferredWidth(columnSizeArray.get(i));
        answerPanel.setViewportView(requestResultTable);
    }

    private Vector<Integer> DetectColumnSize(Vector<Vector<String>> resultList, Vector<String> resultHeader) {
        Vector<Integer> columnSizeArray =  new Vector<>();

        for (int i = 0; i < resultHeader.size(); i ++) {
            columnSizeArray.add(i, 10 * resultHeader.get(i).length());
        }

        for (int i = 0; i < resultList.size(); i ++) {
            try {
                for (int j = 0; j < resultList.get(i).size(); j++) {
                    int originCellSize = columnSizeArray.get(j);
                    int currentCellSize = 8 * resultList.get(i).get(j).length();
                    if (currentCellSize > originCellSize) columnSizeArray.set(j, currentCellSize);
                }
            }
            catch (NullPointerException npe) {}
        }

        return columnSizeArray;
    }

    private void RefreshAnswerPanel(String message) {
        answerField = new JTextPane();
        answerField.setText(message);
        answerPanel.setViewportView(answerField);
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
        buttonPanel.setBounds((int) (windowWidth * 0.4 + 10), 180, (int) (windowWidth * 0.6 - 20), 40);

        connectButton = new JButton("Connect to DB");
        connectButton.setEnabled(connectButtonStatus);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleField.setText("Connecting to " + url);
                connectButtonStatus = false;
                connectButton.setEnabled(connectButtonStatus);
                isConnectedToDB = ConnectToDB();
                if (isConnectedToDB) {
                    consoleField.setText("Connected!");
                    submitButton.setEnabled(isConnectedToDB);
                    isFirstRun = false;
                }
                else {
                    connectButtonStatus = true;
                    connectButton.setEnabled(connectButtonStatus);
                    consoleField.setText("Connection failed!");
                }
            }
        });

        submitButton = new JButton("Submit Request");
        submitButton.setEnabled(isConnectedToDB);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isConnectedToDB) {
                    consoleField.setText("Sending query '" + requestField.getText() + "', wait...");
                    SendQueryToDB(requestField.getText());
                }
                else {
                    JOptionPane.showMessageDialog(null, "There is no connection to DataBase.\nPlease connect first!");
                }
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
                RefreshAnswerPanel("");
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
                    connectButtonStatus = true;
                    isConnectedToDB = false;
                    connectButton.setEnabled(connectButtonStatus);
                    submitButton.setEnabled(isConnectedToDB);
                }
                catch(SQLException sqle) {
                    consoleField.setText("\nSQL closing exception:\n" + sqle.toString());
                }
                catch (NullPointerException npe) {
                    consoleField.setText("\nNull Pointer Exception:\n" + npe.toString());
                }
            }
        });

        saveToXlsButton = new JButton("Save Results to XLS");
        saveToXlsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = "";
                JFileChooser saveFile = new JFileChooser();
                saveFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = saveFile.showDialog(null, "Ok");
                if (result == JFileChooser.APPROVE_OPTION)
                    if (saveFile.getSelectedFile().isDirectory())
                        filename = saveFile.getSelectedFile().getAbsolutePath() + "\\QueryResultExport.xls";
                    else
                        filename = saveFile.getSelectedFile().getAbsolutePath();

                System.out.println(filename);
                Export export = new Export();
                export.setAddRequestListing(true);
                export.setRequestString(requestField.getText());
                export.SaveInstrumentsToXLS(filename, resultTable, resultTableHeader);
            }
        });

        buttonPanel.add(connectButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(clearRequestButton);
        buttonPanel.add(clearAnswerButton);
        buttonPanel.add(disconnectButton);
        buttonPanel.add(saveToXlsButton);
    }

    private boolean ConnectToDB() {
        boolean b = false;
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            if (isFirstRun) RefreshAnswerPanel(connection.getCatalog());
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

    private boolean IsUpdate(String request) {
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
        int j = 1, c = 0;

        resultTable = new Vector<>();
        resultTableHeader = new Vector<>();

        try {
            if (IsUpdate(request)) {
                RefreshAnswerPanel("Server return = " + statement.executeUpdate(request));
            }
            else
                result = statement.executeQuery(request);

            if (result != null) {
                if (!result.next())
                    RefreshAnswerPanel("null");
                else
                    result.absolute(0);

                while (result.next()) {
                    j = 1;
                    Vector<String> currentRow = new Vector<>();

                    while (true) {
                        try {
                            try {
                                String currentCell = result.getString(j);
                                if (c == 0) resultTableHeader.add(result.getMetaData().getColumnName(j));
                                currentRow.add(currentCell);
                            }
                            catch (NullPointerException npe) {
                                System.out.println("Null pointer exception in 'void SendQueryToDB(String request)'");
                            }
                        }
                        catch (SQLException x1) {
                            break;
                        }
                        j ++;
                    }
                    resultTable.add(currentRow);
                    c ++;
                }

                RefreshAnswerPanel(resultTable, resultTableHeader);
            }

        }
        catch (SQLException sqle) {
            consoleField.setText("SQL Exception: " + sqle.toString());
        }
    }

    public static void main (String... args) {
        new MySQLClient();
    }
}