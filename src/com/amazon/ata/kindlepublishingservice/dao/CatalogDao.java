package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public CatalogItemVersion deleteBookFromCatalog(String bookId) {
        CatalogItemVersion book = getBookFromCatalog(bookId);
        book.setInactive(true);
        dynamoDbMapper.save(book);

        return book;
    }

    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }
    }

    /**
     * Adds the new book to the CatalogItemVersionTable.
     * If the request is updating an existing book, set previous active version of book to false
     * and then increment the version by 1.
     * <p></p>
     * If the request is a previously existing book but is set to inactive, then return BookNotFoundException.
     * <p></p>
     * If the request does not exist in the CatalogItemVersionTable, then add the book to the table with version set as 1.
     * @param kindleFormattedBook the book to be added to the CatalogItemVersion table
     * @return the book added
     */
    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook kindleFormattedBook) {
        String bookId = kindleFormattedBook.getBookId();
        CatalogItemVersion newCatalogItem = new CatalogItemVersion();

        if (bookId == null) {
            newCatalogItem.setBookId(KindlePublishingUtils.generateBookId());
            newCatalogItem.setTitle(kindleFormattedBook.getTitle());
            newCatalogItem.setText(kindleFormattedBook.getText());
            newCatalogItem.setAuthor(kindleFormattedBook.getAuthor());
            newCatalogItem.setGenre(kindleFormattedBook.getGenre());
            newCatalogItem.setInactive(false);
            newCatalogItem.setVersion(1);

            dynamoDbMapper.save(newCatalogItem);
            return newCatalogItem;
        }

        //if this throws a BookNotFoundException, it's because the book is inactive
        CatalogItemVersion catalogItemVersion = getBookFromCatalog(bookId);

        // if this request is updating an existing book, increment the version by 1
        // previously active version of the book will be marked inactive.
        catalogItemVersion.setInactive(true);

        newCatalogItem.setBookId(bookId);
        newCatalogItem.setVersion(catalogItemVersion.getVersion() + 1);
        newCatalogItem.setInactive(false);
        newCatalogItem.setText(catalogItemVersion.getText());
        newCatalogItem.setAuthor(catalogItemVersion.getAuthor());
        newCatalogItem.setGenre(catalogItemVersion.getGenre());
        newCatalogItem.setTitle(catalogItemVersion.getTitle());

        dynamoDbMapper.save(catalogItemVersion);
        dynamoDbMapper.save(newCatalogItem);

        return newCatalogItem;
    }
}
