package wizard.test;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/27
 */


public class SimpleTest {
    final static String ctp1_MdAddress = "tcp://180.168.146.187:10010";
    public static void main(String[] args) {
        String[] symbols = {"rb1901"};
        CTPMd ctpMd = new CTPMd(ctp1_MdAddress, "9999", "", "",
                "PREfix", symbols);
        ctpMd.start();
    }
}
