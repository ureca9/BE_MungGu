package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.util.wordutil.ExceptWordUtil;
import org.springframework.stereotype.Component;

@Component
public class ExceptWordFactory extends AbstractWordFactory<ExceptWordUtil> {
    public ExceptWordFactory(ExceptWordUtil exceptWordUtil) {
        super(exceptWordUtil);
    }
}