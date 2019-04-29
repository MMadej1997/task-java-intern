package pl.parser.nbp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FileOperations {


    public static List<String> getFileNamesIfYearsEqual(BufferedReader reader, String  startDate, String endDate) throws  IOException{
        List<String> fileNames = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.matches("c\\d{3}z" + startDate)) {
                fileNames.add(line);
                while (!(line = reader.readLine()).matches("c\\d{3}z" + endDate))
                    if (line.startsWith("c"))
                        fileNames.add(line);

                fileNames.add(line);
                break;
            }
        }
        return fileNames;
    }


    public static List<String> readFileNamesIfYearNotEquals(BufferedReader reader, String startYear, String endYear, String startDate, String endDate) throws Exception{
        List<String> fileNames = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("c\\d{3}z" + startDate)) {
                fileNames.add(line);
                while ((line = reader.readLine()) != null)
                    if (line.startsWith("c"))
                        fileNames.add(line);
                break;
            }
        }


        Integer startYearInt = Integer.parseInt(startYear);
        Integer endYearInt = Integer.parseInt(endYear);
        int difference = Integer.parseInt(endYear) - startYearInt;
        String siteURL;
        URL url;
        URLConnection connection;

        for (int i = 1; i < difference; i++) {

            siteURL = String.format("http://www.nbp.pl/kursy/xml/dir%d.txt", startYearInt + i);
            url = new URL(siteURL);
            connection = url.openConnection();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            line = reader.readLine();
            //consider problem with illegalcharacter: '\ufeff' related with Byte Order Mark
            line = line.substring(line.indexOf("c"));
            fileNames.add(line);

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("c"))
                    fileNames.add(line);
            }
        }


        siteURL = String.format("http://www.nbp.pl/kursy/xml/dir%s.txt", endYear);
        url = new URL(siteURL);
        connection = url.openConnection();
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        //condition consider problem with illegalcharacter: '\ufeff' related with Byte Order Mark
        line = reader.readLine();
        line = line.substring(line.indexOf("c"));
        fileNames.add(line);


        while (!(line = reader.readLine()).matches("c\\d{3}z" + endDate)) {
            if (line.startsWith("c"))
                fileNames.add(line);
        }

        fileNames.add(line);
        return fileNames;
    }
}
