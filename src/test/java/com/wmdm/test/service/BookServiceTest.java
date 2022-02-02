package com.wmdm.test.service;

import com.wmdm.test.api.exceptions.BusinessException;
import com.wmdm.test.model.Book;
import com.wmdm.test.model.repository.BookRepository;
import com.wmdm.test.service.imp.BookServiceImp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp(){
        this.bookService = new BookServiceImp(bookRepository);
        System.out.println("-------------------------------AAA: ");
    }

    @Test
    @DisplayName("To save Book")
    public void saveBookTest(){
        //Scenary
        Book book = createValidBook();
        Mockito.when(bookRepository.save(book)).thenReturn(
                Book.builder().id(17L).isbn("002").title("Drink").author("Jon").build()
        );

        //Execution
        Book bookSaved = this.bookService.save(book);
//        Book bookSaved = new Book();

        //Verification
        assertThat(bookSaved.getId()).isNotNull();
        assertThat(bookSaved.getAuthor()).isEqualTo("Jon");
        assertThat(bookSaved.getTitle()).isEqualTo("Drink");
        assertThat(bookSaved.getIsbn()).isEqualTo("002");
    }

    @Test
    @DisplayName("Create Book With Duplicate Isbn")
    public void createBookWithDuplicateIsbn(){
        //Scenary
        Book book = createValidBook();
        Mockito.when(bookRepository.existsByIsbn(book.getIsbn())).thenReturn(true);

        //Execution
        Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));

        //Verification
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn already registered");
        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }





    private Book createValidBook() {
        return Book.builder().author("Jon").title("Drink").isbn("001").build();
    }

}
