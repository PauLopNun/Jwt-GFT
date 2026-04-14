package com.exampleinyection.jwtgft.book;

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBook(Long id) {
        return bookRepository.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Book> updateBook(Long id, Book book) {
        return bookRepository.findById(id).map(existing -> {
            existing.setTitle(book.getTitle());
            existing.setAuthor(book.getAuthor());
            existing.setIsbn(book.getIsbn());
            existing.setPrice(book.getPrice());
            existing.setStock(book.getStock());
            return bookRepository.save(existing);
        });
    }

    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }
}

