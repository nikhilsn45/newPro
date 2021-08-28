package com.persistent.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.persistent.entities.Doctor;

public interface DoctorDao extends CrudRepository<Doctor,Integer>{

	Doctor findByUserName(String un);
	List<Doctor> findBySpecialityAndAddress_CityAndAddress_State(String speciality, String city, String state);

}
