package com.siemens.internship;

import com.siemens.internship.Class.Item;
import com.siemens.internship.Main.InternshipApplication;
import com.siemens.internship.Repository.ItemRepository;
import com.siemens.internship.Service.ItemService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@SpringBootTest(classes = InternshipApplication.class)
@EnableAsync
class InternshipApplicationTests {

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemService itemService;

	private Item item1;
	private Item item2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		item1 = new Item(1L, "Item One", "Description One", "NEW", "user1@siemens.com");
		item2 = new Item(2L, "Item Two", "Description Two", "NEW", "user2@siemens.com");

		when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
		when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
		when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);
	}

	@Test
	void testProcessItemsAsync_AllItemsProcessedWithStatus() throws Exception {
		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get();

		assertNotNull(result);
		assertEquals(2, result.size());
		for (Item item : result) {
			assertEquals("PROCESSED", item.getStatus());
		}

		verify(itemRepository, times(2)).save(any(Item.class));
	}

	@Test
	void testProcessItemsAsync_HandlesMissingItemGracefully() throws Exception {
		when(itemRepository.findById(2L)).thenReturn(Optional.empty());

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get();

		assertEquals(1, result.size());
		assertEquals("PROCESSED", result.get(0).getStatus());

		verify(itemRepository, times(1)).save(any(Item.class));
	}

	@Test
	void testProcessItemsAsync_ExceptionInThreadHandled() {
		when(itemRepository.findById(2L)).thenThrow(new RuntimeException("DB error"));

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();

		Exception exception = assertThrows(Exception.class, future::get);
		assertTrue(exception.getCause().getMessage().contains("DB error"));
	}


	@Test
	void contextLoads() {
		// Basic Spring context load test
	}
}
