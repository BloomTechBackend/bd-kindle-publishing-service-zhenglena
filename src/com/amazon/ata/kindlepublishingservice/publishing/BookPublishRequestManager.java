package com.amazon.ata.kindlepublishingservice.publishing;

import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {
    private Queue<BookPublishRequest> requestQueue;

    @Inject
    public BookPublishRequestManager() {
        requestQueue = new LinkedList<>();
    }

    /**
     * adds the given BookPublishRequest to the collection
     */
    public void addBookPublishRequest(BookPublishRequest bookPublishRequest) {
        requestQueue.offer(bookPublishRequest);
    }

    /**
     * retrieves the next BookPublishRequest in line for publishing and returns it.
     * If there are no requests to publish this should return null.
     * @return BookPublishRequest or null
     */
    public BookPublishRequest getBookPublishRequestToProcess() {
        if (requestQueue.isEmpty()) {
            return null;
        }
        return requestQueue.poll();
    }

}
