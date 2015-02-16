package es.eucm.ead.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ConflictUtil {

    /**
     * Performs a three-way merge of three sets of "grocery items" into one set.
     *
     * @param baseStr Items local modifications are based on.
     * @param currentStr Items currently on the server.
     * @param modifiedStr Locally modified items.
     * @return Items merged from three sets of items provided.
     */
    public static String resolveConflict(String baseStr, String currentStr, String modifiedStr) {
        List<String> baseItems = Arrays.asList(baseStr.split("\n"));
        List<String> currentItems = Arrays.asList(currentStr.split("\n"));
        List<String> modifiedItems = Arrays.asList(modifiedStr.split("\n"));

        List<String> allItems = new ArrayList<String>();

        // Add unique items to allItems.
        allItems.addAll(baseItems);

        for(String item: currentItems) {
            if (!allItems.contains(item)) {
                allItems.add(item);
            }
        }

        for(String item: modifiedItems) {
            if (!allItems.contains(item)) {
                allItems.add(item);
            }
        }

        // Remove items that were removed from currentItems or modifiedItems.
        for (Iterator<String> iter = allItems.iterator(); iter.hasNext();) {
            String item = iter.next();
            if (baseItems.contains(item) && (!currentItems.contains(item)
                    || !modifiedItems.contains(item))) {
                iter.remove();
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String item : allItems) {
            stringBuilder.append(item);
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * Gets String from InputStream.
     *
     * @param is InputStream used to read into String.
     * @return String resulting from reading is.
     */
    public static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}
