package eu.kuffel.vxapp.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Generates random strings, numbers and dates.
 * @author kuffel
 */
public class AppRandom {

    /**
     * Get a random integer value.
     * @param min Min value.
     * @param max Max value
     * @return Random integer value.
     */
    public static int getRandomInt( int min, int max ){
        Random random = new Random();
        if(min > max){
            return (random.nextInt(min-max)+max);
        }else{
            return (random.nextInt(max-min)+min);
        }
    }

    /**
     * Get a random float value.
     * @param min Min value.
     * @param max Max value
     * @return Random float value.
     */
    public static float getRandomFloat( float min, float max ){
        Random random = new Random();
        return random.nextFloat() * (max - min) + min;
    }
    /**
     * Get a random boolean value.
     * @return True or false
     */
    public static boolean getRandomBoolean(){
        Random random = new Random();
        return random.nextBoolean();
    }


    /**
     * Get a random uppercase string.
     * @param length Desired length.
     * @return Random string
     */
    public static String getRandomStringUppercase( int length ){
        return getRandomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray());
    }

    /**
     * Get a random lowercase string.
     * @param length Desired length.
     * @return Random string
     */
    public static String getRandomStringLowercase( int length ){
        return getRandomString(length, "abcdefghijklmnopqrstuvwxyz".toCharArray());
    }

    /**
     * Get a random string with uppercase/lowercase letters and digits.
     * @param length Desired length.
     * @return Random string
     */
    public static String getRandomStringAlphanumeric( int length ){
        return getRandomString(length, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray());
    }

    /**
     * Get a random string with uppercase and lowercase letters.
     * @param length Desired length.
     * @return Random string
     */
    public static String getRandomString( int length ){
        return getRandomString(length, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray());
    }

    /**
     * Get a random string only with the desired characters.
     * @param length Desired length.
     * @param allowedChars Array with allowed characters, e.g. "ABCDE".toCharArray()
     * @return Random string
     */
    public static String getRandomString( int length, char[] allowedChars ){
        StringBuilder sb = new StringBuilder();
        if(length < 0){
            throw new RuntimeException("invalid length, please provide a positive number.");
        }
        if(allowedChars != null && allowedChars.length > 0){
            for(int i = 0; i < length; i++){
                sb.append( allowedChars[getRandomInt(0, allowedChars.length)]);
            }
        }else{
            throw new RuntimeException("allowedChars does not contain any characters");
        }
        return sb.toString();
    }

    /**
     * Get a random element from the passed array.
     * @param elements Array with allowed values.
     * @return Random Array Element.
     */
    public static String getRandomElement( String[] elements ){
        if(elements != null && elements.length > 0){
            return elements[getRandomInt(0, elements.length)];
        }else{
            throw new RuntimeException("elements array is empty");
        }
    }

    /**
     * Get a random date from 1970 to 2016
     * @return Random date
     */
    public static Date getRandomDate(){
        return getRandomDate(1, 31, 1, 12, 1970, 2016, 0, 23, 0, 59, 0, 59);
    }
    /**
     * Get a random date in the desired range.
     * @param minYear Smallest possible year
     * @param maxYear Largest possible year
     * @return Random Date
     */
    public static Date getRandomDate( int minYear, int maxYear ){
        return getRandomDate(1, 31, 1, 12, minYear, maxYear, 0, 23, 0, 59, 0, 59);
    }
    /**
     * Get a random date in the desired range.
     * @param minDay Smallest possible day
     * @param maxDay Largest possible day
     * @param minMonth Smallest possible month
     * @param maxMonth Largest possible month
     * @param minYear Smallest possible year
     * @param maxYear Largest possible year
     * @return Random Date
     */
    public static Date getRandomDate( int minDay, int maxDay, int minMonth, int maxMonth, int minYear, int maxYear ){
        return getRandomDate(minDay, maxDay, minMonth, maxMonth, minYear, maxYear, 0, 23, 0, 59, 0, 59);
    }
    /**
     * Get a random date in the desired range.
     * @param minDay Smallest possible day
     * @param maxDay Largest possible day
     * @param minMonth Smallest possible month
     * @param maxMonth Largest possible month
     * @param minYear Smallest possible year
     * @param maxYear Largest possible year
     * @param minHour Smallest possible hour
     * @param maxHour Largest possible hour
     * @param minMinute Smallest possible minute
     * @param maxMinute Largest possible minute
     * @param minSecond Smallest possible second
     * @param maxSecond Largest possible second
     * @return Random Date
     */
    public static Date getRandomDate( int minDay, int maxDay, int minMonth, int maxMonth, int minYear, int maxYear, int minHour, int maxHour, int minMinute, int maxMinute, int minSecond, int maxSecond ){
        Calendar c = GregorianCalendar.getInstance();
        int year = getRandomInt(minYear, maxYear+1);
        if(minMonth <= 0 ){
            minMonth = 1;
        }
        if(maxMonth > 12){
            maxMonth = 12;
        }
        int month = getRandomInt(minMonth, maxMonth);
        int[] daysMonth = new int[12];
        daysMonth[0] = 31; // Jan
        if(year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)){
            daysMonth[1] = 29;
        }else{
            daysMonth[1] = 28;
        }
        daysMonth[2] = 31;
        daysMonth[3] = 30;
        daysMonth[4] = 31;
        daysMonth[5] = 30;
        daysMonth[6] = 31;
        daysMonth[7] = 31;
        daysMonth[8] = 30;
        daysMonth[9] = 31;
        daysMonth[10] = 30;
        daysMonth[11] = 31;
        if(minDay < 0 ){
            minDay = 0;
        }
        if(maxDay >= daysMonth[month-1]){
            maxDay = daysMonth[month-1];
        }
        int day = getRandomInt(minDay, maxDay);
        if(minHour < 0) {
            minHour = 0;
        }
        if(maxHour > 23){
            maxHour = 23;
        }
        int hour = getRandomInt(minHour, maxHour);
        if(minMinute < 0) {
            minMinute = 0;
        }
        if(maxMinute > 59){
            maxMinute = 59;
        }
        int minute = getRandomInt(minMinute, maxMinute);
        if(minSecond < 0) {
            minSecond = 0;
        }
        if(maxSecond > 59){
            maxSecond = 59;
        }
        int second = getRandomInt(minSecond, maxSecond);
        c.set(year, month, day, hour, minute, second);
        return c.getTime();
    }

}
