package org.rao.source.genid.generate.jvm;

import org.rao.source.genid.IdGenerator;
import org.rao.source.genid.exception.UnsupportedGenerateIdException;
import org.rao.source.genid.util.UuidV7;

/**
 * uuid v7 生成器
 * @author Rao
 * @Date 2025/2/8
 **/
public class UuidV7Generator implements IdGenerator {

    public static final IdGenerator INSTANCE = new UuidV7Generator();

    @Override
    public long generateId() {
        throw new UnsupportedGenerateIdException("UUID not support generate long id");
    }

    @Override
    public String generateStringId() {
        return UuidV7.randomUUID().toString();
    }
}
