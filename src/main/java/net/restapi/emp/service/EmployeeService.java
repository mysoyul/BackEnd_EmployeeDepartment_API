package net.restapi.emp.service;

import net.restapi.emp.dto.EmployeeDto;
import net.restapi.emp.dto.PageResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    EmployeeDto getEmployeeById(Long employeeId);

    List<EmployeeDto> getAllEmployees();

    List<EmployeeDto> getAllEmployeesDepartment();

    PageResponse<EmployeeDto> getEmployeesPage(int pageNo, int pageSize, String sortBy, String sortDir);

    EmployeeDto updateEmployee(Long employeeId, EmployeeDto updatedEmployee);

    void deleteEmployee(Long employeeId);

    EmployeeDto getEmployeeByEmail(String email);
}
