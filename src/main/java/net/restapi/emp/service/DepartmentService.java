package net.restapi.emp.service;

import net.restapi.emp.dto.DepartmentDto;
import net.restapi.emp.dto.PageResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto departmentDto);

    DepartmentDto getDepartmentById(Long departmentId);

    List<DepartmentDto> getAllDepartments();

    PageResponse<DepartmentDto> getDepartmentsPage(int pageNo, int pageSize, String sortBy, String sortDir);

    DepartmentDto updateDepartment(Long departmentId, DepartmentDto updatedDepartment);

    void deleteDepartment(Long departmentId);
}
