package plugindata.utils;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PnmlFileHandler {

    public static void fixPnmlFile(String pathToPnml) {

        try {
            // input the file content to the StringBuffer "input"
            BufferedReader br = new BufferedReader(new FileReader(pathToPnml));
            StringBuffer res = new StringBuffer();

            // Ignoring <xml> and <pnml> lines in heading
            br.readLine();
            br.readLine();

            res.append("<pnml xmlns=\"http://www.pnml.org/version-2009/grammar/pnml\">");
            res.append('\n');

            String fileName = new File(pathToPnml).getName();
            fileName = fileName.substring(0, fileName.indexOf('.'));

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("net id")) {
                    res.append(line.replaceFirst("net1", fileName));
                    res.append('\n');
                } else if (line.contains("page id")) {
                    res.append(line.replaceFirst("n0", "_" + fileName));
                    res.append('\n');
                } else if (line.contains("initialMarking")) {
                    br.readLine();
                    br.readLine();
                } else if (line.contains("arc id")) {
                    String toBeReplaced = StringUtils.substringBetween(line, "\"");
                    line = line.replace(toBeReplaced, "_" + toBeReplaced + "_" + fileName);
                    res.append(line);
                    res.append('\n');
                } else {
                    res.append(line);
                    res.append('\n');
                }
            }
            br.close();

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(pathToPnml);
            fileOut.write(res.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
    }
}
