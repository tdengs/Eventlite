package uk.ac.man.cs.eventlite.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class EventTest {
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
	public void testGetEventName() throws Exception {
		assertThat(one.getName(), is(equalTo("Event")));
	}

	@Test
	public void testToString() throws Exception {
		String result = one.getDescription();
		assertThat(result, containsString("Description"));
	}
	

	@Test
	public void testEquals() throws Exception {
		Event two = new Event();
		two.setId(one.getId());
		two.setName(one.getName());
		two.setDate(one.getDate());
		two.setTime(one.getTime());
		two.setDescription(one.getDescription());
		two.setVenue(one.getVenue());

		assertThat(one.equals(one), is(true));
		
		two.setId(2);
		assertThat(one.equals(two), is(false));
		assertThat(two.equals(one), is(false));
	}
	@Test
	public void testNotEmpty() throws Exception{
		
		assertFalse(one.getName().isEmpty());
		assertNotEquals(one.getDate(), null);
		assertNotEquals(one.getVenue(), null);
		
		
		
	}
	@Test
	public void testValidDate() throws Exception {
		one.setDate(LocalDate.parse("1998-06-10"));
		Set<ConstraintViolation<Event>> violations = validator.validate(one);
		assertFalse(violations.isEmpty());
	}
	
	@Test
	public void testValidName() throws Exception {
		one.setDate(LocalDate.parse("2023-06-10"));
		one.setName("asldjlasjajhaslkdhflakjshflkjashlskjdhfkajshlfuasuefhalkuhkjhkuvgaliusghkfjghlksdghlaksgfluagslkflaksugfliuagfkausgflkasgfluagsflkaugsfluagslfkagslufgalisufgalkusgelgalsfhdgahgslfagsiuyfgakhsgdlgfalisgflaisgflasugflasgflasgfliasgdflasgflasgflkausgdlfiuasgliefgalysglaiygeifly");
		Set<ConstraintViolation<Event>> violations = validator.validate(one);
		assertFalse(violations.isEmpty());
	}
	
	@Test
	public void testValidDescription() throws Exception {
		one.setName("Testing event");
		one.setDescription("World War II or the Second World War, often abbreviated as WWII or WW2, was a global war that lasted from 1939 to 1945. It involved the vast majority of the world's countries—including all of the great powers—forming two opposing military alliances: the Allies and the Axis powers. In a total war directly involving more than 100 million personnel from more than 30 countries, the major participants threw their entire economic, industrial, and scientific capabilities behind the war effort, blurring the distinction between civilian and military resources. Aircraft played a major role in the conflict, enabling the strategic bombing of population centres and the only two uses of nuclear weapons in war. World War II was by far the deadliest conflict in human history; it resulted in 70 to 85 million fatalities, a majority being civilians. Tens of millions of people died due to genocides (including the Holocaust), starvation, massacres, and disease. In the wake of the Axis defeat, Germany and Japan were occupied, and war crimes tribunals were conducted against German and Japanese leaders. ");
		Set<ConstraintViolation<Event>> violations = validator.validate(one);
		assertFalse(violations.isEmpty());
	}
	
	
}
