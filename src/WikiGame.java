import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WikiGame {

    private JFrame mainFrame;
    private JTextField startField, endField, depthField;
    private JTextArea logArea;
    private JButton searchButton, stopButton;
    private JLabel statusLabel;

    private ArrayList<String> path = new ArrayList<>();
    private ArrayList<String> visited = new ArrayList<>();
    private volatile boolean stopped = false;

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
        startField = new JTextField("/wiki/G7", 35);
        inputPanel.add(startField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("End Article (/wiki/...):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        endField = new JTextField("/wiki/Great_Depression", 35);
        inputPanel.add(endField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchButton = new JButton("Find Path");
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        buttonPanel.add(searchButton);
        buttonPanel.add(stopButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        inputPanel.add(new JLabel("Depth"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        depthField = new JTextField("2", 35);
        inputPanel.add(depthField, gbc);

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
            stopped = true;
            stopButton.setEnabled(false);
            statusLabel.setText("Stopping...");
        });

        mainFrame.setVisible(true);
    }

    private void startSearch() {
        String startLink = startField.getText().trim();
        String endLink = endField.getText().trim();
        path.clear();
        stopped = false;
        logArea.setText("");
        searchButton.setEnabled(false);
        stopButton.setEnabled(true);

        new Thread(() -> {
            int maxDepth = Integer.parseInt(depthField.getText().trim());
            boolean found = false;
            for (int d = 1; d <= maxDepth && !stopped; d++) {
                log("\n--- Trying depth " + d + " ---");
                path.clear();
                visited.clear();
                if (findLink(startLink, endLink, 0, d)) {
                    path.add(0, startLink);
                    log("Found it! Path: " + String.join(" -> ", path));
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Path found in " + (path.size() - 1) + " steps!"));
                    found = true;
                    break;
                }
            }
            if (!found && !stopped) {
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
    public boolean findLink(String currentLink, String endLink, int depth, int maxDepth) {
        if (stopped) return false;

        log("depth is: " + depth + ", link is: https://en.wikipedia.org" + currentLink);

        // BASE CASE: we reached the target
        if (currentLink.equals(endLink)) {
            return true;
        }
        // BASE CASE: gone too deep
        else if (depth >= maxDepth) {
            return false;
        }
        // GENERAL RECURSIVE CASE
        else {
            visited.add(currentLink);
            ArrayList<String> links = getLinks(currentLink);
            for (String link : links) {
                if (stopped) return false;
                if (!visited.contains(link) && findLink(link, endLink, depth + 1, maxDepth)) {
                    path.add(0, link);
                    return true;
                }
            }
        }
        return false;
    }

    // Fetches all wiki links from a page by scraping href from the HTML
    private ArrayList<String> getLinks(String wikiPath) {
        ArrayList<String> links = new ArrayList<>();
        String html = "";

        try {
            URL url = new URL("https://en.wikipedia.org" + wikiPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "WikiGameSolver/1.0");

            if (conn.getResponseCode() != 200) {
                log("Failed to load: " + wikiPath);
                return links;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                html += line;
            }
            conn.disconnect();

        } catch (Exception e) {
            log("Error loading page: " + wikiPath);
            return links;
        }

        // Scan the HTML for href="/wiki/..." and pull out each link
        int pos = 0;
        while (pos < html.length()) {
            int hrefIdx = html.indexOf("href=\"/wiki/", pos);
            if (hrefIdx == -1) break;

            hrefIdx += 6; // move past href="  to the start of /wiki/
            int end = html.indexOf("\"", hrefIdx);
            if (end == -1) break;

            String link = html.substring(hrefIdx, end);

            // Skip special pages
            if (!link.contains(":") && !link.contains("#") && !links.contains(link) && !link.equals("/wiki/Main_Page")) {
                links.add(link);
            }

            pos = end + 1;
        }

        return links;
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}