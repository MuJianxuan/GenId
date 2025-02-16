package org.rao.source.genid.converter;

/**
 * @author Rao
 * @Date 2025/2/8
 **/
public interface IdConverter {

    /**
     * 转换long为String
     * @param id
     * @return
     */
    String asString(long id);

    /**
     * 转换String为long
     * @param id
     * @return
     */
    long asLong(String id);

}
