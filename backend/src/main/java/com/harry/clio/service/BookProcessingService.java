package com.harry.clio.service;

public interface BookProcessingService {
    void process(int bookId);

    void handleBookFailed(int bookId);
}
