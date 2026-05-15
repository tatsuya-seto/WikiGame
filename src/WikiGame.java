import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class WikiGame {

    private JFrame mainFrame;
    private JTextField startField, endField;
    private JTextArea logArea;
    private JButton searchButton, stopButton;
    private JLabel statusLabel;

    private int maxDepth = 5;
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
            if (!link.contains(":") && !link.contains("#") && !links.contains(link)) {
                links.add(link);
            }

            pos = end + 1;
        }

        return links;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand(); //which button was pressed?
        if (command.equals("go")) { //checks if command was go(go is the command for the go button)
            String urlText = Link.getText().trim(); //link input
            String searchword = ta.getText().trim(); //search term input
            //Getting url and searchword texts

            if (urlText.isEmpty() || searchword.isEmpty()) {
                outputArea.setText("Please enter both a link and a search word");
                return;
            }//Text returns this error if both a search term and a link is not inserted

            String allLinks = ""; //stores matching links found
            try {
                URL url = new URL(urlText); //creates url object from input

                URLConnection urlc = url.openConnection();
                urlc.setRequestProperty("User-Agent", "Mozilla 5.0 (Windows; U; " + "Windows NT 5.1; en-US; rv:1.8.0.11) "); //opens connection to url

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(urlc.getInputStream())
                ); //reading
                String line;
                while ((line = reader.readLine()) != null) {//reading line by line
                    int pos = 0; //start position for searching within the line
                    while ((pos = line.indexOf("href=", pos)) != -1) { //find EACh occurance of href
                        int start = pos + 5; //moving past href= (5 chars so +5)
                        char quote = line.charAt(start); //Getting the quote char type
                        if (quote == '"' || quote == '\'') {
                            int end = line.indexOf(quote, start + 1); //Finding closing quote
                            if (end != -1) { //when closing quote found
                                String link = line.substring(start + 1, end); //extract quote
                                if (link.contains(searchword)) { //checking if it has search term
                                    allLinks += link + "\n"; //adding to results
                                }
                                pos = end + 1; // continue searching rest of line
                            } else break; //stop if no closing quote
                        } else break; //stop if no other href
                    }
                }
                reader.close();
            } catch (Exception ex) {//for when it fails
                allLinks = "Error: " + ex.getMessage();
            }

            if (allLinks.isEmpty()) {
                allLinks = "No links found containing \"" + searchword + "\""; //if no matches are found
            }   else {
                //build HTML with clickable links
                StringBuilder html = new StringBuilder("<html><body>");
                String[] lines = allLinks.split("\\R");   // split on newlines
                //iterates through each found link
                for (String link : lines) {
                    link = link.trim();//removing extra spaces
                    if (link.isEmpty()) continue; //skips empty lines

                    //adds eahc link as a clickable html anchor
                    html.append("<a href=\"")
                            .append(link)
                            .append("\">")
                            .append(link)
                            .append("</a><br>");
                }

                html.append("</body></html>"); //closes html structure

                outputArea.setText(html.toString()); //displays clickable links in outputarea
            }

        }
