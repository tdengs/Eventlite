package uk.ac.man.cs.eventlite.entities;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Pattern;
import com.mapbox.api.geocoding.v5.*;
import com.mapbox.geojson.*;


import java.util.List;

@Entity
@Table(name = "venues")
public class Venue {
	@Id
	@GeneratedValue
	private long id;

	@Size(max = 255, message = "Length of name have to be less than 256 characters")
	@NotEmpty(message = "Name cannot be empty")
	private String name;

	@Positive(message ="Capacity must be positive")
	private int capacity;

	@OneToMany(mappedBy = "venue")
	List<Event> events;

	@Size(max = 299, message = "Length of name have to be less than 300 characters")
	@NotEmpty(message = "address cannot be empty")
	@Pattern(regexp = "^[a-zA-Z0-9,.' ]+[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$", message = "The address must contain a UK postcode.")
	public String address;
	
	private double latitude;
	private double longitude;

	public  Venue(){

	}
	public Venue(long id, String name,int capacity, List<Event> events, String address, double longitude, double latitude) {
		this.id = id;
		this.name = name;
		this.capacity = capacity;
		this.events = events;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;

	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
