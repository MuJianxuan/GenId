package org.rao.source.genid.generate.jvm;

import cn.hutool.core.util.IdUtil;
import org.rao.source.genid.IdGenerator;
import org.rao.source.genid.exception.UnsupportedGenerateIdException;

/**
 * @author Rao
 * @Date 2025/2/8
 **/
public class UuidGenerator implements IdGenerator {

    public static final IdGenerator INSTANCE = new UuidGenerator();

    @Override
    public long generateId() {
        throw new UnsupportedGenerateIdException("UUID not support generate long id");
    }

    @Override
    public String generateStringId() {
        return IdUtil.fastUUID();
    }
}
