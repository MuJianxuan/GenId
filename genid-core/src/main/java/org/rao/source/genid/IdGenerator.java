package org.rao.source.genid;

import org.rao.source.genid.converter.IdConverter;
import org.rao.source.genid.converter.SimpleIdConverter;

/**
 * ID生成器
 * @author Rao
 * @Date 2025/2/8
 **/
public interface IdGenerator extends StringIdGenerator {

    /**
     * ID转换器
     * @return
     */
    default IdConverter idConverter() {
        return SimpleIdConverter.INSTANCE;
    }
    /**
     * 生成ID
     * @return
     */
    long generateId();

    /**
     * 生成字符串ID
     * @return
     */
    @Override
    default String generateStringId() {
        return idConverter().asString(this.generateId());
    }
}
