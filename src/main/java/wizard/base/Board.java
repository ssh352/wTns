package wizard.base;

import java.util.concurrent.ConcurrentHashMap;

import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import wizard.interfaces.Engine;
import wizard.interfaces.MD;
import wizard.interfaces.Strategy;
import wizard.interfaces.TD;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/11/1
 */
public class Board {
    public static final String sharedBasePath = "/share";
    public static final ConcurrentHashMap<String, Engine> engines = new ConcurrentHashMap<>();


    public static String getPathByName(String type, String name){
        return sharedBasePath + "/" + type + "/" + name;
    }

    public static synchronized void addEngine(String name, Engine engine){
        engines.put(name, engine);
    }

    public static synchronized ExcerptAppender getWriterByName(String type, String name){
        String path = getPathByName(type, name);
        SingleChronicleQueue queue = SingleChronicleQueueBuilder.binary(path).build();
        return queue.acquireAppender();
    }

    public static synchronized ExcerptTailer getReaderByName(String type, String name){
        String path = getPathByName(type, name);
        SingleChronicleQueue queue = SingleChronicleQueueBuilder.binary(path).build();
        return queue.createTailer();
    }

    public static synchronized Strategy getStrategyWriter(){
        return getWriterByName("stIn", "common").methodWriter(Strategy.class);
    }

    public static synchronized MethodReader getStrategyReader(Strategy strategy){
        return Board.getReaderByName("stIn", "common").methodReader(strategy);
    }
        public static synchronized MD getMdWriter(String mdName){
        return getWriterByName("mdIn", mdName).methodWriter(MD.class);
    }

    public static synchronized MethodReader getMdReader(CTPMd ctpMd){
        return Board.getReaderByName("mdIn", ctpMd.mdName).methodReader(ctpMd);
    }
     public static synchronized TD getTdWriter(String tdName){
        return getWriterByName("tdIn", tdName).methodWriter(TD.class);
    }

    public static synchronized MethodReader getTdReader(CTPTd ctpTd){
        return Board.getReaderByName("tdIn", ctpTd.tdName).methodReader(ctpTd);
    }




}
