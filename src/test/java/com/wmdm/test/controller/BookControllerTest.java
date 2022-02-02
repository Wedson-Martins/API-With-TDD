package com.wmdm.test.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wmdm.test.DTOs.BookDTO;
import com.wmdm.test.api.exceptions.BusinessException;
import com.wmdm.test.model.Book;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.net.URI;
import java.util.regex.Matcher;

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
        BookDTO dto = createNewBook();

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

    private BookDTO createNewBook() {
       return BookDTO.builder().author("João").title("O Menino da Vila").isbn("001").build();
    }
}
