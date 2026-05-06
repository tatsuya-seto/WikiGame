import java.util.ArrayList;

public class WikiGame {

    private int maxDepth;
    private ArrayList<String> path = new ArrayList<>();

    public static void main(String[] args) {
        WikiGame w = new WikiGame();
    }

    public WikiGame() {

        String startLink = "";  // beginning link, where the program will start
        String endLink = "";    // ending link, where the program is trying to get to
        maxDepth = 1;           // start this at 1 or 2, and if you get it going fast, increase

        if (findLink(startLink, endLink, 0)) {
            System.out.println("found it********************************************************************");
            path.add(startLink);
        } else {
            System.out.println("did not found it********************************************************************");
        }

    }

    // method
    public boolean findLink(String startLink, String endLink, int depth) {

        System.out.println("depth is: " + depth + ", link is: https://en.wikipedia.org" + startLink);

        // BASE CASE
        if () {

        } else if () {

        }

        // GENERAL RECURSIVE CASE
        else {

        }

        return false;
    }

}