package com.wmdm.test.controller;

import com.wmdm.test.DTOs.LoanDTO;
import com.wmdm.test.model.entity.Book;
import com.wmdm.test.model.entity.Loan;
import com.wmdm.test.service.BookService;
import com.wmdm.test.service.LoanService;
import com.wmdm.test.service.imp.LoanServiceImp;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/loan")
public class LoanController {
    @Autowired
    LoanService loanService = new LoanServiceImp();

    @Autowired
    private BookService bookService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping()
    public LoanDTO save(@RequestBody LoanDTO loanDTO){
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan loanToPersist = modelMapper.map(loanDTO, Loan.class);
        Loan entity = loanService.save(loanToPersist);
        return modelMapper.map(entity, LoanDTO.class);
    }


}
