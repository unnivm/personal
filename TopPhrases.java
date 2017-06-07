/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * The over all idea is to use hash and pair the value. Here it goes.
 *
 * Read from the large file and split the phrases based on the delimiter. The
 * array now contains all the 50 phrases (ideally) that are present in the line.
 * Iterate the array and hash these phrases. After this operation, searches for
 * an existing hash in the list. In order to find the same hash in the list, the
 * following process is used:
 *
 * Iterate the list and hash the phrases as we move on. Then compare this hash
 * with the hash we had already computed earlier. If they are same, we increase
 * the frequency and updates the list with the hash, counter and the phrase
 * itself.
 *
 * Once we do for all the entries in the very large file, we sort this list by
 * value using a custom comparator and will write the top 100000 frequent
 * phrases into a disk file
 *
 * Memory requirement is as follows: 10 GB file may contain = 130 million
 * entries Hash function is 32 bytes means:
 *
 * one hash key of a phrase = 32 bytes one million hash keys = 32 MB
 *
 * So the maximum memory it may take for 130 million hash keys = 3 - 4GB.
 *
 * Complexity: O(nlogN)
 *
 * @author Unni Vemanchery Mana
 */
public class TopPhrases {

    /**
     * end of file
     */
    private static final Object EOF = null;

    /**
     * a pipe delimiter used in the file
     */
    private static final String DELIMITER = "\\|";

    /**
     * list that maintains the hash value of phrases
     */
    private static ArrayList<String> list;

    /**
     * very large input file to read from
     */
    private static final String VERY_LARGE_FILE = "C:\\Users\\DELL\\Documents\\NetBeansProjects\\GED2\\src\\com\\wallethub\\file.dat";

    public TopPhrases() {
        list = new ArrayList<>();
    }

    public static void main(String[] arg) {

        Logger.getLogger(TopPhrases.class.getName()).log(Level.INFO, "Processing starts..");

        BufferedReader reader = null;
        list = new ArrayList<>();

        try {
            File file = new File(VERY_LARGE_FILE);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String phrase = null;
            while ((phrase = reader.readLine()) != EOF) {
                String[] phrases = phrase.split(DELIMITER);

                for (String p : phrases) {
                    p = p.trim();
                    int ph = p.hashCode();

                    if (list.isEmpty()) {
                        addPhrase(ph + "@" + 1 + "@" + p);
                    } else {

                        int size = list.size();
                        boolean find = false;

                        for (int i = 0; i < size; i++) {
                            String[] pp = list.get(i).split("@");

                            if (Integer.parseInt(pp[0]) == ph) {
                                int cnt = Integer.parseInt(pp[1]);
                                cnt += 1;
                                updatePhrase(ph + "@" + cnt + "@" + pp[2], i);
                                find = true;
                                break;
                            }
                        }
                        if (!find) {
                            addPhrase(ph + "@" + 1 + "@" + p); //list.add(ph+"@"+1+"@"+p);
                        }
                    }
                }
            }

            // sorts this list with a custom comparator
            Collections.sort(list, new PhraseComparator());

            reader.close();

            Logger.getLogger(TopPhrases.class.getName()).log(Level.INFO, "Processing completed.. Now writing to disk file");

            BufferedWriter writer = new BufferedWriter(new FileWriter("output.dat"));

            int size = list.size();

            for (int i = 0; i < 100000; i++) {

                if ( i >= size ) {
                    break;
                }

                String str = list.get(i);

                // splits the phrases
                String[] phrases = str.split("@");
                String p = phrases[2];
                String cnt = phrases[1];

                // writes phrase and its frequencies to file
                writer.write(p);
                writer.write("->");
                writer.write(cnt);
                writer.write("\n");
            }

            //
            writer.flush();
            writer.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopPhrases.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(TopPhrases.class.getName()).log(Level.SEVERE, null, ioe);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(TopPhrases.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * adds a phrase to list
     *
     * @param phrase
     */
    public static void addPhrase(String phrase) {
        list.add(phrase);
    }

    /**
     * Updates a phrase with current position to the list
     *
     * @param phrase
     * @param index
     */
    public static void updatePhrase(String phrase, int index) {
        list.set(index, phrase);
    }

    /**
     * sorts based on frequencies by descending
     */
    private static class PhraseComparator implements Comparator<String> {

        public PhraseComparator() {
        }

        @Override
        public int compare(String o1, String o2) {
            Integer n2 = Integer.parseInt(o2.split("@")[1]);
            Integer n1 = Integer.parseInt(o1.split("@")[1]);

            return n2.compareTo(n1);
        }
    }

}
