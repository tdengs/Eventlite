package uk.ac.man.cs.eventlite.dao;


import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class EventServiceImpl implements EventService {

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
		
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscTimeAsc();
		
	}
	
	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}
	

	
	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}

	@Override
	public void delete(Event event) {
		eventRepository.delete(event);
	}

	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public void deleteAll() {
		eventRepository.deleteAll();
	}

	@Override
	public void deleteAll(Iterable<Event> events) {
		eventRepository.deleteAll(events);
	}

	@Override
	public void deleteAllById(Iterable<Long> ids) {
		eventRepository.deleteAllById(ids);
	}

	@Override
	public boolean existsById(long id) {
		return eventRepository.existsById(id);
	}


	@Override
	public Iterable<Event> findByClosestName(String nameSearch) {
		//Shortened name for simplicity
		return eventRepository.findAllByNameIgnoreCaseContainingOrderByNameAsc(nameSearch);
	}

	@Override
	public Iterable<Event> findByClosestNameUpcoming(String nameSearch) {
		//Shortened name for simplicity
		return eventRepository.findAllByNameIgnoreCaseContainingAndDateAfterOrderByDateAscNameAsc(nameSearch, LocalDate.now());
	}
	
	@Override
	public Iterable<Event> findByClosestNamePast(String nameSearch) {
		//Shortened name for simplicity
		return eventRepository.findAllByNameIgnoreCaseContainingAndDateBeforeOrderByDateDescNameAsc(nameSearch, LocalDate.now());
	}
	
	@Override
	public Iterable<Event> findAllUpcoming(){
		return eventRepository.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
	}
	
	@Override
	public Iterable<Event> findNextThreeEvents(){
		return eventRepository.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
	}

	@Override
	public Iterable<Event> findAllPast() {
		return eventRepository.findByDateBeforeOrderByDateDescNameAsc(LocalDate.now());
	}

	@Override
	public Iterable<Event> getByVenue(Venue venue) {
		return eventRepository.findByVenueEquals(venue);
	}
	
	@Override
	public Iterable<Event> findNext3Events() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		return eventRepository.findTop3ByDateAfterOrderByDateAscNameAsc(yesterday);
	}
}
