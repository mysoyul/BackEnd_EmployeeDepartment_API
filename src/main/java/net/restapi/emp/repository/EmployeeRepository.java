package net.restapi.emp.repository;

import net.restapi.emp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    //Employee findByDepartment(Long id);
}
