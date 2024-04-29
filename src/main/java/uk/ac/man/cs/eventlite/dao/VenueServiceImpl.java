package uk.ac.man.cs.eventlite.dao;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class VenueServiceImpl implements VenueService {


	
	@Autowired
	private VenueRepository venueRepository;

	@Autowired
	private EventService eventService;

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAllByOrderByNameAsc();
	}
	
	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}
	
	
	@Override
	public void delete(Venue venue) {
		venueRepository.delete(venue);
	}
	
	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}

	@Override
	public void deleteAll() {
		venueRepository.deleteAll();
	}

	@Override
	public void deleteAll(Iterable<Venue> venues) {
		venueRepository.deleteAll(venues);
	}

	@Override
	public void deleteAllById(Iterable<Long> ids) {
		venueRepository.deleteAllById(ids);
	}

	@Override
	public boolean existsById(long id) {
		return venueRepository.existsById(id);
	}



	@Override
	public Optional<Venue> findById(long id) {
		return venueRepository.findById(id);
	}

	@Override
	public Iterable<Venue> findByName(String nameSearch){
		return venueRepository.findAllByNameIgnoreCaseContainingOrderByNameAsc(nameSearch);
	}

	@Override
	public void getCoordinates(String address, Venue venue){
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1IjoiZzE3IiwiYSI6ImNsMTNmOGEyajAwNTczZWthN2NjeHEwNWcifQ.YBfuECXBRkmbx4OmPObCFQ")
				.query(address)
				.build();

		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

				List<CarmenFeature> results = response.body().features();

				if (results.size() > 0) {

					// Log the first results Point.
					Point firstResultPoint = results.get(0).center();
					venue.setLongitude(firstResultPoint.coordinates().get(0));
					venue.setLatitude(firstResultPoint.coordinates().get(1));


				} else {

					// No result for your request were found.

					venue.setLongitude(-100);
					venue.setLatitude(-100);
				}
			}

			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
				throwable.printStackTrace();
			}
		});



	}


	@Override
	public Iterable<Venue> findTop3Venues() {

		Iterable<Event> events = eventService.findAllUpcoming();

		List<Venue> venues = StreamSupport.stream(events.spliterator(), false)
				.collect(Collectors.groupingBy(Event::getVenue, Collectors.counting())).entrySet().stream()
				.sorted(Map.Entry.comparingByValue()).limit(3).map(e -> e.getKey()).collect(Collectors.toList());

		Collections.reverse(venues);

//		System.out.println(venues);
//		System.out.println(events);

		return venues;

	}

}

