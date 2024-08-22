package com.example.dswan.dao;

import com.example.dswan.domain.DisplayInfo;
import com.mongodb.MongoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DisplayDao {

    @Value("${mongo.displayInfo.collection.name}")
    private String displayInfoCollectionName;

    private final MongoTemplate mongoTemplate;

    public DisplayDao(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Cacheable(value = "displayInfo", unless = "#result == null")
    public List<DisplayInfo> getDisplayInfo(String infoKey) {
        log.info("getDisplayInfo() - cache miss infoKey={}", infoKey);
        List<DisplayInfo> displayDetails = new ArrayList<>();

        try {
            log.debug("getDisplayInfo() - Retrieving display details from Mongo collection: {}", displayInfoCollectionName);
            Query query = new Query();
            query.addCriteria(Criteria.where("infoKey").is(infoKey));
            displayDetails = mongoTemplate.find(query, DisplayInfo.class, displayInfoCollectionName);
        } catch (MongoException | DataAccessResourceFailureException ex) {
            String message = String.format("Failed to retrieve display details from Mongo | exception=%s", ex.getMessage());
            log.warn(message);
            throw ex;
        }
        return displayDetails;
    }
}
