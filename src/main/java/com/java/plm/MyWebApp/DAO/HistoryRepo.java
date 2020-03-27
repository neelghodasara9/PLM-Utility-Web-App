package com.java.plm.MyWebApp.DAO;

import org.springframework.data.repository.CrudRepository;

import com.java.plm.MyWebApp.model.Historylist;

public class HistoryRepo {
	public interface PersonRepository extends CrudRepository<Historylist, Integer> {}

}
