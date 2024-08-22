package com.example.dswan.services;

import com.example.dswan.dao.DisplayDao;
import com.example.dswan.domain.DisplayInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DisplayService {
    private final DisplayDao displayDao;

    @Autowired
    public DisplayService(final DisplayDao displayDao) {
        this.displayDao = displayDao;
    }

    /**
     * Method to retrieve display data
     **/
    public List<DisplayInfo> getDisplayInfo(String infoKey) {
        return displayDao.getDisplayInfo(infoKey);
    }
}
