package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.manager.BanWordManager;
import org.springframework.stereotype.Component;

@Component
public class BanWordFactory extends AbstractWordFactory<BanWordManager> {
    public BanWordFactory(BanWordManager banWordManager) {
        super(banWordManager);
    }
}