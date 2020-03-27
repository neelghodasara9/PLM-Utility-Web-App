package com.java.plm.MyWebApp.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.plm.MyWebApp.DAO.HistoryRepo.PersonRepository;
import com.java.plm.MyWebApp.model.Historylist;

@Service
public class HistoryService {

	@Autowired
	PersonRepository personRepository;

	public List<Historylist> getAllPersons() {
		List<Historylist> history = new ArrayList<Historylist>();
		personRepository.findAll().forEach(person -> history.add(person));
		return history;
	}

	/*public Historylist getPersonById(int id) {
		return personRepository.findById(id).get();
	}*/

	public void saveOrUpdate(Historylist history) {
		personRepository.save(history);
	}

	/*public void delete(int id) {
		personRepository.deleteById(id);
	}*/
}
