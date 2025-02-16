package org.rao.source.genid.generate.snowflake;

import org.rao.source.genid.IdGenerator;

/**
 * 雪花ID算法
 * @author Rao
 * @Date 2025/2/8
 **/
public class SnowflakeGenerator implements IdGenerator {

    public static final SnowflakeGenerator INSTANCE = new SnowflakeGenerator();

    @Override
    public long generateId() {
        return 0;
    }
}
