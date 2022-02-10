package com.wmdm.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wmdm.test.DTOs.LoanDTO;
import com.wmdm.test.api.exceptions.BusinessException;
import com.wmdm.test.model.entity.Book;
import com.wmdm.test.model.entity.Loan;
import com.wmdm.test.service.BookService;
import com.wmdm.test.service.LoanService;
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

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class LoanControllerTest {


    private String LOAN_API = "/api/loan";

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Register new Loan")
    public void save() throws Exception {

        LoanDTO dto = createLoanDTO();
        String json = new ObjectMapper().writeValueAsString(dto);
        Book book = createBook();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
        Loan loan = createLoan();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Do Not Save Without Isbn Correct")
    public void doNotSaveWithoutIsbnCorrect() throws Exception {
        LoanDTO dto = createLoanDTO();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = createBook();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));

    }


    @Test
    @DisplayName("Book already loaned")
    public void lonedeBookErrorOnCreateLoanTest() throws Exception {

        LoanDTO dto = createLoanDTO();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = createBook();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willThrow(new BusinessException("Book already loaned"));

//        Loan loan = createLoan();
//        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }




    private Loan createLoan() {
        return Loan.builder().id(1L).isbn("123").custumer("Wedson").build();
    }


    private Book createBook(){
        return Book.builder().id(1L).author("João").title("123").title("New History").isbn("123").build();
    }

    private LoanDTO createLoanDTO(){
        return LoanDTO.builder().id(1L).isbn("123").custumer("Wedson").build();
    }
}
