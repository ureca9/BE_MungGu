package com.meong9.backend.global.utils;

import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.entity.PensionFile;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.entity.PlaceFile;

import java.util.ArrayList;
import java.util.List;

public class ImageMapper {

    public static List<String> getPensionImageUrl(Pension pension) {
        List<String> imageUrls = new ArrayList<>();
        if (!pension.getPensionFiles().isEmpty()) {
            for (PensionFile pensionFile : pension.getPensionFiles()) {
                if (imageUrls.size() < 3) {
                    imageUrls.add(pensionFile.getMediaFile().getFileUrl());
                } else {
                    break;
                }
            }
        }
        return imageUrls;
    }

    // PlaceLike 이미지 URL 추출
    public static List<String> getPlaceImageUrl(Place place) {
        List<String> imageUrls = new ArrayList<>();
        if (!place.getPlaceFiles().isEmpty()) {
            for (PlaceFile placeFile : place.getPlaceFiles()) {
                if (imageUrls.size() < 3) {
                    imageUrls.add(placeFile.getMediaFile().getFileUrl());
                } else {
                    break;
                }
            }
        }
        return imageUrls;
    }

    public static List<String> getPensionImageUrlAll(Pension pension) {
        List<String> imageUrls = new ArrayList<>();
        if (!pension.getPensionFiles().isEmpty()) {
            for (PensionFile pensionFile : pension.getPensionFiles()) {
                imageUrls.add(pensionFile.getMediaFile().getFileUrl());
            }
        }
        return imageUrls;
    }

    // PlaceLike 이미지 URL 추출
    public static List<String> getPlaceImageUrlAll(Place place) {
        List<String> imageUrls = new ArrayList<>();
        if (!place.getPlaceFiles().isEmpty()) {
            for (PlaceFile placeFile : place.getPlaceFiles()) {
                imageUrls.add(placeFile.getMediaFile().getFileUrl());
            }
        }
        return imageUrls;
    }
}
