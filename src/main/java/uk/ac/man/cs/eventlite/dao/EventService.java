package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Optional<Event> findById(long id);
	
	public Iterable<Event> findNextThreeEvents();
	public Iterable<Event> findAllUpcoming();
	public Iterable<Event> findAllPast();

	public Iterable<Event> findByClosestName(String nameSearch);
	public Iterable<Event> findByClosestNameUpcoming(String nameSearch);
	public Iterable<Event> findByClosestNamePast(String nameSearch);

	public Event save(Event event);
	
	public void delete(Event event);

	public void deleteById(long id);

	public void deleteAll();

	public void deleteAll(Iterable<Event> events);

	public void deleteAllById(Iterable<Long> ids);

	public boolean existsById(long id);

	public Iterable<Event> getByVenue(Venue venue);
	
	public Iterable<Event> findNext3Events();
	
	
	





	
}
