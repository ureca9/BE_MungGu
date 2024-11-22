package com.meong9.backend.global.mediafile.controller;

import com.meong9.backend.global.mediafile.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class MediaFileController {

    private final MediaFileService imageService;

    @PostMapping("/upload")
    public String upload(MultipartFile image) throws IOException {
        return imageService.upload(image);
    }
}
