package net.restapi.emp.repository;

import net.restapi.emp.dto.EmployeeDto;
import net.restapi.emp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    //Employee findByDepartment(Long id);
    Optional<EmployeeDto> findByEmail(String email);
}
