package smartpostxml;

import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class SmartPostXML {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        /*Create arrays for weekdays to make reuse easier*/
        String [] finnishWeekdays = new String[] {
            "ma", "ti", "ke", "to", "pe" , "la", "su"
        };
        String [] eestiWeekdays = new String[] {
            "E", "T", "K", "N", "R" , "L", "P"
        };
        
        /*Select which weekdays are used*/
        String[] weekDays = eestiWeekdays;
        //String[] weekDays = finnishWeekdays;
        ArrayList<SmartPost> postList = new ArrayList<SmartPost>();    
        try {
         /*Store the URLs of both depending which is used*/
         String viroPost = "http://iseteenindus.smartpost.ee/api/?request=destinations&country=EE&type=APT";
         String suomiPost = "https://iseteenindus.smartpost.ee/api/?request=destinations&country=FI&type=APT";
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(viroPost);
         //Document doc = dBuilder.parse(suomiPost); 
         doc.getDocumentElement().normalize();
         NodeList nList = doc.getElementsByTagName("item");
         System.out.println("----------------------------");
         
         
         /* Basic XML parsing for eacg ITEM, in this case the SmartPosts */
         for (int temp = 0; temp < nList.getLength(); temp++) {
             
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;
            String maAv="", tiAv="", keAv="", toAv="", peAv="", laAv="", suAv="";
            String avail = eElement
                    .getElementsByTagName("availability")
                    .item(0).getTextContent();
            
            /*Because Estonian data has some inconsistencies
            * ; instead of ,
            * en dash or em dash instead of regular
            * word "kell" written before the time
            */
            avail = avail.replace(";", ",");
            avail = avail.replaceAll("\\p{Pd}", "-");
            avail = avail.replace("kell ", "");
            /**************/
            
            /*If there are more than one information for availability, 
            * it's separated with a comma 
            */
            String [] availArray = avail.split(",");
            
            
            /*Next if-clause: 
            * Another inconsistency in Estonian case
            * No commas used to separate values
            * -> just get rid of the tail to make life easier
            * or use substrings to make it "correct"
            */
            if (availArray[0].length() > 20) {
                String tempAvail = availArray[0].substring(0,17);
                String tempAvail2 = "";
                /*There are three cases in total, if-clause is because of one*/
                if ((availArray[0].charAt(15) == ' ')) {
                    tempAvail2 = availArray[0].substring(16);
                } else {
                     tempAvail2 = availArray[0].substring(15);
                }
                String[] temporar  = tempAvail.split(" ");
                String[] temporar2  = tempAvail2.split(" ");
                String forArray = "";
                for (int i = 0; i < temporar2.length; i++) {
                    if ((i % 2 == 0) && (i > 0))
                        forArray += ",";
                    forArray += temporar2[i] + " ";
                }
                /*Create new array for values based on the substrings*/
                String [] newArray = {
                    temporar[0]+ " " +temporar[1], forArray
                };
                availArray = newArray;
            }
            
            
            /*No we have a list of availabilities for one item
            * Then we just go through it
            */
            for (String av : availArray) {
                String availStart ="", availEnd ="";
                
                /*Day and time is separated by space */
                String [] dayTime = av.split(" ");
                /*E.g "ma-pe 10 - 18 
                * We get the following list where:
                * 0 = days: ma-pe, 1 = start time: 10, 
                * 2 = middle dash: -, 3 = end time: 18
                */
                
                /*Because of the comma split earlier, 
                * After first availability info (after fist comma)
                * they start with an empty space
                * e.g "_la_10-18" (used underscore to show spaces)
                * This means it's beneficial to "clear" the first space
                * And that is what the following for-clause does
                */
                String[] days = null;
                if (dayTime[0].equals("")) {
                    for (int i = 0; i+1 < dayTime.length; i++) {
                        dayTime[i] = dayTime[i+1];
                    }
                    dayTime[dayTime.length-1] = "";
                } 
                
                /************/
                
                /* Now we split the days with dash
                * e.g. ma-pe
                * and store the "starting" day
                */
                days = dayTime[0].split("-");
                availStart = days[0];
                
                
                /********** START OF EXCEPTION CHECKS *************/
                /*
                * From here on, starts the handling of exceptions in data
                * E.g. Test if there is only one day
                * 24h instead of time 
                * Some different formatting that causes problems
                */
                try {
                    /*Test if we have ma-pe or just ma*/
                    if (days[1] !=null) {  
                        /*Store the end day if there is*/
                        availEnd = days[1]; 
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    /*Throws this exception if there is not, 
                    * no need to do anything
                    */
                }
                try {
                    /*If there is only the value 24h,
                    * Set all availability times to 0-23:59
                    */
                    if (dayTime[0].equals("24h")) {
                        maAv = "0.00-23:59";
                        tiAv = "0.00-23:59";
                        keAv = "0.00-23:59";
                        toAv = "0.00-23:59";
                        peAv = "0.00-23:59";
                        laAv = "0.00-23:59";
                        suAv = "0.00-23:59";
                        continue;
                    }
                    try {
                        String test = dayTime[1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        String[] empty = new String [] {
                            "","", "-",""
                        };
                        dayTime = empty;
                    }
                    /*
                    * If there is 24 given to specific days
                    */
                    if (dayTime[1].equals("24h")) {
                        String[] time24 = new String [] {
                            "","0:00", "-","23:59"
                        };
                        dayTime = time24;
                    }
                    try {
                        /* This test is because in Estonian data, time is
                        * not separated with a space from the dash,
                        * unlike in the Finnish data
                        */
                        String test = dayTime[3];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        /*Then just change it to match the Finnish data, 
                        * could be done either way 
                        * (probably easier the other way actually)
                        */
                        String [] array = dayTime[1].split("-");
                        String[] newArray = new String [] {
                           "",array[0],"-", array[1]
                        };
                        dayTime = newArray;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    /* This case is because of one inconsistency in Estonian data
                    * Where time is separated like this: "10 -21"
                    */
                    if (!availEnd.equals("")) {
                        try {
                            String [] array = dayTime[2].split("-");
                            String[] newArray = new String [] {
                               "",dayTime[1],"-", array[1]
                            };
                            dayTime = newArray;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            /* This case is because time may not be separated
                            * with a dash
                            */
                            try {
                                String[] newArray = new String [] {
                                    "",dayTime[1],"-", dayTime[2]
                                };
                                dayTime = newArray;
                            } catch (ArrayIndexOutOfBoundsException ez) {
                                /*Every other inconsistency is then made empty
                                * There should be none left
                                */
                                String[] empty = new String [] {
                                    "","", "-",""
                                };
                                dayTime = empty;
                            }
                            
                            
                        }
                    }
                }
                
                /********** END OF EXCEPTION CHECKS *************/
                
                
                
                /********** START OF PARSING *************/
                /*
                * Now starts the actual parsing of information
                * Basically compare if given start or end day matches a weekday
                * If it does, set the availability time for it
                * Then check if the given end date is given (not equal to "")
                * And does it match the same day as start day
                * If the given end day is not the same as the start day 
                * e.g ma-ke
                * move the start day to the next weekday (ti) and this goes on
                * until end day is reached 
                */
                if (availStart.equals(weekDays[0])) {
                    maAv = dayTime[1] + dayTime[2] + dayTime[3];
                    if (!availEnd.equals(weekDays[0]) && !availEnd.equals("")) {
			availStart = weekDays[1];
                    }
                }
                if ((availStart.equals(weekDays[1])) || (availEnd.equals(weekDays[1]))) {
                    tiAv = dayTime[1] + dayTime[2] + dayTime[3];
                    if (!availEnd.equals(weekDays[1]) && !availEnd.equals("")) {
                            availStart = weekDays[2];
                    }
                } 
                if ((availStart.equals(weekDays[2])) || (availEnd.equals(weekDays[2]))) {
                    keAv = dayTime[1] + dayTime[2] + dayTime[3];
                    if (!availEnd.equals(weekDays[2]) && !availEnd.equals("")) {
                            availStart = weekDays[3];
                    }
                } 
                if ((availStart.equals(weekDays[3])) || (availEnd.equals(weekDays[3]))) {
                    toAv = dayTime[1] + dayTime[2] + dayTime[3];
                    if (!availEnd.equals(weekDays[3]) && !availEnd.equals("")) {
                            availStart = weekDays[4];
                    }
                } 
                if ((availStart.equals(weekDays[4])) || (availEnd.equals(weekDays[4]))) {
                    peAv = dayTime[1] + dayTime[2] + dayTime[3];
                    if (!availEnd.equals(weekDays[4]) && !availEnd.equals("")) {
                            availStart = weekDays[5];
                    }
                } 
                if ((availStart.equals(weekDays[5])) || (availEnd.equals(weekDays[5]))) {
                    laAv = dayTime[1] + dayTime[2] + dayTime[3];
                    if (!availEnd.equals(weekDays[5]) && !availEnd.equals("")) {
                            availStart = weekDays[6];
                    }
                } 
                if ((availStart.equals(weekDays[6])) || (availEnd.equals(weekDays[6]))) {
                        suAv = dayTime[1] + dayTime[2] + dayTime[3];
                }   
            }
            /********** END OF PARSING *************/
            
            /*
            * Now create the smartpost object
            * give it the availability info
            * and store it in the arraylist
            */
            String city = eElement
                    .getElementsByTagName("city")
                    .item(0).getTextContent();; 
            String name = eElement
                    .getElementsByTagName("name")
                    .item(0).getTextContent();
            String placeId = eElement
                    .getElementsByTagName("place_id")
                    .item(0).getTextContent();
            String country = eElement
                    .getElementsByTagName("country")
                    .item(0).getTextContent();
            String address = eElement
                    .getElementsByTagName("address")
                    .item(0).getTextContent();
            String postalCode = eElement
                    .getElementsByTagName("postalcode")
                    .item(0).getTextContent();
            String desc = eElement
                    .getElementsByTagName("description")
                    .item(0).getTextContent();
            SmartPost sp = new SmartPost(city, name, placeId, country, address, 
                    postalCode, desc);
            sp.setAvailability(maAv, tiAv, keAv, toAv, peAv, laAv, suAv);
            postList.add(sp);           
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      System.out.println("Now print all smartposts!");
      for (SmartPost s : postList) {
          s.printAvailability();
      }
    }
}

class SmartPost {
    String maAv, tiAv, keAv, toAv, peAv, laAv, suAv;
    String city;
    String name;
    String placeId;
    String country;
    String address;
    String postalCode;
    String desc;
    
    public SmartPost(String city, String name, String placeId, String country, String address, String postalCode, String desc){
        this.city = city;
        this.name = name;
        this.placeId = placeId;
        this.country = country;
        this.address = address;
        this.postalCode = postalCode;
        this.desc = desc;
    }
    
    public void setAvailability(String maAv, String tiAv, String keAv, String toAv, String peAv, String laAv, String suAv){
        this.maAv = maAv;
        this.tiAv = tiAv; 
        this.keAv = keAv;
        this.toAv = toAv;
        this.peAv = peAv;
        this.laAv = laAv;
        this.suAv = suAv;
    }
    
    public void printAvailability() {
        System.out.println("Smartpost: " + name 
                + "\n Availability: Ma: " + maAv 
                + "\t Ti: " + tiAv
                + "\t Ke: " + keAv
                + "\t To: " + toAv
                + "\t Pe: " + peAv
                + "\t La: " + laAv
                + "\t Su: " + suAv);
    }
}
