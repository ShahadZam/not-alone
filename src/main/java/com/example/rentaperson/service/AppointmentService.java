package com.example.rentaperson.service;

import com.example.rentaperson.dto.PostAppointment;
import com.example.rentaperson.dto.UserBody;
import com.example.rentaperson.model.Appointment;
import com.example.rentaperson.model.User;
import com.example.rentaperson.repository.AppointmentRepository;
import com.example.rentaperson.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    private final SendEmailService service;
    String title,body,body1,emailUser,emailPerson;





    public List<Appointment> getAll(){return appointmentRepository.findAll();}

    public void post(PostAppointment ap, Integer id){
        User person=userRepository.findUsersByUsername(ap.getUsername());
        Appointment appointment1=new Appointment(null,id,person.getId(),ap.getLocation(),ap.getHours(),person.getPricePerHour()*ap.getHours(),"new",ap.getDate(),ap.getRequest(),true,false);
        appointmentRepository.save(appointment1);
    }

    public List viewYourAppointments(User user){
        if(user.getRole().equals("PERSON")) {
            return appointmentRepository.findAppointmentByPersonId((user.getId()));
        }
        else{
            return appointmentRepository.findAppointmentByUserId((user.getId()));
        }
    }
    public void makeItConfirmed(User person,Integer id){
        Appointment appointment= appointmentRepository.
                findAppointmentByPersonIdAndIdAndStatus(person.getId(),id,"new");
        User user=userRepository.getById(appointment.getUserId());


        appointment.setStatus("confirmed");
        appointmentRepository.save(appointment);
        //send an email?
         title="-Not alone- Confirm your appointment";
         body="Your Appointment with "+person.getUsername()+" confirmed\n" +
                "location: "+appointment.getLocation()+"\n" +
                "hours: "+appointment.getHours()+"\n" +
                 "date: "+appointment.getDate()+"\n" +
                 "requests: "+appointment.getRequest()+"\n" +
                "total: "+appointment.getTotal()+"\n Have fun!";

         body1="Your Appointment with "+user.getUsername()+" confirmed\n" +
                "location: "+appointment.getLocation()+"\n" +
                "hours: "+appointment.getHours()+"\n" +
                 "date: "+appointment.getDate()+"\n" +
                 "requests: "+appointment.getRequest()+"\n" +
                "total: "+appointment.getTotal()+"\n Have fun!";
         emailUser=user.getEmail();
         emailPerson=person.getEmail();

    }




    public void updateApp(User user,Appointment appointment,Integer id) {
        Appointment newA=appointmentRepository.findAppointmentByIdAndUserId(id,user.getId());
        if(appointment.getLocation()!=null)
            newA.setLocation(appointment.getLocation());
        if(appointment.getHours()!=null)
            newA.setHours(appointment.getHours());
        if(appointment.getDate()!=null)
            newA.setDate(appointment.getDate());
        if(appointment.getRequest()!=null)
            newA.setRequest(appointment.getRequest());
        appointmentRepository.save(newA);
    }

    public void updateAppReview(Integer id) {
        Appointment newA=appointmentRepository.findAppointmentById(id);
        newA.setCanAddReview(false);
        appointmentRepository.save(newA);
    }

    public void deleteApp(User user,Integer id) {
        Appointment appointment=appointmentRepository.findAppointmentByIdAndUserId(id,user.getId());
        appointmentRepository.delete(appointment);
    }

    public List viewConfirm(User user){
        if(user.getRole().equals("PERSON")){
            return appointmentRepository.findAppointmentByPersonIdAndStatus(user.getId(),"confirmed");
        }
        else{
            return appointmentRepository.findAppointmentByUserIdAndStatus(user.getId(),"confirmed");
        }
    }

    public List viewNew(User user){
        if(user.getRole().equals("PERSON")){
            return appointmentRepository.findAppointmentByPersonIdAndStatus(user.getId(),"new");
        }
        else{
            return appointmentRepository.findAppointmentByUserIdAndStatus(user.getId(),"new");
        }
    }

    public List viewCompleted(User user) {
        if (user.getRole().equals("PERSON")) {
            return appointmentRepository.findAppointmentByPersonIdAndStatus(user.getId(), "completed");
        } else {
            return appointmentRepository.findAppointmentByUserIdAndStatus(user.getId(), "completed");
        }
    }

        public void makeItCanceled(User person, Integer id) {
            Appointment appointment= appointmentRepository.
                    findAppointmentByPersonIdAndIdAndStatus(person.getId(),id,"new");
            User user=userRepository.getById(appointment.getUserId());
            appointment.setStatus("canceled");
            appointmentRepository.save(appointment);
        }


    public List<PostAppointment> formatList(List<Appointment> app) {
        List<PostAppointment> appBody =new ArrayList<>();
        User user;
        for (Appointment value : app) {
            user=userRepository.getById(value.getPersonId());
                appBody.add(new PostAppointment(value.getId(),user.getUsername(), value.getLocation(),value.getHours(), value.getTotal(),value.getStatus(),value.getDate(),value.getRequest()));
        }
        return appBody;
    }

    public void makeItCompleted(User person, Integer id) {
        Appointment appointment= appointmentRepository.
                findAppointmentByPersonIdAndIdAndStatus(person.getId(),id,"confirmed");
        User user=userRepository.getById(appointment.getUserId());
        appointment.setStatus("completed");
        appointmentRepository.save(appointment);
    }

    public void makeItPaid(Integer id,User user) {
        Appointment appointment=appointmentRepository.findAppointmentByIdAndUserId(id,user.getId());
        appointment.setPayed(true);

        appointmentRepository.save(appointment);
        try {
            service.sendEmail(emailUser, title, body);
            service.sendEmail(emailPerson, title, body1);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public Appointment getAppById(Integer id,User user) {
        return appointmentRepository.findAppointmentByIdAndUserId(id,user.getId());
    }
}


