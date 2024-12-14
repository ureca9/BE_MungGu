package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.service.ExceptWordManager;
import org.springframework.stereotype.Component;

@Component
public class ExceptWordFactory extends AbstractWordFactory<ExceptWordManager> {
    public ExceptWordFactory(ExceptWordManager exceptWordManager) {
        super(exceptWordManager);
    }
}