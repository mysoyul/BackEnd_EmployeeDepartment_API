package net.restapi.emp.repository;

import net.restapi.emp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    //Employee findByDepartment(Long id);
    @Query("SELECT em FROM Employee em WHERE em.email = :email")
    Optional<Employee> findByEmail(@Param("email") String email);
}
