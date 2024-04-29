package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;


	@LocalServerPort
	private int port;


	private int currentRows;

	private WebTestClient client;

	private String[] login() {
		String[] tokens = new String[2];

		// Although this doesn't POST the log in form it effectively logs us in.
		// If we provide the correct credentials here, we get a session ID back which
		// keeps us logged in.
		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}

	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		// matcher.matches() must be called; might as well assert something as well...
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}

	private ObjectMapper mapper = new ObjectMapper();

	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";
	private static String CSRF_HEADER = "X-CSRF-TOKEN";

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		currentRows = countRowsInTable("events");
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	//Tests for Show Events

	@Test
	public void testShowEvent() {
		//dont know why but 1 and 2 are not filled and therefore we are left with 3
		client.get().uri("/events/3").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	//tests going to the new event page without a user
	@Test
	public void getNewEventNoUser() {
		// Should redirect to the sign-in page.
		client.get().uri("/events/new").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));
	}
	//tests going to the new event page with a user

	@Test
	public void getNewEventWithUser() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/new")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("_csrf"));
				});
	}
	//tests Accessing update event without a login user

	//Tests for Update Events
	@Test
	public void getUpdateEventNoUser() {
		// Should redirect to the sign-in page.
		client.get().uri("/events/update/3").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));
	}

	//tests going to the update event page with a user
	@Test
	public void getUpdateEventWithUser() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/update/3")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("_csrf"));
				});
	}


	@Test
	@DirtiesContext
	public void UpdateEventWithUser_Invalid() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "3");
		form.add("name", "MassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveName");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "iiiiiii");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void UpdateEventWithUser_Valid() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "3");
		form.add("name", "PASS3");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "iiiiiii");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/updateEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	//tests going to the update event page with a user
	@Test
	public void getUpdateEventWithUser_Event_Does_Not_Exist() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/update/99")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}


	// test for Create New Event
	@Test
	public void postEventNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "2");
		form.add("description", "description fot test event");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));

		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "2");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

		// Check one row is added to the database.
		assertThat(currentRows + 1, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_NoName() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "iiiiiii");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_ToLongName() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "MassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveNameMassiveName");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "iiiiiii");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_ToLongDiscription() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "iiiiiii");
		form.add("description", "Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long ");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_wrongDate() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().minusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "2");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_NoDate() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", "");
		form.add("time", "12:33");
		form.add("venue.id", "2");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_NoVenue() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_InvalidVenue() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().plusDays(3)).toString());
		form.add("time", "12:33");
		form.add("venue.id", "iiiiiii");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_noTime() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().plusDays(1)).toString());
		form.add("time", "");
		form.add("venue.id", "2");
		form.add("description", "description fot test event");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

		// Check one row is added to the database.
		assertThat(currentRows + 1, equalTo(countRowsInTable("events")));
	}

	@Test
	@DirtiesContext
	public void postEventWithUser_noDescription() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEvent");
		form.add("date", (LocalDate.now().plusDays(1)).toString());
		form.add("time", "");
		form.add("venue.id", "2");
		form.add("description", "");

		// The session ID cookie holds our login credentials.
		client.post().uri("/events/newEvent").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

		// Check one row is added to the database.
		assertThat(currentRows + 1, equalTo(countRowsInTable("events")));
	}

	// Tests For delete Events

	// Delete event without a user
	@Test
	public void deleteEventNoUser() {
		int currentRows = countRowsInTable("events");

		// Should redirect to the sign-in page.
		client.delete().uri("/events/3").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));

		// Check that nothing is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	// delete event with user
	@Test
	@DirtiesContext
	public void deleteEventWithUser() {
		int currentRows = countRowsInTable("events");
		String[] tokens = login();

		// The session ID cookie holds our login credentials.
		// And for a DELETE we have no body, so we pass the CSRF token in the headers.
		client.delete().uri("/events/3").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/events"));

		// Check that one row is removed from the database.
		assertThat(currentRows - 1, equalTo(countRowsInTable("events")));
	}

	//delete a not found event
	@Test
	public void deleteEventNotFound() {
		int currentRows = countRowsInTable("events");
		String[] tokens = login();

		// The session ID cookie holds our login credentials.
		// And for a DELETE we have no body, so we pass the CSRF token in the headers.
		client.delete().uri("/events/99").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isNotFound();

		// Check nothing is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}

}
