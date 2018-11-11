package wizard.interfaces;

import wizard.test.Order;
import wizard.test.Tick;
import wizard.test.Trade;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/29
 */
public interface Strategy {
    void onTick(Tick tick);
    void onReturnOrder(Order order);
    void onReturnTrade(Trade trade);
}
