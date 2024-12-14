package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.service.BanWordManager;
import org.springframework.stereotype.Component;

@Component
public class BanWordFactory extends AbstractWordFactory<BanWordManager> {
    public BanWordFactory(BanWordManager banWordManager) {
        super(banWordManager);
    }
}