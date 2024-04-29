package uk.ac.man.cs.eventlite.dao;


import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event,Long>{
	public Iterable<Event> findAllByOrderByDateAscTimeAsc();
	
	public Iterable<Event> findByDateAfterOrderByDateAscNameAsc(LocalDate date);
	
	public Iterable<Event> findByDateBeforeOrderByDateDescNameAsc(LocalDate date);
	
	public Iterable<Event> findAllByNameIgnoreCaseContainingOrderByNameAsc(String nameSearch);
	
	public Iterable<Event> findAllByNameIgnoreCaseContainingAndDateAfterOrderByDateAscNameAsc(String nameSearch, LocalDate date);
	
	public Iterable<Event> findAllByNameIgnoreCaseContainingAndDateBeforeOrderByDateDescNameAsc(String nameSearch, LocalDate date);

	public Iterable<Event> findByVenueEquals(Venue venue);

	public Iterable<Event> findTop3ByDateAfterOrderByDateAscNameAsc(LocalDate yesterday);
	
}
