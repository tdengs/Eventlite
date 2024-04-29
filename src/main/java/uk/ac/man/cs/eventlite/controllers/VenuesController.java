package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "venues/not_found";
	}

	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) {

		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		

		model.addAttribute("venue", venue);
		model.addAttribute("event", eventService.getByVenue(venue));
		

		return "venues/show" ;
	}
	
	@GetMapping
	public String getAllVenues(Model model) {

		model.addAttribute("venues", venueService.findAll());

		return "venues/index";
	}
	
	@GetMapping("/search")
	public String searchResults(@RequestParam(name="term") String nameSearch, Model model) {
		//Might need to make this a required term in future
		model.addAttribute("venues", venueService.findByName(nameSearch));
		return "/venues/searchResult";
	}
	
	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {

		for (Venue V : venueService.findAll()) {
			if (V.getId()==id) {
				if (V.getEvents().size()!=0){
					redirectAttrs.addFlashAttribute("ok_message", "Venue cannot be deleted as it has events associated with it.");
					String returnString = ("redirect:/venues/"+id);
					return returnString;
				}
				else {
					venueService.deleteById(id);
					redirectAttrs.addFlashAttribute("ok_message", "Venue deleted.");
					return "redirect:/venues";
					}
			}
		}
		return "redirect:/venues";
		
	}


	@GetMapping("update/{id}")
	public String showUpdateVenue(@PathVariable("id") long id, Model model) {

		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));

		model.addAttribute("venue", venue);
		//model.addAttribute("venues", venueService.findAll());

		return "venues/update" ;
	}

	@PostMapping(value ="/updateVenue", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateVenue(@RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors, Model model, RedirectAttributes redirectAttrs) throws InterruptedException {
		
		if (errors.hasErrors()) {
			//model.addAttribute("venues", venueService.findAll());
			model.addAttribute("venue", venue);
			return "venues/update";
		}
		venueService.getCoordinates(venue.getAddress(), venue);
		Thread.sleep(1000L);
		if(venue.getLatitude() < -90){
			model.addAttribute("venue", venue);
			redirectAttrs.addFlashAttribute("ok_message", "Location not Found");
			return "venues/update";
		}

		venueService.save(venue);

		return "redirect:/venues";

	}
	
	@GetMapping("/new")
	public String newVenue(Model model) {
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}

		//model.addAttribute("event", new Event());
		//model.addAttribute("venues", venueService.findAll());

		return "venues/new";
	}

	@PostMapping(value = "/newVenue", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createVenue(@RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) throws InterruptedException{

		if (errors.hasErrors()) {
			//model.addAttribute("venues", venueService.findAll());
			model.addAttribute("venue", venue);
			return "venues/new";
		}

		//model.addAttribute("venues", venueService.findAll());
		venueService.getCoordinates(venue.getAddress(), venue);
		Thread.sleep(1000L);
		if(venue.getLatitude() < -90){
			model.addAttribute("venue", venue);
			redirectAttrs.addFlashAttribute("ok_message", "Location not Found");
			return "venues/new";
		}
		venueService.save(venue);


		return "redirect:/venues";
	}
	
	

}
