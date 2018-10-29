package wizard.tools;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/29
 */
public class StringUtils {
    public static boolean isEmpty(String str){
        return str == null || str.length() < 1 || str.trim().length() < 1;
    }
}
