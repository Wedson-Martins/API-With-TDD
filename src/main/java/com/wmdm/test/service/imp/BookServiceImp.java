package com.wmdm.test.service.imp;

import com.wmdm.test.api.exceptions.BusinessException;
import com.wmdm.test.model.Book;
import com.wmdm.test.model.repository.BookRepository;
import com.wmdm.test.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImp implements BookService {


    BookRepository repository;

    public BookServiceImp(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        boolean test = repository.existsByIsbn(book.getIsbn());
        if(test){
            throw new BusinessException("Isbn already registered");
        }
        Book bookSaved = repository.save(book);
        return bookSaved;
    }
}
