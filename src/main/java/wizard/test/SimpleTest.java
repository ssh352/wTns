package wizard.test;

import wizard.base.Board;
import wizard.base.CTPMd;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/27
 */


public class SimpleTest {
    public static final String mdName = "fakeMd1";
    public static void main(String[] args) {


        String ctp1_MdAddress = "tcp://";
        String[] symbols = {"kk"};
        ctp1_MdAddress = ctp1_MdAddress + args[0];
        symbols[0] = args[1];
        Board board = new Board();

        CTPMd ctpMd = new CTPMd(board, ctp1_MdAddress, "9999", "125268", "140706",
                mdName, symbols);
        ctpMd.start();

    }
}
