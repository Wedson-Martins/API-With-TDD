package com.wmdm.test.repository;

import com.wmdm.test.model.entity.Book;
import com.wmdm.test.model.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("To Save")
    public void save(){
        //Scenary
        Book book = Book.builder().author("Wedson").title("My Life").isbn("123").build();

        //Execution
        Book bookSaved = bookRepository.save(book);

        //Verification
        assertThat(bookSaved)
                .isNotNull()
                .isInstanceOf(Book.class);
        assertThat(bookSaved.getId()).isEqualTo(book.getId());
        assertThat(bookSaved.getTitle()).isEqualTo("My Life");
        assertThat(bookSaved.getAuthor()).isEqualTo("Wedson");
        assertThat(bookSaved.getIsbn()).isEqualTo("123");
    }

    @Test
    @DisplayName("Return True When Isbn Exists")
    public void returnTrueWhenIsbnExists(){

        //Scenary
        String isbn = "123";
        Book book = Book.builder().author("Wedson").title("My Life").isbn(isbn).build();
        this.entityManager.persist(book);


        //Execution
        boolean exist = bookRepository.existsByIsbn(isbn);

        //Verification
        assertThat(exist).isTrue();
//        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Return False When Isbn doesn't Exists")
    public void returnFalseWhenIsbnDoesNotExists(){

        //Scenary
        String isbn = "123";
        Book book = Book.builder().author("Wedson").title("My Life").isbn(isbn).build();


        //Execution
        boolean exist = bookRepository.existsByIsbn(isbn);


        //Verification
        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Find By Id Test")
    public void findByIdTest(){
        //Scenary

        Book book = Book.builder().title("O Pequeno Principe").author("Wedson").isbn("123").build();
        entityManager.persist(book);

        //Execution
        Optional<Book> bookReturned = bookRepository.findById(book.getId());
        System.out.println("-------------------------------------: " + book);

        //Verification
        assertThat(bookReturned).isPresent();
    }


    @Test
    @DisplayName("To Delete")
    public void delete(){
        //Scenary
        Book book = Book.builder().author("Wedson").title("My Life").isbn("123").build();
        entityManager.persist(book);

        Book findBook = entityManager.find(Book.class, book.getId());
        //Execution
        bookRepository.delete(findBook);
        Book bookreturned = entityManager.find(Book.class, book.getId());

        //Verification
        assertThat(bookreturned)
                .isNull();
    }

}
