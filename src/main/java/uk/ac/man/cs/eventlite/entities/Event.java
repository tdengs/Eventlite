package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;

import java.time.LocalTime;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.ac.man.cs.eventlite.dao.VenueService;

@Entity
@Table(name = "events")
public class Event {
	@Id
	@GeneratedValue
	private long id;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Future(message = "Please enter a future date")
	@NotNull(message = "Please enter a future date")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime time;

	@JsonBackReference
	@JoinColumn(name="venue_id")
	@ManyToOne
	@NotNull(message = "Venue cannot be empty")
	private Venue venue;
	
	@Size(max = 499, message = "The description have be less than 500 characters")
	private String description; 
	
	@Size(max = 255, message = "Length of name have to be less than 256 characters")
	@NotEmpty(message = "Name cannot be empty")
	private String name;

	public Event(String name,LocalTime time,LocalDate date,Venue venue, String description) {
		this.name = name;
		this.time = time;
		this.date = date;
		this.venue = venue;
		this.description = description; 
	}

	public Event() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

}
