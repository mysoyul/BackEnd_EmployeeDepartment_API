package net.javaguides.ems.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.ems.dto.EmployeeDto;
import net.javaguides.ems.entity.Department;
import net.javaguides.ems.entity.Employee;
import net.javaguides.ems.exception.ResourceNotFoundException;
import net.javaguides.ems.mapper.DepartmentMapper;
import net.javaguides.ems.mapper.EmployeeMapper;
import net.javaguides.ems.repository.DepartmentRepository;
import net.javaguides.ems.repository.EmployeeRepository;
import net.javaguides.ems.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private EmployeeRepository employeeRepository;

    private DepartmentRepository departmentRepository;

    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {

        Employee employee = EmployeeMapper.mapToEmployee(employeeDto);

        Department department = departmentRepository.findById(employeeDto.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department is not exists with id: " +
                                employeeDto.getDepartmentId(),
                                HttpStatus.NOT_FOUND));

        employee.setDepartment(department);

        Employee savedEmployee = employeeRepository.save(employee);
        return EmployeeMapper.mapToEmployeeDto(savedEmployee);
    }

    @Override
    public EmployeeDto getEmployeeById(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee is not exists with given id : " + employeeId,
                                HttpStatus.NOT_FOUND));

        return EmployeeMapper.mapToEmployeeDto(employee);
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(EmployeeMapper::mapToEmployeeDto)
                .toList();
                //.map((employee) -> EmployeeMapper.mapToEmployeeDto(employee))
                //.collect(Collectors.toList());
    }

    public List<EmployeeDto> getAllEmployeesDepartment() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeMapper::mapToEmployeeDepartmentDto)
                .toList();
    }

    @Override
    public EmployeeDto updateEmployee(Long employeeId, EmployeeDto updatedEmployee) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee is not exists with given id: " + employeeId,
                                HttpStatus.NOT_FOUND)
        );

        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setEmail(updatedEmployee.getEmail());

        Department department = departmentRepository.findById(updatedEmployee.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department is not exists with id: " + updatedEmployee.getDepartmentId(),
                                HttpStatus.NOT_FOUND
                                ));

        employee.setDepartment(department);

        Employee updatedEmployeeObj = employeeRepository.save(employee);

        return EmployeeMapper.mapToEmployeeDto(updatedEmployeeObj);
    }

    @Override
    public void deleteEmployee(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee is not exists with given id: " + employeeId,
                        HttpStatus.NOT_FOUND)
        );

        employeeRepository.deleteById(employeeId);
    }
}
