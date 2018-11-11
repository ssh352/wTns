package wizard.interfaces;

import wizard.test.Order;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/29
 */
public interface TD extends Engine{
    void onOrder(Order order);
    void onCancelOrder(Order order);
}
