package com.harry.clio.service;

public interface BookProcessService {
    void process(int bookId);

    void handleBookFailed(int bookId);
}
