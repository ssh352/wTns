package wizard.base;

import java.util.concurrent.ConcurrentHashMap;

import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import wizard.interfaces.Engine;
import wizard.interfaces.Strategy;

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
}
