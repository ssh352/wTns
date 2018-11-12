package wizard.test;

import java.util.concurrent.atomic.AtomicInteger;

import net.openhft.chronicle.bytes.MethodReader;
import wizard.base.Board;
import wizard.interfaces.MD;
import wizard.interfaces.RtConstant;
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
    private AtomicInteger orderRef = new AtomicInteger(0);


    public ST1(String mdName, String tdName){
        this.mdWriter = Board.getMdWriter(mdName);
        this.tdWriter = Board.getTdWriter(tdName);
        this.eventReader = Board.getStrategyReader(this, true);
    }

    public void start(){
        while (true) {
            eventReader.readOne();
        }
    }


    public String genOrderId(){
        return mdWriter.getName() + "_#_" + tdWriter.getName() + "_#_" + orderRef.get();
    }


    public void sendOrder(String symbol, int volume, double price, String direction, String offset){
        Order orderReq = new Order();
        orderReq.symbol = symbol;
        orderReq.volume = volume;
        orderReq.price = price;
        orderReq.priceType = RtConstant.PRICETYPE_LIMITPRICE; // only support this
        orderReq.direction = direction;
        orderReq.offset = offset;
        orderReq.orderId = genOrderId();
        tdWriter.onOrder(orderReq);
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
