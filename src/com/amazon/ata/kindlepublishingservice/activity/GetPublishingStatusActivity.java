package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.PublishingStatusItemConverter;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GetPublishingStatusActivity {
    private final PublishingStatusDao publishingStatusDao;

    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    /**
     * Accepts a publishingRecordId and returns the publishing status history of the book submission from the
     * PublishingStatus table.
     * When a SUCCESSFUL PublishingStatus has been reached, the PublishingStatusRecord should contain a bookId,
     * if the book already exists, each PublishingStatusRecord will have a bookId.
     * Converts the PublishingStatusItem list to a PublishingStatusRecord list.
     *
     * @param publishingStatusRequest contains the ID to be queried
     * @return GetPublishingStatusResponse containing the PublishingStatusRecord list.
     */
    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        String publishingRecordId = publishingStatusRequest.getPublishingRecordId();
        List<PublishingStatusItem> statusItems = publishingStatusDao.getPublishingStatusItems(publishingRecordId);

        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(PublishingStatusItemConverter.toPublishingStatusRecord(statusItems))
                .build();
    }
}
