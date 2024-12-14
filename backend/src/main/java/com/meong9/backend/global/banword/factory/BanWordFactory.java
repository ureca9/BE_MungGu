package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.util.wordutil.BadWordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BanWordFactory extends AbstractWordFactory<BadWordUtil> {

    @Autowired
    public BanWordFactory(BadWordUtil badWordUtil) {
        super(badWordUtil);
    }

}