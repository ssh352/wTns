package wizard.interfaces;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/29
 */
public interface MD extends Engine {
    void onSub(String symbol);
    void onUnsub(String symbol);
}
