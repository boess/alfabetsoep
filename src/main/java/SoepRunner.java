import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SoepRunner
 */
public class SoepRunner {

    private static List<String> inputFile;
    private static List<Character> charList;
    private static Map<Character, Integer> values = new HashMap<>(26);
    private static String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public static void main(final String args[]) {

        if(args == null || args.length != 1) {
            System.out.println("Arguments are not correct, enter int for the number of innerLoops");
            System.exit(1);
        }

        int innerLoops = 0;

        try {
            innerLoops = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            System.out.println("Arguments are not correct, enter int for the number of innerLoops");
            System.exit(1);
        }

        long start = System.currentTimeMillis();

        //convert the String to a List of characters so we can shuffle it easily later on
        charList = new ArrayList<>(26);
        for(char c : abc.toCharArray()) {
            charList.add(c);
        }

        SoepRunner runner = new SoepRunner();
        Result result = runner.getResult(innerLoops);

        long took = (System.currentTimeMillis() - start) ;

        //write the result to the file so we don't forgot the result
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("results.txt", true))) {
            bw.newLine();
            bw.append("Score: ").append(String.valueOf(result.getScore())).append(" for input: ").append(result.getAlphabet());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Highscore = " + result.getScore());
        System.out.println("Best Input = " + result.getAlphabet());
        System.out.println("Time taken = " + took/1000 +"s");

    }


    /**
     * Turn the file into a List of String to prevent reading the file all the time
     */
    private static void initFile() {

        inputFile = new ArrayList<>(67250);

        try (BufferedReader br = new BufferedReader(new FileReader("english_words.txt")))
        {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                inputFile.add(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Check how many words in the input file have an ascending sequence based on the given alphabet
     * @param alphabet the alphabet to check
     * @return score for this alphabet
     */
    private static int checkFile(String alphabet) {

        if(inputFile == null) {
            initFile();
        }

        int totalScore = 0;

        //convert the input to a map with the values to easily get the value of the letters
        int i = 0;
        for(char c : alphabet.toCharArray()) {
            values.put(c, i);
            i++;
        }

        //check which words have an ascending sequence now
        for(String word : inputFile) {
            totalScore += check(word);
        }

        return totalScore;
    }


    /**
     * Check if the word has an ascending sequence
     * @param word the word to check
     * @return 1 if the word has an ascending sequence, otherwise 0
     */
    private static int check(String word) {

        int prev = -1;

        for(char c : word.toCharArray()) {
            int current = values.get(c);
            if(prev > current) {
                return 0;
            }
            else {
                prev = current;
            }
        }

        return 1;
    }


    /**
     * We try to optimize the score.
     *
     * We do this by setting the best position for each letter and use that result to set the best position for the next letter etc.
     * Because the result changes, we will do it again until the result no longer changes.
     *
     * Because the score is dependent on the sequence in which the best position for each letter is found, we loop several times with
     * a random alphabet to try to find the best score possible.
     *
     * @param loops the number of loops this method will take (each loop takes a randomly shuffled alphabet)
     * @return result object with the best score and the related string
     */
    private Result getResult(int loops) {

        String bestInputOuter = "";
        int prevHigh;
        int highscoreOuter = -1;
        StringBuilder sb;

        System.out.println("Looping "+loops+ " times");


        //since the score is dependent on in which way the letters are processed, we loop a number of times to (hopefully) find the best
        for (int j = 0; j<loops;j++) {

            int highscore = -1;
            String bestInput = abc;

            //since the bestInput will change - we will loop until we do no longer improve the score
            do {
                prevHigh = highscore;

                //find the best position for all the letters in the input string
                for(char c : charList) {
                    //find the best position for this letter
                    //remove the current char from the bestInput
                    String inputWithoutChar = bestInput.replace(c+"", "");

                    //find the best position for this char in the bestInput string
                    for(int i=0; i<26; i++) {
                        sb = new StringBuilder(26);
                        sb.append(inputWithoutChar.substring(0, i));
                        sb.append(c);
                        sb.append(inputWithoutChar.substring(i));
                        int score = checkFile(sb.toString());
                        if(score > highscore) {
                            //we have a new best, 'save' this result and use it for the next iteration
                            highscore = score;
                            bestInput = sb.toString();
                        }
                    }

                }

            }
            while (prevHigh != highscore);

            if(highscore > highscoreOuter) {
                //new best
                bestInputOuter = bestInput;
                highscoreOuter = highscore;
                System.out.println("=== Outer Input changed === " + bestInput);
                System.out.println("=== Outer New highscore === " + highscore);

            }

            //done for this input - shuffle the charList and try again
            Collections.shuffle(charList);
        }

        return new Result(highscoreOuter, bestInputOuter);

    }


    private class Result {
        int score;
        String alphabet;

        public Result(int score, String alphabet) {
            this.score = score;
            this.alphabet = alphabet;
        }


        public int getScore() {
            return score;
        }


        public String getAlphabet() {
            return alphabet;
        }
    }
}
