package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.MockitoAnnotations.initMocks;

public class BookPublishTaskTest {
    //submit a book publish request by calling SubmitBookForPublishing
    //which should return a publishingRecordId
    //use this to check the status by calling GetPublishingStatus
    //Should be able to see the different states that the request has gone through
    //in the publish status history
    //once the publish request has hit the SUCCESSFUL state, call GetBook with the
    //bookId to see the new or updated book

    @Mock
    private PublishingStatusDao publishingStatusDao;

    @Mock
    private CatalogDao catalogDao;

    @InjectMocks
    private BookPublishTask bookPublishTask;

    @BeforeAll
    private void setup() {
        initMocks(this);
    }

    @Test
    public void run_nullRequest_doesNothing() {

    }


}
