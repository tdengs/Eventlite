package uk.ac.man.cs.eventlite.config.data;

import org.slf4j.Logger;
import java.time.LocalDate;
import java.time.LocalTime;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;


	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.

			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {

				Venue house2 = new Venue();
				house2.setName("Colab");
				house2.setCapacity(100);
				house2.setAddress("123 Main St Swindon SN2 2DQ");
				venueService.getCoordinates(house2.getAddress(), house2);
				Thread.sleep(1000L);
				venueService.save(house2);

				Venue house3 = new Venue();
				house3.setName("Killburn 1.8");
				house3.setCapacity(100);
				house3.setAddress("Oxford Rd, Manchester M13 9PL");
				venueService.getCoordinates(house3.getAddress(), house3);
				Thread.sleep(1000L);
				venueService.save(house3);

				Venue house5 = new Venue();
				house5.setName("MCED");
				house5.setCapacity(100);
				house5.setAddress("M13 9SS");
				venueService.getCoordinates(house5.getAddress(), house5);
				Thread.sleep(1000L);
				venueService.save(house5);

				Venue house4 = new Venue();
				house4.setName("Parliment");
				house4.setCapacity(100);
				house4.setAddress("London SW1A 0AA");
				venueService.getCoordinates(house4.getAddress(), house4);
				Thread.sleep(1000L);
				venueService.save(house4);

				Event event = new Event();
				event.setName("PASS2");
				//need find format later on
				event.setDate(LocalDate.parse("2022-05-10"));
				event.setTime(LocalTime.parse("16:00"));
				event.setVenue(house3);
				event.setDescription("This is a session to learn some imformation some students from the year above think would be usefull");
				eventService.save(event);
				
				Event SoftEnd = new Event();
				SoftEnd.setName("Software Engineering Check In");
				//need find format later on
				SoftEnd.setDate(LocalDate.parse("2022-05-10"));
				SoftEnd.setTime(LocalTime.parse("16:00"));
				SoftEnd.setVenue(house3);
				SoftEnd.setDescription("Check in meeting with TA");
				eventService.save(SoftEnd);

				Event event1 = new Event();
				event1.setName("Soft Eng Exam");
				//need find format later on
				event1.setDate(LocalDate.parse("2022-05-24"));
				event1.setTime(LocalTime.parse("16:00"));
				event1.setVenue(house5);
				event1.setDescription("Exam help me please");
				eventService.save(event1);

				Event event2 = new Event();
				event2.setName("Paradigms Exam");
				//need find format later on
				event2.setDate(LocalDate.parse("2022-06-01"));
				event2.setTime(LocalTime.parse("16:00"));
				event2.setVenue(house5);
				event2.setDescription("Exam why do i hate myself");
				eventService.save(event2);

				Event event3 = new Event();
				event3.setName("Algo Exam");
				//need find format later on
				event3.setDate(LocalDate.parse("2022-05-22"));
				event3.setTime(LocalTime.parse("16:00"));
				event3.setVenue(house5);
				event3.setDescription("please shot me now ");
				eventService.save(event3);

				Event event4 = new Event();
				event4.setName("Machine learning  Exam");
				//need find format later on
				event4.setDate(LocalDate.parse("2022-05-26"));
				event4.setTime(LocalTime.parse("16:00"));
				event4.setVenue(house3);
				event4.setDescription("If you fail you die ");
				eventService.save(event4);

				Event event5 = new Event();
				event5.setName(" AI Exam");
				//need find format later on
				event5.setDate(LocalDate.parse("2022-06-06"));
				event5.setTime(LocalTime.parse("16:00"));
				event5.setVenue(house3);
				event5.setDescription("Skynet is coming");
				eventService.save(event5);

				Event event6 = new Event();
				event6.setName(" Computer Vision Exam");
				//need find format later on
				event6.setDate(LocalDate.parse("2022-06-10"));
				event6.setTime(LocalTime.parse("16:00"));
				event6.setVenue(house4);
				event6.setDescription("Deadline is coming");
				eventService.save(event6);

			}
		};
	}
}
