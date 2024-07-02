package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CatalogDaoTest {

    @Mock
    private PaginatedQueryList<CatalogItemVersion> list;

    @Mock
    private DynamoDBMapper dynamoDbMapper;

    @InjectMocks
    private CatalogDao catalogDao;

    @BeforeEach
    public void setup(){
        initMocks(this);
    }

    @Test
    public void getBookFromCatalog_bookDoesNotExist_throwsException() {
        // GIVEN
        String invalidBookId = "notABookID";
        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(true);

        // WHEN && THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.getBookFromCatalog(invalidBookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void getBookFromCatalog_bookInactive_throwsException() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(true);
        item.setBookId(bookId);
        item.setVersion(1);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN && THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.getBookFromCatalog(bookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void getBookFromCatalog_oneVersion_returnVersion1() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(1);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN
        CatalogItemVersion book = catalogDao.getBookFromCatalog(bookId);

        // THEN
        assertEquals(bookId, book.getBookId());
        assertEquals(1, book.getVersion(), "Expected version 1 of book to be returned");
        assertFalse(book.isInactive(), "Expected book to be active.");

        verify(dynamoDbMapper).query(eq(CatalogItemVersion.class), requestCaptor.capture());
        CatalogItemVersion queriedItem = (CatalogItemVersion) requestCaptor.getValue().getHashKeyValues();
        assertEquals(bookId, queriedItem.getBookId(), "Expected query to look for provided bookId");
        assertEquals(1, requestCaptor.getValue().getLimit(), "Expected query to have a limit set");
    }

    @Test
    public void getBookFromCatalog_twoVersions_returnsVersion2() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(2);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN
        CatalogItemVersion book = catalogDao.getBookFromCatalog(bookId);

        // THEN
        assertEquals(bookId, book.getBookId());
        assertEquals(2, book.getVersion(), "Expected version 2 of book to be returned");
        assertFalse(book.isInactive(), "Expected book to be active.");

        verify(dynamoDbMapper).query(eq(CatalogItemVersion.class), requestCaptor.capture());
        CatalogItemVersion queriedItem = (CatalogItemVersion) requestCaptor.getValue().getHashKeyValues();
        assertEquals(bookId, queriedItem.getBookId(), "Expected query to look for provided bookId");
        assertEquals(1, requestCaptor.getValue().getLimit(), "Expected query to have a limit set");
    }

    @Test
    public void deleteBookFromCatalog_setBookToInactive() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(2);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN
        CatalogItemVersion book = catalogDao.deleteBookFromCatalog(bookId);

        // THEN
        assertTrue(book.isInactive(), "Expected book to be inactive.");
        verify(dynamoDbMapper).save(book);
    }

    @Test
    public void validateBookExists_bookDoesNotExist_throwsException() {
        // GIVEN
        String invalidBookId = "notABookID";
        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(true);

        // WHEN && THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.validateBookExists(invalidBookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void validateBookExists_inactiveBook_bookNotNull() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(true);
        item.setBookId(bookId);
        item.setVersion(1);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN & THEN
        assertDoesNotThrow(() -> catalogDao.validateBookExists(bookId),
                "Expected inactive book to still be validated.");
    }

    @Test
    public void validateBookExists_activeBook_bookNotNull() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(1);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN & THEN
        assertDoesNotThrow(() -> catalogDao.validateBookExists(bookId),
                "Expected active book to be validated.");
    }

    @Test
    public void createOrUpdateBook_newBook_returnsCatalogItemVersion() {
        // GIVEN
        KindleFormattedBook kindleFormattedBook = KindleFormattedBook.builder()
                .withText("text")
                .withTitle("Title")
                .withAuthor("author")
                .withGenre(BookGenre.ACTION)
                .build();

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(list);

        // WHEN
        CatalogItemVersion actual = catalogDao.createOrUpdateBook(kindleFormattedBook);

        // THEN
        assertNotNull(actual.getBookId());
        assertEquals(kindleFormattedBook.getAuthor(), actual.getAuthor());
        assertEquals(kindleFormattedBook.getText(), actual.getText());
        assertEquals(kindleFormattedBook.getTitle(), actual.getTitle());
        assertEquals(kindleFormattedBook.getGenre(), actual.getGenre());
        assertFalse(actual.isInactive());
        assertEquals(1, actual.getVersion());
        verify(dynamoDbMapper).save(actual);
    }

    @Test
    public void createOrUpdateBook_activeBook_returnsNewCatalogItemVersion() {
        // GIVEN
        String bookId = "book.123";
        KindleFormattedBook kindleFormattedBook = KindleFormattedBook.builder()
                .withBookId(bookId)
                .build();

        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(1);
        item.setText("text");
        item.setTitle("Title");
        item.setAuthor("author");
        item.setGenre(BookGenre.ACTION);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        when(catalogDao.getBookFromCatalog(bookId)).thenReturn(item);

        // WHEN
        CatalogItemVersion actual = catalogDao.createOrUpdateBook(kindleFormattedBook);

        // THEN
        //This captures the items saved by DynamoDBMapper
        ArgumentCaptor<CatalogItemVersion> captor = ArgumentCaptor.forClass(CatalogItemVersion.class);
        verify(dynamoDbMapper, times(2)).save(captor.capture());

        List<CatalogItemVersion> savedItems = captor.getAllValues();
        assertEquals(2, savedItems.size());

        //First save should save the older version (1) that is now marked inactive
        CatalogItemVersion firstSavedItem = savedItems.get(0);
        assertEquals(bookId, firstSavedItem.getBookId());
        assertTrue(firstSavedItem.isInactive());
        assertEquals(1, firstSavedItem.getVersion());

        //Second save should save the new version (2) that is marked active
        CatalogItemVersion secondSavedItem = savedItems.get(1);
        assertEquals(bookId, secondSavedItem.getBookId());
        assertFalse(secondSavedItem.isInactive());
        assertEquals(2, secondSavedItem.getVersion());
        assertEquals(item.getAuthor(), secondSavedItem.getAuthor());
        assertEquals(item.getText(), secondSavedItem.getText());
        assertEquals(item.getTitle(), secondSavedItem.getTitle());
        assertEquals(item.getGenre(), secondSavedItem.getGenre());
    }
}