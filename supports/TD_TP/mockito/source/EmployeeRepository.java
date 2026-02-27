package fr.amu.univ.miage.m1.glq.mockito;

import java.util.List;

public interface EmployeeRepository {

	List<Employee> findAll();

	Employee save(Employee e);
}
