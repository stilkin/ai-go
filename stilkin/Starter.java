package stilkin;

import java.util.Scanner;

/**
 * Application entry point. Reads the input and passes it to the parser.
 * 
 * @author stilkin
 * 
 */
public class Starter {

    public static void main(String[] args) {
	final GoBot bot = new GoBot();
	final InputParser inputParser = new InputParser();

	// enable the bot to be notified of parser events
	inputParser.addActionRequestListener(bot);

	final Scanner scan = new Scanner(System.in);
	while (scan.hasNextLine()) {
	    final String line = scan.nextLine();
	    if (line.length() != 0) {
		// pass the input to the parser
		inputParser.parse(line);
	    }
	}
	scan.close();
    }
}
