package com.olexijko.paymentgw.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olexijko.paymentgw.dto.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditSender {
    private static final int MIN_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 4;
    private static final int THREAD_ALIVE_TIME_SECONDS = 60;

    private final ExecutorService executorService =
            new ThreadPoolExecutor(MIN_POOL_SIZE, MAX_POOL_SIZE, THREAD_ALIVE_TIME_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private final ObjectMapper objectMapper;

    @Value("${audit.sender.output-file}")
    private String outputFileAbsolutePath;


    public AuditSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void sendPayment(PaymentDto paymentDto) {
        try {
            executorService.execute(() -> writeToFile(paymentDto));
        } catch (Exception e) {
            LOGGER.error("Error during writing Payment info to audit file");
        }
    }

    private void writeToFile(PaymentDto paymentDto) {
        try {
            final Path outputFilePath = Path.of(outputFileAbsolutePath);
            Files.createDirectories(outputFilePath.getParent());
            final String paymentJson = objectMapper.writeValueAsString(paymentDto);
            synchronized (this) {
                Files.write(outputFilePath, List.of(paymentJson), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
