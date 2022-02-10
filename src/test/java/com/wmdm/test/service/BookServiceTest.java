package com.wmdm.test.service;

import com.wmdm.test.api.exceptions.BusinessException;
import com.wmdm.test.model.entity.Book;
import com.wmdm.test.model.repository.BookRepository;
import com.wmdm.test.service.imp.BookServiceImp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("Don't Create Book With Duplicate Isbn")
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

    @Test
    @DisplayName("Get By Id")
    public void getById(){
        //Scenary
        Long id = 10L;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        //Execution
        Optional<Book> bookReturned = bookService.getById(id);
        //Verification
            assertThat(bookReturned).isPresent();
            assertThat(bookReturned).get().isInstanceOf(Book.class);
            assertThat(bookReturned.get().getId()).isEqualTo(id);
            assertThat(bookReturned.get().getTitle()).isEqualTo(createValidBook().getTitle());
            assertThat(bookReturned.get().getAuthor()).isEqualTo(createValidBook().getAuthor());
            assertThat(bookReturned.get().getIsbn()).isEqualTo(createValidBook().getIsbn());
    }


    @Test
    @DisplayName("Update")
    public void updateTest(){
        //Scenary
        Book book = createValidBook();
        Long id = 1L;
        book.setId(id);
        Mockito.when(bookRepository.save(book)).thenReturn(book);
        System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF: " + book);
        //Execution
        Book bookReturned = bookService.update(book);
        //Verification
        assertThat(bookReturned).isNotNull();
        assertThat(bookReturned.getId()).isEqualTo(id);
        assertThat(bookReturned.getAuthor()).isEqualTo(createValidBook().getAuthor());
        assertThat(bookReturned.getIsbn()).isEqualTo(createValidBook().getIsbn());
        assertThat(bookReturned.getTitle()).isEqualTo(createValidBook().getTitle());
    }

    @Test
    @DisplayName("Update Not Found Book")
    public void updateNotFoundBookTest(){
        //Scenary
        Book book = createValidBook();
        //Execution

        Throwable exception = Assertions.catchThrowable(() -> bookService.update(book));

        //Verification
        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id Can't Be null!");
        Mockito.verify(bookRepository,Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Delete")
    public void delete(){
        //Scenary
        Book book = createValidBook();
        book.setId(1L);

        //Execution
        bookService.delete(book);

        //Verification
        Mockito.verify(bookRepository, Mockito.times(1)).delete(book);

    }

    @Test
    @DisplayName("Delete Not Found Book")
    public void deleteNotFoundBookTest(){
        //Scenary
        Book book = createValidBook();

        //Execution
        //Verification
        Throwable exception = Assertions.catchThrowable(() -> bookService.delete(book));
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id Can't Be null!");
        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Find Book Test")
    public void findBookTest(){
        Book book = createValidBook();
        PageRequest pageRequest  = PageRequest.of(0,10);
        List<Book> lista = Arrays.asList(book);

        Page<Book> page = new PageImpl<Book>(Arrays.asList(book), pageRequest, 1);

        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = bookService.find(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);


    }

    private Book createValidBook() {
        return Book.builder().author("Jon").title("Drink").isbn("001").build();
    }
}
