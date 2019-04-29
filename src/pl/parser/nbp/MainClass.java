package pl.parser.nbp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainClass {




    //additional method, which helps to get user inputs instead of using system args
    //it returns String array with choosen currency, start date, end date
    public static String[] collectUserInput (){
        Scanner scanner = new Scanner(System.in);
        String[] arr = new String[3];

        System.out.println("Podaj kod waluty:   (Spośród: USD, EUR, CHF, GBP)");
        arr[0] = scanner.nextLine();

        while( !Arrays.asList("USD", "EUR", "CHF", "GBP").contains(arr[0])){
            System.out.println("Podałeś błędny kod waluty, podaj go jeszcze raz:   (Spośród: USD, EUR, CHF, GBP)");
            arr[0] = scanner.nextLine();
        }

        System.out.println("Podaj datę początkową w formacie rok-miesiąc-dzień:");
        arr[1] = scanner.nextLine();
        while (true){
            try {
                LocalDate startTime = LocalDate.parse(arr[1], DateTimeFormatter.ISO_LOCAL_DATE);
                break;
            } catch (DateTimeException e) {
                System.out.println("Podałeś błędną datę, podaj ją jeszcze raz w formacie rok-miesiąc-dzień:");
                arr[1] = scanner.nextLine();
            }
        }

        System.out.println("Podaj datę końcową w formacie rok-miesiąc-dzień:");
        arr[2] = scanner.nextLine();
        while (true){
            try {
                LocalDate startTime = LocalDate.parse(arr[2], DateTimeFormatter.ISO_LOCAL_DATE);
                break;
            } catch (DateTimeException e) {
                System.out.println("Podałeś błędną datę, podaj ją jeszcze raz w formacie rok-miesiąc-dzień:");
                arr[2] = scanner.nextLine();
            }
        }
        return arr;
    }




    public static List<String> readFileNamesFromSite(String startDate, String endDate){
        List<String> fileNames = new ArrayList<>();
        BufferedReader reader = null;
        try{
            String startYear = getYear(startDate);
            String endYear   = getYear(endDate);

            startDate = modifyDate(startDate);
            endDate   =  modifyDate(endDate);

            String siteURL = String.format("http://www.nbp.pl/kursy/xml/dir%s.txt", startYear);
            URL url = new URL(siteURL);
            URLConnection connection = url.openConnection();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            if(startYear.equals(endYear)) {
                fileNames.addAll(FileOperations.getFileNamesIfYearsEqual(reader, startDate, endDate));
            }else{
                fileNames.addAll(FileOperations.readFileNamesIfYearNotEquals(reader,startYear, endYear, startDate, endDate));
            }

        }catch (Exception exc){
            exc.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileNames;
    }


    public static String modifyDate(String date){
        return date.replace("-","").substring(2);
    }


    public static String getYear(String str){
        Pattern pattern = Pattern.compile("(\\d{4})-\\d{2}-\\d{2}");
        Matcher matcher = pattern.matcher(str);
        matcher.matches();
        return matcher.group(1);
    }

    public static String getIdentifier(String str){
        Pattern pattern = Pattern.compile(".*c(\\d{3})z");
        Matcher matcher = pattern.matcher(str);
        matcher.matches();
        return matcher.group(1);
    }

    // buing rate will be at index 0 of BigDecimal array, selling rate will be at index 1
    public static BigDecimal[] getSingleBuyingAndSellingRate(String currency, String fileName){
        BigDecimal[] buyingAndSellingRate = new BigDecimal[2];
        try {
            String siteURL = String.format("http://www.nbp.pl/kursy/xml/%s.xml", fileName);
            URL url = new URL(siteURL);
            URLConnection connection = url.openConnection();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(connection.getInputStream());
            document.getDocumentElement().normalize();

            NodeList nList = document.getElementsByTagName("pozycja");
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if(element.getElementsByTagName("kod_waluty").item(0).getTextContent().equals(currency)){
                        buyingAndSellingRate[0] = new BigDecimal(element.getElementsByTagName("kurs_kupna").item(0).getTextContent().replace(",","."));
                        buyingAndSellingRate[1] = new BigDecimal(element.getElementsByTagName("kurs_sprzedazy").item(0).getTextContent().replace(",","."));
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buyingAndSellingRate;
    }



    public static void collectListsOfBuingAndSellingRate(String currency, List<String> listOfFiles){
        try{

            List<BigDecimal> listOfBuingRates = new ArrayList<>();
            List<BigDecimal> listOfSellingRates= new ArrayList<>();

            for(String str : listOfFiles){
                BigDecimal[] buyingAndSellingRate = getSingleBuyingAndSellingRate(currency, str);
                listOfBuingRates.add(buyingAndSellingRate[0]);
                listOfSellingRates.add(buyingAndSellingRate[1]);
            }

            BigDecimal avgBuying = MathematicalOperations.getAverageRate(listOfBuingRates).setScale(4, RoundingMode.HALF_UP);
            BigDecimal avgSelling = MathematicalOperations.getAverageRate(listOfSellingRates);
            BigDecimal standardDeviation = MathematicalOperations.getStandardDeviation(listOfSellingRates, avgSelling);

            System.out.println(avgBuying + " " + standardDeviation);

        }catch (Exception exc){
            exc.printStackTrace();
        }
    }




    public static void main(String[] args) {

        String currency  = args[0];
        String startDate = args[1];
        String endDate   = args[2];

//      Additional way to get user inputs
//      String[] userInput = collectUserInput();
//      String currency  = userInput[0];
//      String startDate = userInput[1];
//      String endDate   = userInput[2];
        List<String> listOfFileNames = readFileNamesFromSite(startDate, endDate);
        collectListsOfBuingAndSellingRate(currency, listOfFileNames);
    }



}
