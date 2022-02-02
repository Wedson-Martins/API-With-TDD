package com.wmdm.test.repository;

import com.wmdm.test.model.Book;
import com.wmdm.test.model.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

}
