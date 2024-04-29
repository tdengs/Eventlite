package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.TwitterService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	private TwitterService twitterService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	//need to write test for this
	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {

		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));

		model.addAttribute("event", event);
		model.addAttribute("title", "Events");
		return "events/show" ;
	}

	@GetMapping
	public String getAllEvents(Model model) throws TwitterException {

		model.addAttribute("future", eventService.findAllUpcoming());
		model.addAttribute("past", eventService.findAllPast());
		model.addAttribute("tweetfeed", twitterService.getTimeLine());

		return "events/index";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");
		return "redirect:/events";
	}




	@GetMapping("update/{id}")
	public String showUpdateEvent(@PathVariable("id") long id, Model model) {

		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));

		model.addAttribute("event", event);
		model.addAttribute("venues", venueService.findAll());

		return "events/update" ;
	}

	@PostMapping(value ="/updateEvent", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors, Model model) {
		
		if (errors.hasErrors()) {
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("event", event);
			return "events/update";
		}

		eventService.save(event);

		return "redirect:/events";

	}

	@GetMapping("/search")
	public String searchResults(@RequestParam(name="term") String nameSearch, Model model) {
		//Might need to make this a required term in future
		model.addAttribute("future", eventService.findByClosestNameUpcoming(nameSearch));
		model.addAttribute("past", eventService.findByClosestNamePast(nameSearch));
		return "/events/search";
	}


	@GetMapping("/new")
	public String newEvent(Model model) {
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
		}

		//model.addAttribute("event", new Event());
		model.addAttribute("venues", venueService.findAll());

		return "events/new";
	}

	@PostMapping(value = "/newEvent", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("event", event);
			return "events/new";
		}

		model.addAttribute("venues", venueService.findAll());

		eventService.save(event);
//		redirectAttrs.addFlashAttribute("ok_message", "New event added.");

		return "redirect:/events";
	}
	
	@GetMapping(value = "/postTweet")
	// Use the request header annotation to get the previous page
	public String postTweet(@RequestHeader String referer, @RequestParam(name="tweet") String tweetContent, RedirectAttributes redirectAttrs, Model model) throws TwitterException {
		// Gotta add the form in the html
		TwitterService.createTweet(tweetContent);
		// need to figure out how to have a popup to thank them for tweeting


		redirectAttrs.addFlashAttribute("ok_message", "Your tweet: '"+tweetContent+"' was posted successfully.");
		return "redirect:" + referer;

	}

}
