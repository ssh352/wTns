package wizard.test;

import net.openhft.chronicle.bytes.MethodReader;
import wizard.base.Board;
import wizard.interfaces.Strategy;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/11/1
 */
public class ST1 implements Strategy {
    public static void main(String[] args) {
        String f1 = "fakeMd1";
        MethodReader reader = Board.getReaderByName("md", f1).methodReader(new ST1());
        while (true) {
            reader.readOne();
        }
    }

    @Override
    public void onTick(Tick tick) {
        System.err.println(tick);
    }

    @Override
    public void onReturnOrder(Order order) {

    }

    @Override
    public void onReturnTrade(Trade trade) {

    }
}
