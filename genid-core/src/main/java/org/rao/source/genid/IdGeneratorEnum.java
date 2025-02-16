package org.rao.source.genid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.rao.source.genid.generate.jvm.UuidGenerator;

/**
 * ID 生成枚举
 * @author Rao
 * @Date 2025/2/8
 **/
@Getter
@AllArgsConstructor
public enum IdGeneratorEnum {

    UUID(UuidGenerator.INSTANCE),

    ;

    /**
     * -- GETTER --
     *  获取ID 生成器
     *
     * @return
     */
    private final IdGenerator idGenerator;

}
