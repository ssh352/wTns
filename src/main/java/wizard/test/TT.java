package wizard.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/31
 */
public class TT {
    public static void main(String[] args) {
        Calendar cal= Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        Date d = cal.getTime();
        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String future = sp.format(d);
        System.out.println(future);
    }
}
