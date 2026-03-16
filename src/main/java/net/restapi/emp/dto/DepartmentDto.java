package net.restapi.emp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {
    private Long id;

    @NotBlank(message = "부서 이름은 필수 입력 항목입니다.")
    private String departmentName;

    @NotBlank(message = "부서 설명은 필수 입력 항목입니다.")
    private String departmentDescription;
}
