package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.models.requests.RemoveBookFromCatalogRequest;
import com.amazon.ata.kindlepublishingservice.models.response.RemoveBookFromCatalogResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;

/**
 * Implementation of the RemoveBookFromCatalogActivity for the ATACurriculumKindlePublishingService's
 * RemoveBookFromCatalog API.
 *
 * This API allows the client to remove a book in the catalog by marking it as inactive.
 */
public class RemoveBookFromCatalogActivity {
    private CatalogDao catalogDao;

    /**
     * Instantiates a new RemoveBookFromCatalogActivity object.
     *
     * @param catalogDao CatalogDao to access the catalog table.
     */
    @Inject
    public RemoveBookFromCatalogActivity(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    /**
     * Submits the book in the request for deletion.
     *
     * @param removeBookFromCatalogRequest Request object containing the book id to be deleted from the table.
     * @return RemoveBookFromCatalogResponse object containing the book id and the active status.
     */
    public RemoveBookFromCatalogResponse execute(RemoveBookFromCatalogRequest removeBookFromCatalogRequest) {
        CatalogItemVersion book = catalogDao.deleteBookFromCatalog(removeBookFromCatalogRequest.getBookId());

        return RemoveBookFromCatalogResponse.builder()
                .withId(book.getBookId())
                .isInactive(book.isInactive())
                .build();
    }
}
