### 페이징 처리
* Spring Data Core와 Spring Data JPA에서 제공하는 페이징기능을 추가하고 싶어요. 페이징기능을 추가하고 처리한 결과를 md 문서로 작성해 주세요.
* 페이징 테스트를 하기 위해서 @V2__insert_department.sql를 참고하여 데이터를 더 추가하고 싶어요. Version3.sql 을 작성해 주세요.
* DepartmentController의 getDepartmentsPage() 와 EmployeeController의 getEmployeesPage() 메서드를 테스트하고 싶어요.
* 서버에 추가된 페이징 기능을 React 클라이언트에 요청하여 구현하고 싶어요. 추가된 서버의 페이징 기능을 잘 설명하기 위한 내용을 paging.md에 갱신해 주세요. 
* @net.restapi.emp.security.userinfo.UserInfo 클래스에 대한 V4_create_userinfo.sql과 V5_insert_userinfo.sql 를 작성해 주세요.
* jwt 토큰 생성을 위한 코드를 작성하였어요. 개선한 점을 수정해 주세요. 코드를 개선한 결과와 jwt 토큰생성에 대한 내용을 md 문서로 작성해 주세요.
* @image/Admin토큰생성.png, @image/Admin토큰_employees_403.png 를 보면 api/employees 를 Admin 토큰으로 요청하면 왜 403 오류가 발생하나요?
```  
  Role이 여러개 일때 처리하는 코드
   public UserInfoUserDetails(UserInfo userInfo) {
    this.userInfo = userInfo;
    this.email=userInfo.getEmail();
    this.password=userInfo.getPassword();
    this.authorities= Arrays.stream(userInfo.getRoles().split(","))
    .map(SimpleGrantedAuthority::new)
    .collect(Collectors.toList());
    }
     user_info 테이블 데이터
   1 | adminboot | admin@aa.com | $2a$10$mkMeA3UpwNIwdEv1v0IvruIBXemqLjxUxKhtEkWIbiot8szzs4GHC | ROLE_ADMIN,ROLE_USER
   Admin의 Role이 여러개 일때 처리를 하였음   
```
```
@security/userinfo/CurrentUser를 적용하여 EmployeeController 에서 아래와 같이 구현하였는데 코드를 좀 더 수정해야 할 것 같아요
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees(@CurrentUser UserInfo currentUser){
        if (currentUser != null) {
            List<EmployeeDto> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
        }else {
            return ResponseEntity.noContent().build();
        }
    }
```
jwt 토큰을 검증하는 로직 ( filter ) 와 SecurityConfig 코드에 개선할 점이 있다면 수정하고  jwt.md 문서에 반영해 주세요.


```
POST
http://localhost:8080/userinfos/login
{
	"email":"admin@aa.com",
	"password":"pwd1"
}
user_info 테이블의 데이터가 맞는데 왜 아래와 같은 오류가 발생하나요?
JWT token parsing failed: JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.

user@aa.com (ROLE_USER) 일반 User Role 계정으로 employees 목록을 요청함
http://localhost:8080/api/employees
Bearer Token eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGFhLmNvbSIsImlhdCI6MTc3NDQzMjU5NSwiZXhwIjoxNzc0NDM2MTk1fQ.c1GxMauI-koTYd1vVLBLLuzPdIMVqTM4nw9D4R0jcEI
권한없음 오류가 발생하였는데 statusCode는 403이 아니라 500으로 나옴  DefaultExceptionAdvice를 수정해야 하나요?
{
    "statusCode": 500,
    "message": "Access Denied",
    "timestamp": "2026-03-25 18:57:05 수 오후"
}

DefaultExceptionAdvice 클래스에 401 인증실패에 대한 내용도 추가해 주세요.

401,403 오류처리에 대한 내용도 jwt.md에 반영해 주세요.

백엔드의 인증과 권한, JWT 토큰생성과 토큰검증에 대한 내용을 ReactJS와 Zustand를 사용한 클라이언트 프로그램을 작성하려고 합니다. 클라이언트가 참조할 md문서를 작성해 주세요

SecurityConfig, JwtAuthenticationFilter, JwtService 클래스에 주석을 추가해 주세요.


클라이언트에서 아래와 같은 오류 발생함
http://localhost:8080/api/departments/page?pageNo=0&pageSize=5&sortBy=id&sortDir=asc' from origin 
'http://localhost:3000' has been blocked by CORS policy: Response to preflight request doesn't pass access control check: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.

jwt.md 문서에 내용을 반영해 주세요.   

```