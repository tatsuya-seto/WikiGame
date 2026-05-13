import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WikiGame {

    private JFrame mainFrame;
    private JTextField startField, endField;
    private JTextArea logArea;
    private JButton searchButton, stopButton;
    private JLabel statusLabel;

    private int maxDepth = 2;
    private ArrayList<String> path = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WikiGame::new);
    }

    public WikiGame() {
        initGUI();
    }

    private void initGUI() {
        mainFrame = new JFrame("Wikipedia Game Solver");
        mainFrame.setSize(1000, 720);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout(10, 10));

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Search Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        inputPanel.add(new JLabel("Start Article (/wiki/...):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        startField = new JTextField("/wiki/Banana", 35);
        inputPanel.add(startField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("End Article (/wiki/...):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        endField = new JTextField("/wiki/Milton_Academy", 35);
        inputPanel.add(endField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchButton = new JButton("Find Path");
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        buttonPanel.add(searchButton);
        buttonPanel.add(stopButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Search Log"));

        // Status Bar
        statusLabel = new JLabel("Enter Wikipedia article paths and click Find Path.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        mainFrame.add(inputPanel, BorderLayout.NORTH);
        mainFrame.add(scrollPane, BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> startSearch());
        stopButton.addActionListener(e -> {
            stopButton.setEnabled(false);
            statusLabel.setText("Stopping...");
        });

        mainFrame.setVisible(true);
    }

    private void startSearch() {
        String startLink = startField.getText().trim();
        String endLink = endField.getText().trim();
        path.clear();
        logArea.setText("");
        searchButton.setEnabled(false);
        stopButton.setEnabled(true);

        new Thread(() -> {
            if (findLink(startLink, endLink, 0)) {
                path.add(0, startLink);
                log("Found it! Path: " + String.join(" -> ", path));
                SwingUtilities.invokeLater(() -> statusLabel.setText("Path found in " + (path.size() - 1) + " steps!"));
            } else {
                log("Did not find it.");
                SwingUtilities.invokeLater(() -> statusLabel.setText("No path found."));
            }
            SwingUtilities.invokeLater(() -> {
                searchButton.setEnabled(true);
                stopButton.setEnabled(false);
            });
        }).start();
    }

    // Recursion method
    public boolean findLink(String currentLink, String endLink, int depth) {
        log("depth is: " + depth + ", link is: https://en.wikipedia.org" + currentLink);

        // BASE CASE: we reached the target
        if (currentLink.equals(endLink)) {
            return true;
        }
        //base case: gone too deep
        else if (depth >= maxDepth) {
            return false;
        }
        //general recursive case
        else {
            ArrayList<String> links = getLinks(currentLink);
            for (String link : links) {
                if (findLink(link, endLink, depth + 1)) {
                    path.add(0, link);
                    return true;
                }
            }
        }

        return false;
    }



    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}