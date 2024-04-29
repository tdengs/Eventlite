package uk.ac.man.cs.eventlite.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class VenueTest {
	private Validator validator;
	
	@Mock
	private Venue venue;

	private Event one;
	


	@BeforeEach
	public void setup() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		venue = new Venue();
		venue.setName("Home");
		venue.setCapacity(100);
		venue.setAddress("123 Main St Swindon SN2 2DQ");
		one = new Event();
		one.setId(1);
		one.setName("Event");
		one.setDate(LocalDate.parse("2022-06-10"));
		one.setTime(LocalTime.parse("16:00"));
		one.setDescription("Description");
		one.setVenue(venue);
	}

	@Test
	public void testGetVenueName() throws Exception {
		assertThat(venue.getName(), is(equalTo("Home")));
	}
	@Test
	public void testNotEmpty() throws Exception{
		
		assertFalse(venue.getName().isEmpty());
		assertFalse(venue.getAddress().isEmpty());
		int testint = venue.getCapacity();
		assertNotEquals(testint, 0);
		
		
	}
	@Test
	public void testInvalidCapacity() throws Exception {
		venue.setCapacity(-100);
		Set<ConstraintViolation<Venue>> violations = validator.validate(venue);
		assertFalse(violations.isEmpty());
	}
	
	@Test
	public void testInvalidName() throws Exception {
		venue.setCapacity(100);
		venue.setName("asldjlasjajhaslkdhflakjshflkjashlskjdhfkajshlfuasuefhalkuhkjhkuvgaliusghkfjghlksdghlaksgfluagslkflaksugfliuagfkausgflkasgfluagsflkaugsfluagslfkagslufgalisufgalkusgelgalsfhdgahgslfagsiuyfgakhsgdlgfalisgflaisgflasugflasgflasgfliasgdflasgflasgflkausgdlfiuasgliefgalysglaiygeifly");
		Set<ConstraintViolation<Venue>> violations = validator.validate(venue);
		assertFalse(violations.isEmpty());
	}
	@Test
	public void testInvalidAddress() throws Exception {
		venue.setName("Home");
		venue.setAddress("B******t address");
		Set<ConstraintViolation<Venue>> violations = validator.validate(venue);
		assertFalse(violations.isEmpty());
	}
	@Test
	public void testValidCapacity() throws Exception {
		venue.setCapacity(115);
		Set<ConstraintViolation<Venue>> violations = validator.validate(venue);
		assertTrue(violations.isEmpty());
	}
	
	@Test
	public void testValidName() throws Exception {
		
		venue.setName("NewHome");
		Set<ConstraintViolation<Venue>> violations = validator.validate(venue);
		assertTrue(violations.isEmpty());
	}
	@Test
	public void testValidAddress() throws Exception {
		
		venue.setAddress("Oxford Rd, Manchester M13 9PL");
		Set<ConstraintViolation<Venue>> violations = validator.validate(venue);
		assertTrue(violations.isEmpty());
	}
	
	
	


	
}
