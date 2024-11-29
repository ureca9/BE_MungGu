package com.meong9.backend.domain.pension.controller;

import com.meong9.backend.domain.pension.service.PensionDetailService;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PensionController {

    private final PensionDetailService pensionDetailService;

    @GetMapping("/pensions/detail/{pensionId}")
    public ResponseEntity<?> getPensionDetail(@PathVariable(name = "pensionId") Long pensionId) {
        return CommonResponse.ok("success", pensionDetailService.getPensionDetail(pensionId));
    }
}
