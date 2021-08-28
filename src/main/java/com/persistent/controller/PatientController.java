package com.persistent.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jasper.tagplugins.jstl.core.If;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.persistent.dto.AppointmentDto;
import com.persistent.dto.DoctorDto;
import com.persistent.dto.PatientDto;
import com.persistent.entities.AppointAJAX;
import com.persistent.entities.Appointment;
import com.persistent.entities.Doctor;
import com.persistent.entities.FeedBack;
import com.persistent.entities.Patient;
import com.persistent.entities.SearchAJAX;
import com.persistent.entities.User;
import com.persistent.service.AppointmentService;
import com.persistent.service.DoctorService;
import com.persistent.service.PatientService;
import com.persistent.service.UserService;

@Controller
public class PatientController {
	
	@Autowired
	private PatientService serv;
	
	@Autowired
	private UserService creadServ;
	
	@Autowired
	private DoctorService docServ;
	
	@Autowired
	private AppointmentService appServ;

	@RequestMapping(path="/save_patient", method=RequestMethod.POST)
	public String save_patient(@ModelAttribute PatientDto uInfo,@ModelAttribute User u)
	{
		System.out.println(uInfo);
		System.out.println(u);

		serv.addPatient(uInfo.conToPatient());
		creadServ.addUser(u);
        
		return "redirect:/";//redirected to user dashboard
	}
	
	@RequestMapping(path="/profile/{username}", method=RequestMethod.GET)
	public String user_profile(@PathVariable String username, Model model)
	{
		System.out.println(username);
		List<Appointment> appoints= appServ.getAllAppointment(username);
		List<Map<String,Object>> maps = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> mapsaccepted = new ArrayList<Map<String,Object>>();
		
		for (Iterator iterator = appoints.iterator(); iterator.hasNext();) {
			Appointment appointment = (Appointment) iterator.next();
			Map<String,Object> map = new HashMap<>();
			map.put("id", appointment.getId());
			map.put("timing", appointment.getTiming());
			map.put("fName", appointment.getDoc().getfName());
			map.put("contact", appointment.getDoc().getContactNo().toString());
			map.put("address", appointment.getDoc().getAddress().getCity() + ", " + appointment.getDoc().getAddress().getState());
			map.put("status", appointment.getStatus());
			map.put("feed", appointment.getFeed());
			if(appointment.getStatus().equals("pending"))
				maps.add(map);
			else
				mapsaccepted.add(map);
			
		}
		
		System.out.println(maps);
		model.addAttribute("pat",new PatientDto(serv.findPatientByUserName(username)));
		model.addAttribute("appoints", maps);
		model.addAttribute("appointsAccept", mapsaccepted);
		return "patient_profile";
	}

		@PostMapping("/search")
    public @ResponseBody List<DoctorDto> user_search_results(SearchAJAX t) {

        //getuser filtered list from database
		System.out.println(t.type+ " " + t.state + " " + t.city);
		List<Doctor> d = docServ.findBySpecialityAndAddress(t.type,t.state,t.city);
		List<DoctorDto> doctors = new ArrayList<DoctorDto>();
		for (Doctor doctor : d) {
			doctors.add(new DoctorDto(doctor));
		}
		System.out.println(doctors);
        //List<DoctorInfo> users = Arrays.asList(new DoctorInfo(),new DoctorInfo());
        return doctors;
    }
		
		@RequestMapping(path=("/doctor/{username}"), method=RequestMethod.GET)
		public String doctor_info(@ModelAttribute("pat") PatientDto pat,@PathVariable String username,Model model)
		{
			System.out.println(username);
			Doctor doc = docServ.findDoctorByUserName(username);
			List<Appointment> list = appServ.getAllAptHaveFeedForDoctor(username);
			Float avg = appServ.getAvgRating(username);
			
			System.out.println("*******************\n\n\n");
			System.out.println(model.getAttribute("pat"));
			System.out.println(doc);
			System.out.println(list);
			System.out.println(avg);
			
			model.addAttribute("doc",new DoctorDto(doc));
			model.addAttribute("appointments",list);
			model.addAttribute("avg",avg);
			return "doctor_info";
		}
		

		@RequestMapping(path=("/doctor/book_slot"), method=RequestMethod.POST)
		public @ResponseBody String book_slot(AppointAJAX appoint)
		{
			System.out.println(appoint.getDoc());
			
			Doctor d = docServ.findDoctorByUserName(appoint.getDoc());//To display required doctor's details
			Patient p = serv.findPatientByUserName("uday");//To display required patient's details
			
			
			Appointment ap = new AppointmentDto(appoint.getTiming(),appoint.getStatus()).convertToEntity();
			ap.setDoc(d);
			ap.setPat(p);
			
			appServ.addAppointment(ap);// saved an appointment in Appointment Table
			
			return "Your Appointment is booked";
		}
		
		@RequestMapping(path="/feedback", method=RequestMethod.GET)
		public String feedback(String id)
		{
			return "FeedBack";
		}
		
		@RequestMapping(path="/feedback_submit", method=RequestMethod.POST)
		public String feedbacksubmit(FeedBack feed, Integer apointment)
		{
			Appointment apt = appServ.getAppointment(apointment);
			apt.setFeed(feed);
			
			appServ.addAppointment(apt);
			System.out.println(apt);
			System.out.println(feed);
			return "redirect:/feedback?success";
		}

}
