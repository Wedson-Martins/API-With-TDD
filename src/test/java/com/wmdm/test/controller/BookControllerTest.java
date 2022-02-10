package com.wmdm.test.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wmdm.test.DTOs.BookDTO;
import com.wmdm.test.api.exceptions.BusinessException;
import com.wmdm.test.model.entity.Book;
import com.wmdm.test.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest//para camada de Controller
@AutoConfigureMockMvc//para camada de Controller
public class BookControllerTest {


    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Create Book")
    public void createBookTest() throws Exception {
        //Criando DTO para verificar o retorno para o Front
        BookDTO dto = BookDTO.builder().author("João").title("O Menino da Vila").isbn("1213128").build();
        //Criando Book para ser o retorno do back no Mock
        Book savedBook = Book.builder().id(10L).author("João").title("O Menino da Vila").isbn("1213128").build();
        //setando o retorno do Back para o método save do service no Mock
        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);
        //Convertendo a DTO em um JSON para enviar no body do request
        String json = new ObjectMapper().writeValueAsString(dto);
        //Executando um request no endPoint
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //Verificando o retorno do Front
        mvc.perform(request)
           .andExpect(status().isCreated())
           .andExpect(jsonPath("id").value(10L))
           .andExpect(jsonPath("title").value(dto.getTitle()))
           .andExpect(jsonPath("author").value(dto.getAuthor()))
           .andExpect(jsonPath("isbn").value(dto.getIsbn()));

    }

    @Test
    @DisplayName("Create Invalid Book")
    public void createInvalidBookTest() throws Exception{
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Create Book With Duplicate Isbn")
    public void createBookWithDuplicateIsbn() throws Exception {
        BookDTO dto = createNewBookDTO();

        String errorMessege = "Isbn already registered";
        BDDMockito.given(bookService.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(errorMessege));

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(errorMessege));
    }

    @Test
    @DisplayName("Get Book Details Test")
    public void getBookDetailsTest() throws Exception {
        //Scenery
        Long id = 10L;
        Book book = Book.builder().id(id)
                        .title(createNewBookDTO().getTitle())
                        .author(createNewBookDTO().getAuthor())
                        .isbn(createNewBookDTO().getIsbn())
                        .build();
        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        //Execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(id))
            .andExpect(jsonPath("title").value(createNewBookDTO().getTitle()))
            .andExpect(jsonPath("author").value(createNewBookDTO().getAuthor()))
            .andExpect(jsonPath("isbn").value(createNewBookDTO().getIsbn()));
    }

    @Test
    @DisplayName("Book Not Found Test")
    public void bookNotFoundTest() throws Exception {
        //Scenery
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());
        //Execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Book Test")
    public void deleteBookTest() throws Exception {
        //Scenary
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        //Execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/1"));
        //Verification
        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Not Found Delete Book Test")
    public void DeleteBookNonExistentTest() throws Exception {
        //Scenary
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //Execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/1"));
        //Verification
        mvc
            .perform(request)
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("updateBookTest")
    public void updateBookTest() throws Exception {
        //Scenery
        Long id = 10L;
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(id).build()));
        Book book = createNewBook();
        book.setId(id);
        String json = new ObjectMapper().writeValueAsString(book);
        BDDMockito.given(bookService.update(book)).willReturn(book);
        //Execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        //Verification
        mvc
            .perform(request)
            .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBookDTO().getTitle()))
                .andExpect(jsonPath("author").value(createNewBookDTO().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBookDTO().getIsbn()));
    }

    @Test
    @DisplayName("Don't Update Book Nonexistent Test")
    public void updateBookNonExistentTest() throws Exception {
        //Scenery
        Long id = 10L;
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());
        Book book = createNewBook();
        book.setId(id);
        String json = new ObjectMapper().writeValueAsString(book);
        BDDMockito.given(bookService.update(book)).willReturn(book);
        //Execution
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        //Verification
        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Find Books Test")
    public void findBooksTest() throws Exception {

        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn())
                .author(createNewBook().getAuthor())
                .build();

        BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class) ))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0,100),1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        System.out.println(BOOK_API.concat(queryString));

mvc
        .perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("content", Matchers.hasSize(1)))
        .andExpect(jsonPath("totalElements").value(1))
        .andExpect(jsonPath("pageable.pageSize").value(100))
        .andExpect(jsonPath("pageable.pageNumber").value(0));

    }

    private BookDTO createNewBookDTO() {
       return BookDTO.builder().author("João").title("O Menino da Vila").isbn("001").build();
    }

    private Book createNewBook() {
        return Book.builder().author("João").title("O Menino da Vila").isbn("001").build();
    }
}
