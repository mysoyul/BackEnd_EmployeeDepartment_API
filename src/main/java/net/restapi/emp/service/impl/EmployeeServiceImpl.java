package net.restapi.emp.service.impl;

import lombok.AllArgsConstructor;
import net.restapi.emp.dto.EmployeeDto;
import net.restapi.emp.dto.PageResponse;
import net.restapi.emp.entity.Department;
import net.restapi.emp.entity.Employee;
import net.restapi.emp.exception.ResourceNotFoundException;
import net.restapi.emp.mapper.EmployeeMapper;
import net.restapi.emp.repository.DepartmentRepository;
import net.restapi.emp.repository.EmployeeRepository;
import net.restapi.emp.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public EmployeeDto getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(EmployeeMapper::mapToEmployeeDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee is not exists with given email : " + email,
                                HttpStatus.NOT_FOUND));

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
    public PageResponse<EmployeeDto> getEmployeesPage(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Employee> page = employeeRepository.findAll(pageable);

        List<EmployeeDto> content = page.getContent()
                .stream()
                .map(EmployeeMapper::mapToEmployeeDto)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
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
