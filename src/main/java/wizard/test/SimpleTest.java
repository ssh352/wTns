package wizard.test;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/27
 */


public class SimpleTest {
    public static void main(String[] args) {
        String ctp1_MdAddress = "tcp://";
        String[] symbols = {"kk"};
        ctp1_MdAddress = ctp1_MdAddress + args[0];
        symbols[0] = args[1];

        CTPMd ctpMd = new CTPMd(ctp1_MdAddress, "9999", "125268", "140706",
                "PREfix ", symbols);
        ctpMd.start();
    }
}
