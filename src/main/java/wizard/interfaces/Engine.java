package wizard.interfaces;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/29
 */
public interface Engine {
    boolean alive();
    boolean start();
    boolean stop();
    String getName();
}
