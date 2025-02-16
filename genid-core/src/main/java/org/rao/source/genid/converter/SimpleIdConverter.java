package org.rao.source.genid.converter;

/**
 * 简单的ID转换器
 * @author Rao
 * @Date 2025/2/8
 **/
public class SimpleIdConverter implements IdConverter {

    public static final IdConverter INSTANCE = new SimpleIdConverter();

    @Override
    public String asString(long id) {
        return String.valueOf(id);
    }

    @Override
    public long asLong(String id) {
        return Long.parseLong(id);
    }
}
