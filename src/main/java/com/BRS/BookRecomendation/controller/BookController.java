package com.BRS.BookRecomendation.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.BRS.BookRecomendation.DTO.BookSection;
import com.BRS.BookRecomendation.DTO.CartItemDTO;
import com.BRS.BookRecomendation.DTO.WishlistItemDTO;
import com.BRS.BookRecomendation.Entities.Genre;
import com.BRS.BookRecomendation.Entities.Order;
import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.Cart;
import com.BRS.BookRecomendation.Entities.Wishlist;
import com.BRS.BookRecomendation.repository.BookRepository;
import com.BRS.BookRecomendation.repository.GenreRepository;
import com.BRS.BookRecomendation.service.OrderService;
import com.BRS.BookRecomendation.service.UserBookService;


@RestController
@RequestMapping("/book")
public class BookController {

	private Logger logger = LoggerFactory.getLogger(BookController.class);

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private GenreRepository genreRepository;

	@Autowired
	private UserBookService userBookService;
	
	@Autowired
	private OrderService orderService;

	@GetMapping("/hello")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public String sayHello() {
		return "Hello, World!";
	}
	
	@GetMapping("/{bookId}")
	public ResponseEntity<Optional<Book>> getBook(@PathVariable Long bookId) {
		Optional<Book> book = bookRepository.findById(bookId);
		
		return ResponseEntity.ok(book);
	}
	

	@GetMapping("/allBooks")
	public List<BookSection> getAllBooks() {

		List<Genre> allGenreTags = genreRepository.findAll();

		ArrayList<BookSection> allBookSections = new ArrayList<>();
		BookSection tempBookSection;

		for (Genre genre : allGenreTags) {
			tempBookSection = new BookSection();
			tempBookSection.setGenre(genre.getGenreTag());
			tempBookSection.setBooks(bookRepository.findByGenreId(genre.getGenreId()));
			allBookSections.add(tempBookSection);
		}

		return allBookSections;
	}
	
	@GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        List<Book> books = userBookService.searchBooks(query);
        return ResponseEntity.ok(books);
    }
	
	

	@GetMapping("/user/{username}/cart")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public ResponseEntity<List<CartItemDTO>> getUserCart(@PathVariable String username) {
		List<Cart> cartItems = userBookService.getUserCart(username);

		List<CartItemDTO> cartItemDTOs = cartItems.stream()
				.map(cart -> CartItemDTO.builder()
						.bookId(cart.getBook().getId())
						.title(cart.getBook().getTitle())
						.author(cart.getBook().getAuthor())
						.imageUrl(cart.getBook().getImageUrl())
						.price(cart.getBook().getPrice())
						.quantity(cart.getQuantity())
						.addedDate(cart.getAddedDate())
						.build())
				.collect(Collectors.toList());

		return ResponseEntity.ok(cartItemDTOs);
	}

	@PostMapping("/user/{username}/cart/{bookId}")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public ResponseEntity<?> addToCart(
			@PathVariable String username,
			@PathVariable Long bookId,
			@RequestParam(defaultValue = "1") Integer quantity) {

		if (!userBookService.checkStockAvailability(bookId, quantity)) {
			return ResponseEntity.badRequest().body("Not enough stock available");
		}

		userBookService.addToCart(username, bookId, quantity);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/user/{username}/cart/{bookId}")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public ResponseEntity<?> removeFromCart(
			@PathVariable String username,
			@PathVariable Long bookId) {
		userBookService.removeFromCart(username, bookId);
		return ResponseEntity.ok().build();
	}
    
    @DeleteMapping("/remove-all/{username}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> removeAllBooksFromCart(@PathVariable String username) {
        userBookService.removeAllBooks(username);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All books removed from cart for user: " + username);
        
        return ResponseEntity.ok(response);  // Return JSON response
    }
	
	
	@PutMapping("/user/{username}/cart/{bookId}")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public ResponseEntity<?> updateCartQuantity(
			@PathVariable String username,
			@PathVariable Long bookId,
			@RequestParam Integer quantity) {

		if (!userBookService.checkStockAvailability(bookId, quantity)) {
			return ResponseEntity.badRequest().body("Not enough stock available");
		}

		userBookService.updateCartQuantity(username, bookId, quantity);
		return ResponseEntity.ok().build();
	}
	

	@GetMapping("/user/{username}/wishlist")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public ResponseEntity<List<WishlistItemDTO>> getUserWishlist(@PathVariable String username) {
		List<Wishlist> wishlistItems = userBookService.getUserWishlist(username);

		List<WishlistItemDTO> wishlistItemDTOs = wishlistItems.stream()
				.map(wishlist -> WishlistItemDTO.builder()
						.bookId(wishlist.getBook().getId())
						.title(wishlist.getBook().getTitle())
						.author(wishlist.getBook().getAuthor())
						.imageUrl(wishlist.getBook().getImageUrl())
						.price(wishlist.getBook().getPrice())
						.addedDate(wishlist.getAddedDate())
						.build())
				.collect(Collectors.toList());

		return ResponseEntity.ok(wishlistItemDTOs);
	}

	@PostMapping("/user/{username}/wishlist/{bookId}")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public ResponseEntity<?> addToWishlist(
			@PathVariable String username,
			@PathVariable Long bookId) {
		userBookService.addToWishlist(username, bookId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/user/{username}/wishlist/{bookId}")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public ResponseEntity<?> removeFromWishlist(
			@PathVariable String username,
			@PathVariable Long bookId) {
		userBookService.removeFromWishlist(username, bookId);
		return ResponseEntity.ok().build();
	}
	
    @PostMapping("user/{username}/order/place")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Order> placeOrder(@PathVariable String username) {
        return ResponseEntity.ok(orderService.placeOrder(username));
    }

    @GetMapping("user/{username}/order/get")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String username) {
        return ResponseEntity.ok(orderService.getOrdersByUsername(username));
    }
	
    //change mapping to admin
    @GetMapping("user/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    
}
