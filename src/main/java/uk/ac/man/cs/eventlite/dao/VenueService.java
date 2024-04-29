package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.List;
import java.util.Optional;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();

	public Optional<Venue> findById(long id);

	public Venue save(Venue venue);
	
	public void delete(Venue venue);

	public void deleteById(long id);

	public void deleteAll();

	public void deleteAll(Iterable<Venue> venues);

	public void deleteAllById(Iterable<Long> ids);
	
	public boolean existsById(long id);
	
	public Iterable<Venue> findByName(String nameSearch);

	public void getCoordinates(String address, Venue venue);

	public Iterable<Venue> findTop3Venues();
}
