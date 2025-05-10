package com.siemens.internship.Service;

import com.siemens.internship.Class.Item;
import com.siemens.internship.Repository.ItemRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;

@Service
public class ItemService {
    
    
    @Autowired
    private Validator validator;



    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // Use a thread-safe list to collect processed items
        List<CompletableFuture<Item>> futures = new ArrayList<>();
        List<Long> itemIds = itemRepository.findAllIds();

        for (Long id : itemIds) {
            // Compose a CompletableFuture for each item and collect the future
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100); // Simulate delay
                    return itemRepository.findById(id).orElse(null);
                } catch (InterruptedException e) {
                    // Propagate as a runtime exception
                    throw new RuntimeException("Thread interrupted while processing item " + id, e);
                }
            }, executor).thenApply(item -> {
                if (item != null) {
                    // Process the item (e.g., update its status)
                    item.setStatus("PROCESSED");
                    // Validate the item

                    itemRepository.save(item); // Persist changes
                }
                return item;    // Return the processed item
            });

            futures.add(future);    // Collect the future for this item
        }

        // Combine all futures into a single CompletableFuture that completes when all are done
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join) // Safe because all futures are complete
                        .filter(Objects::nonNull)    // Filter out any null items
                        .collect(Collectors.toList()) // Collect results into a list
                );
    }
}


