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

    /*String url;
    String webAddr = "localhost:3306";
    String databaseName = "world";
    String useSSLcommand = "?useSSL=false";
    String user = "joshimo";
    String password = "sakamoto";
    String query = ";";*/

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

    JPanel mainButtonPanel;
    JPanel navigateButtonPanel;

    JButton connectButton;
    JButton submitButton;
    JButton clearRequestButton;
    JButton clearAnswerButton;
    JButton disconnectButton;
    JButton saveToXlsButton;
    JButton prevButton;
    JButton nextButton;

    boolean connectButtonStatus = true;
    boolean isFirstRun = true;
    boolean isConnectedToDB = false;

    int requestCounter = 0;

    String request;

    Vector<Vector<String>> resultTable;
    Vector<String> resultTableHeader;
    Vector<String> requestList;

    TitledBorder requestBorder = new TitledBorder("Request to SQL database:");
    TitledBorder answerBorder = new TitledBorder("Answer of SQL server:");
    TitledBorder consoleBorder = new TitledBorder("Console");

    static int scrWidth;
    static int scrHeight;
    static double kW;
    static double kH;

    static int L;
    static int H;

    static {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        scrWidth = (int) screen.getWidth();
        scrHeight = (int) screen.getHeight();
        if (scrWidth >= 1366) {
            if (scrWidth < 1600) {
                L = 1366;
                H = 768;
            }
            else {
                L = (int) (0.85 * scrWidth);
                H = (int) (0.85 * scrHeight);
            }
        }
        else {
            L = scrWidth;
            H = scrHeight;
        }

        if (scrWidth < 1152) {
            JOptionPane.showMessageDialog(null, "Unsupported screen resolution!");
            System.exit(0);
        }

        kW = 1.0 * L / 1366;
        kH = 1.0 * H / 768;
    }

    public MySQLClient() {
        super("MySQL Client");
        ImageIcon frameIcon = new ImageIcon("src\\main\\resources\\Icons\\sql.png");
        this.setIconImage(frameIcon.getImage());

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
        System.out.println("url = " + url);
        requestList = new Vector<>();

        Init();
    }

    private void Init() {

        if (scrWidth > 1366)
            this.setLocation((int) (0.5 * (scrWidth - L)), (int) (0.5 * (scrHeight - H)));

        this.setSize(L, H);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);

        CreateLabels(L);
        CreateRequestPanel(L);
        CreateAnswerPanel(L);
        CreateConsolePanel(L);
        CreateMainButtonPanel(L);
        CreateNavigateButtonPanel(L);

        this.add(serverNameLabel);
        this.add(databaseNameLabel);
        this.add(userNameLabel);
        this.add(requestPanel);
        this.add(mainButtonPanel);
        this.add(navigateButtonPanel);
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
        requestPanel.setBounds(10, 80, windowWidth - 20, (int) (110 * kH));
    }

    private void CreateAnswerPanel(int windowWidth) {
        answerPanel = new JScrollPane(null, 20, 30);
        answerPanel.setBounds(10, (int) (240 * kH), windowWidth - 20, (int) (400 * kH));
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
        consolePanel.setBounds(10, (int) (640 * kH), windowWidth - 20, (int) (100 * kH));
    }

    private void CreateMainButtonPanel(int windowWidth) {
        mainButtonPanel = new JPanel();
        if (kW > 0.94)
            mainButtonPanel.setBounds((int) (windowWidth * 0.35 + 10), (int) (190 * kH), (int) (windowWidth * 0.65 - 20), (int) (50 * kH));
        else
            mainButtonPanel.setBounds((int) (windowWidth * 0.25 + 10), (int) (190 * kH), (int) (windowWidth * 0.75 - 20), (int) (50 * kH));

        ImageIcon connectButtonIcon = new ImageIcon("src\\main\\resources\\Icons\\connect.png");
        ImageIcon submitButtonIcon = new ImageIcon("src\\main\\resources\\Icons\\submit.png");
        ImageIcon disconnectButtonIcon = new ImageIcon("src\\main\\resources\\Icons\\disconnect.png");
        ImageIcon saveToXlsButtonIcon = new ImageIcon("src\\main\\resources\\Icons\\export.png");
        ImageIcon clearIcon = new ImageIcon("src\\main\\resources\\Icons\\clear.png");

        connectButton = new JButton("Connect to DB", connectButtonIcon);
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

        submitButton = new JButton("Submit Request", submitButtonIcon);
        submitButton.setEnabled(isConnectedToDB);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isConnectedToDB) {
                    request = requestField.getText();
                    requestList.add(request);
                    requestCounter = requestList.size();
                    nextButton.setEnabled(false);
                    if (requestList.size() > 1) prevButton.setEnabled(true);
                    consoleField.setText("Sending query '" + requestField.getText() + "', wait...");
                    SendQueryToDB(request);
                }
                else {
                    JOptionPane.showMessageDialog(null, "There is no connection to DataBase.\nPlease connect first!");
                }
            }
        });

        clearRequestButton = new JButton("Clear Request", clearIcon);
        clearRequestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestField.setText(";");
            }
        });

        clearAnswerButton = new JButton("Clear Answer", clearIcon);
        clearAnswerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RefreshAnswerPanel("");
            }
        });

        disconnectButton = new JButton("Disconnect", disconnectButtonIcon);
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

        saveToXlsButton = new JButton("Save to XLS", saveToXlsButtonIcon);
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

        mainButtonPanel.add(connectButton);
        mainButtonPanel.add(submitButton);
        mainButtonPanel.add(clearRequestButton);
        mainButtonPanel.add(clearAnswerButton);
        mainButtonPanel.add(disconnectButton);
        mainButtonPanel.add(saveToXlsButton);
    }

    private void CreateNavigateButtonPanel(int windowWidth) {
        navigateButtonPanel = new JPanel();
        navigateButtonPanel.setBounds(10, (int) (190 * kH), (int) (windowWidth * 0.25 - 20), (int) (50 * kH));

        ImageIcon prevButtonIcon = new ImageIcon("src\\main\\resources\\Icons\\undo.png");
        ImageIcon nextButtonIcon = new ImageIcon("src\\main\\resources\\Icons\\redo.png");

        prevButton = new JButton("Prev Request", prevButtonIcon);
        prevButton.setEnabled(false);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (requestCounter > 0) {
                    requestCounter --;
                    nextButton.setEnabled(true);
                    request = requestList.get(requestCounter);
                    requestField.setText(request);
                    if (requestCounter == 0) prevButton.setEnabled(false);
                }
            }
        });

        nextButton = new JButton("Next Request", nextButtonIcon);
        nextButton.setEnabled(false);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (requestCounter < requestList.size() - 1) {
                    requestCounter ++;
                    prevButton.setEnabled(true);
                    request = requestList.get(requestCounter);
                    requestField.setText(request);
                    if (requestCounter == requestList.size() - 1) nextButton.setEnabled(false);
                }
            }
        });

        navigateButtonPanel.add(prevButton);
        navigateButtonPanel.add(nextButton);
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
        return (request.toLowerCase().contains("create ") |
                request.toLowerCase().contains("drop ") |
                request.toLowerCase().contains("add ") |
                request.toLowerCase().contains("insert ") |
                request.toLowerCase().contains("update ") |
                request.toLowerCase().contains("change ") |
                request.toLowerCase().contains("modify ") |
                request.toLowerCase().contains("use ") |
                request.toLowerCase().contains("delete "));
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
                                if (c == 0) resultTableHeader.add(result.getMetaData().getColumnLabel(j));
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