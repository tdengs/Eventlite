package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;
	
	protected int numberOfVenues;

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@BeforeEach
	public void setup() {
		numberOfVenues = (int) venueService.count();
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	
	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._links.self.href")
				.value(endsWith("/api/venues")).jsonPath("$._embedded.venues.length()").value(equalTo(numberOfVenues));
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.error")
				.value(containsString("venue 99")).jsonPath("$.id").isEqualTo(99);
	}

	@Test
	public void getVenue() {
		client.get().uri("/venues/1").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.name")
				.isEqualTo("Killburn");;
	}




}
