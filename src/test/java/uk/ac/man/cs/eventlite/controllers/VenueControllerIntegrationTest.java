package uk.ac.man.cs.eventlite.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
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

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenueControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

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
		currentRows = countRowsInTable("venues");
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void testShowEvent() {
		//dont know why but 1 and 2 are not filled and therefore we are left with 3
		client.get().uri("/venues/2").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}

	//tests going to the new Venue page without a user
	@Test
	public void getNewVenueNoUser() {
		// Should redirect to the sign-in page.
		client.get().uri("/venues/new").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));
	}
	//tests going to the new Venue page with a user
	@Test
	public void getNewVenueWithUser() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/venues/new")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("_csrf"));
				});
	}

	//Tests for Update Venue
	@Test
	public void getUpdateVenueNoUser() {
		// Should redirect to the sign-in page.
		client.get().uri("/venues/update/3").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));
	}

	//tests going to the update event page with a user
	@Test
	public void getUpdateVenueWithUser() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/venues/update/2")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("_csrf"));
				});
	}

	//tests going to the update event page with a user
	@Test
	public void getUpdateVenueWithUser_Venue_Does_Not_Exist() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/venues/update/99")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}

	@Test
	@DirtiesContext
	public void UpdateVenueWithUser_Valid() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "burn");
		form.add("id", "2");
		form.add("address","Oxford Rd, Manchester M13 9PL");
		form.add("capacity", "200");

		// The session ID cookie holds our login credentials.
		client.post().uri("/venues/updateVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound();

		// Check one row is added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	@DirtiesContext
	public void postVenueNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "testVenue");
		form.add("address","Oxford Rd, Manchester M13 9PL");
		form.add("capacity", "200");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/newVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));
		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	@DirtiesContext
	public void postVenueWithUser() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "testVenue");
		form.add("address","Oxford Rd, Manchester M13 9PL");
		form.add("capacity", "200");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/newVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));
		// Check nothing added to the database.
		assertThat(currentRows +1 , equalTo(countRowsInTable("venues")));
	}

	@Test
	@DirtiesContext
	public void postVenueWithUser_No_Name() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "");
		form.add("address","Oxford Rd, Manchester M13 9PL");
		form.add("capacity", "200");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/newVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		// Check nothing added to the database.
		assertThat(currentRows , equalTo(countRowsInTable("venues")));
	}
	@Test
	@DirtiesContext
	public void postVenueWithUser_To_Long_Name() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long ");
		form.add("address","Oxford Rd, Manchester M13 9PL");
		form.add("capacity", "200");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/newVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	@DirtiesContext
	public void postVenueWithUser_Invalid_Address() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long ");
		form.add("address","hhhsh");
		form.add("capacity", "200");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/newVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	@DirtiesContext
	public void postVenueWithUser_No_Address() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long ");
		form.add("address","");
		form.add("capacity", "200");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/newVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}


	@Test
	@DirtiesContext
	public void postVenueWithUser_No_Capacity() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long Description That is too Long ");
		form.add("address","Oxford Rd, Manchester M13 9PL");
		form.add("capacity", "");

		// We don't set the session ID, so have no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venues/newVenue").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isOk();
		// Check nothing added to the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}


	// Tests For delete Venue

	// Delete Venue without a user
	@Test
	public void deleteVenueNoUser() {
		int currentRows = countRowsInTable("venues");

		// Should redirect to the sign-in page.
		client.delete().uri("/venues/2").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));

		// Check that nothing is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
	// delete event with user
	@Test
	@DirtiesContext
	public void deleteVenueWithUser_with_Events() {
		int currentRows = countRowsInTable("venues");
		String[] tokens = login();

		// The session ID cookie holds our login credentials.
		// And for a DELETE we have no body, so we pass the CSRF token in the headers.
		client.delete().uri("/venues/2").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/venues/2"));

		// Check that one row is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	@DirtiesContext
	public void deleteVenueWithUser_With_No_Events() {
		int currentRows = countRowsInTable("venues");
		String[] tokens = login();

		// The session ID cookie holds our login credentials.
		// And for a DELETE we have no body, so we pass the CSRF token in the headers.
		client.delete().uri("/venues/1").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/venues"));

		// Check that one row is removed from the database.
		assertThat(currentRows -1, equalTo(countRowsInTable("venues")));
	}

	//delete a not found event
	@Test
	public void deleteVenueNotFound() {
		int currentRows = countRowsInTable("venues");
		String[] tokens = login();

		// The session ID cookie holds our login credentials.
		// And for a DELETE we have no body, so we pass the CSRF token in the headers.
		client.delete().uri("/venue/99").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isNotFound();

		// Check nothing is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}




}
