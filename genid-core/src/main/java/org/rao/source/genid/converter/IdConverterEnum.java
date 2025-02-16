package org.rao.source.genid.converter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rao
 * @Date 2025/2/8
 **/
@AllArgsConstructor
@Getter
public enum IdConverterEnum {

    SIMPLE(SimpleIdConverter.INSTANCE),

    ;

    private final IdConverter idConverter;

}
