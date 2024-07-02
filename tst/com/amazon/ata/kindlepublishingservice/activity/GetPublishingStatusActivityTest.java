package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class GetPublishingStatusActivityTest {
    @Mock
    PublishingStatusDao publishingStatusDao;

    @InjectMocks
    GetPublishingStatusActivity getPublishingStatusActivity;

    @BeforeEach
    public void setup() {
        initMocks(this);
    }

    @Test
    public void execute_successfulPublishingStatusWithExistentBook_returnsMultipleRecordsWithBookId() {
        //GIVEN
        String SUCCESSFUL = "publishingstatus.bdd319cb-05eb-494b-983f-6e1b983c4c46";
        GetPublishingStatusRequest request = GetPublishingStatusRequest.builder()
                .withPublishingRecordId(SUCCESSFUL)
                .build();

        PublishingStatusItem item1 = new PublishingStatusItem();
        item1.setPublishingRecordId(SUCCESSFUL);
        item1.setStatus(PublishingRecordStatus.QUEUED);
        item1.setStatusMessage("Queued for publishing at 2020-02-25 15:17:08.929");
        item1.setBookId("book.b3750190-2a30-4ca8-ae1b-73d0d202dc41");

        PublishingStatusItem item2 = new PublishingStatusItem();
        item2.setPublishingRecordId(SUCCESSFUL);
        item2.setStatus(PublishingRecordStatus.IN_PROGRESS);
        item2.setStatusMessage("Processing started at 2020-02-25 15:17:09.213");
        item2.setBookId("book.b3750190-2a30-4ca8-ae1b-73d0d202dc41");

        PublishingStatusItem item3 = new PublishingStatusItem();
        item3.setPublishingRecordId(SUCCESSFUL);
        item3.setStatus(PublishingRecordStatus.SUCCESSFUL);
        item3.setStatusMessage("Book published at 2020-02-25 15:17:09.551");
        item3.setBookId("book.b3750190-2a30-4ca8-ae1b-73d0d202dc41");

        List<PublishingStatusItem> statusItems = new ArrayList<>();
        statusItems.add(item1);
        statusItems.add(item2);
        statusItems.add(item3);

        when(publishingStatusDao.getPublishingStatusItems(request.getPublishingRecordId()))
                .thenReturn(statusItems);
        //WHEN
        GetPublishingStatusResponse response = getPublishingStatusActivity.execute(request);

        //THEN
        assertNotNull(response, "Expected request to return a non-null response.");
        assertNotNull(response.getPublishingStatusHistory(), "Expected a non-null list of PublishingStatusRecords");
        List<PublishingStatusRecord> recordList = response.getPublishingStatusHistory();
        assertEquals(statusItems.size(), recordList.size(),
                "Expected size of PublishingStatusRecord list to be equal to PublishingStatusItem list");
        assertNotNull(recordList.get(0).getBookId(), "Expected bookId to be present for all items in PublishingStatusItem list");
        assertNotNull(recordList.get(1).getBookId(), "Expected bookId to be present for all items in PublishingStatusItem list");
        assertNotNull(recordList.get(2).getBookId(), "Expected bookId to be present for all items in PublishingStatusItem list");
    }

    @Test
    public void execute_successfulPublishingStatusWithNonExistentBook_returnsMultipleRecords() {
        //GIVEN
        String SUCCESSFUL_NEW_VERSION = "publishingstatus.2bc206a1-5b41-4782-a260-976c0a291825";
        GetPublishingStatusRequest request = GetPublishingStatusRequest.builder()
                .withPublishingRecordId(SUCCESSFUL_NEW_VERSION)
                .build();

        PublishingStatusItem item1 = new PublishingStatusItem();
        item1.setPublishingRecordId(SUCCESSFUL_NEW_VERSION);
        item1.setStatus(PublishingRecordStatus.QUEUED);
        item1.setStatusMessage("Queued for publishing at 2020-02-25 15:17:08.929");

        PublishingStatusItem item2 = new PublishingStatusItem();
        item2.setPublishingRecordId(SUCCESSFUL_NEW_VERSION);
        item2.setStatus(PublishingRecordStatus.IN_PROGRESS);
        item2.setStatusMessage("Processing started at 2020-02-25 15:17:09.213");

        PublishingStatusItem item3 = new PublishingStatusItem();
        item3.setPublishingRecordId(SUCCESSFUL_NEW_VERSION);
        item3.setStatus(PublishingRecordStatus.SUCCESSFUL);
        item3.setStatusMessage("Book published at 2020-02-25 15:17:09.551");
        item3.setBookId("book.b3750190-2a30-4ca8-ae1b-73d0d202dc41");

        List<PublishingStatusItem> statusItems = new ArrayList<>();
        statusItems.add(item1);
        statusItems.add(item2);
        statusItems.add(item3);

        when(publishingStatusDao.getPublishingStatusItems(request.getPublishingRecordId()))
                .thenReturn(statusItems);
        //WHEN
        GetPublishingStatusResponse response = getPublishingStatusActivity.execute(request);

        //THEN
        assertNotNull(response, "Expected request to return a non-null response.");
        assertNotNull(response.getPublishingStatusHistory(), "Expected a non-null list of PublishingStatusRecords");

        List<PublishingStatusRecord> recordList = response.getPublishingStatusHistory();

        assertEquals(statusItems.size(), recordList.size(),
                "Expected size of PublishingStatusRecord list to be equal to PublishingStatusItem list");
        assertNull(recordList.get(0).getBookId(), "Expected bookId to be null");
        assertNull(recordList.get(1).getBookId(), "Expected bookId to be null");
        assertNotNull(recordList.get(2).getBookId(), "Expected bookId to be present once status is SUCCESSFUL.");
    }

}
