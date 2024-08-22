package com.example.dswan.controllers;

import com.example.dswan.domain.DisplayInfo;
import com.example.dswan.services.DisplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DisplayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayController.class);

    private final DisplayService displayService;

    public DisplayController(DisplayService displayService) {
        this.displayService = displayService;
    }

    /**
     * Endpoint for getting Display Data
     *
     * @return displayData
     */
    @GetMapping("/subscriptions/display")
    public ResponseEntity<List<DisplayInfo>> getDisplayInfo(@RequestParam(required = false) String infoKey) {
        LOGGER.info("getDisplayInfo() - Input parameters infoKey: {}", infoKey);
        List<DisplayInfo> displayData = displayService.getDisplayInfo(infoKey);
        return new ResponseEntity<>(displayData, HttpStatus.OK);
    }
}
