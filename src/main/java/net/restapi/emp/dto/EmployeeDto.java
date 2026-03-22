package net.restapi.emp.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeDto {
    private Long id;

    @NotBlank(message = "직원 firstName은 필수 입력 항목입니다.")
    private String firstName;

    @NotBlank(message = "직원 lastName은 필수 입력 항목입니다.")
    private String lastName;

    @NotBlank(message = "직원 email은 필수 입력 항목입니다.")
    private String email;

    @NotNull(message = "직원의 부서코드는 필수 입력 항목입니다.") // null 방지 (필수)
    @Positive(message = "올바른 부서 형식이 아닙니다.") // 0 또는 음수 방지
    private Long departmentId;

    private DepartmentDto departmentDto;

    public EmployeeDto(Long id,String firstName,String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }


    public EmployeeDto(Long id,String firstName,String lastName, String email, Long departmentId) {
        this(id,firstName,lastName,email);
        this.departmentId = departmentId;
    }


    public EmployeeDto(Long id,String firstName,String lastName, String email, DepartmentDto departmentDto) {
        this(id,firstName,lastName,email);
        this.departmentDto = departmentDto;
    }
}
