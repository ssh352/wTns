package wizard.test;

import net.openhft.chronicle.bytes.MethodReader;
import wizard.base.Board;
import wizard.interfaces.MD;
import wizard.interfaces.Strategy;
import wizard.interfaces.TD;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/11/1
 */
public class ST1 implements Strategy {
    public MD mdWriter;
    public TD tdWriter;
    public MethodReader eventReader;


    public ST1(String mdName, String tdName){
        this.mdWriter = Board.getMdWriter(mdName);
        this.tdWriter = Board.getTdWriter(tdName);
        this.eventReader = Board.getStrategyReader(this);
    }

    public void start(){
        while (true) {
            eventReader.readOne();
        }
    }


    public static void main(String[] args) {
        ST1 st1 = new ST1(args[0], args[1]);
        st1.start();
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
