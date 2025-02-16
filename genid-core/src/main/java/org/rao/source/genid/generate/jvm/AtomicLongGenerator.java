package org.rao.source.genid.generate.jvm;

import org.rao.source.genid.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Rao
 * @Date 2025/2/8
 **/
public class AtomicLongGenerator implements IdGenerator {

    public static final AtomicLongGenerator INSTANCE = new AtomicLongGenerator();

    private final AtomicLong idGen = new AtomicLong();

    @Override
    public long generateId() {
        return idGen.incrementAndGet();
    }
}
