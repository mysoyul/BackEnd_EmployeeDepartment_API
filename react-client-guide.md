# React + Zustand 클라이언트 구현 가이드

백엔드 API(Spring Boot + JWT)와 연동하는 ReactJS + Zustand 클라이언트 구현 참조 문서입니다.

---

## 1. 백엔드 API 개요

| 항목 | 값 |
|---|---|
| Base URL | `http://localhost:8080` |
| 인증 방식 | JWT Bearer Token |
| 토큰 유효 시간 | 3600초 (60분) |
| Content-Type | `application/json` |

---

## 2. 전체 API 엔드포인트

### 2-1. 인증 (토큰 불필요)

| Method | URL | 설명 |
|---|---|---|
| POST | `/userinfos/new` | 회원 가입 |
| POST | `/userinfos/login` | 로그인 (JWT 토큰 발급) |

### 2-2. 직원 API

| Method | URL | 필요 권한 | 설명 |
|---|---|---|---|
| GET | `/api/employees/welcome` | 없음 | 공개 경로 |
| GET | `/api/employees` | `ROLE_ADMIN` | 전체 직원 목록 |
| GET | `/api/employees/{id}` | `ROLE_USER` | 직원 단건 조회 |
| GET | `/api/employees/email/{email}` | `ROLE_USER` | 이메일로 직원 조회 |
| GET | `/api/employees/departments` | 인증 | 직원+부서 목록 |
| GET | `/api/employees/page` | 인증 | 직원 페이징 조회 |
| POST | `/api/employees` | 인증 | 직원 등록 |
| PUT | `/api/employees/{id}` | 인증 | 직원 수정 |
| DELETE | `/api/employees/{id}` | 인증 | 직원 삭제 |

### 2-3. 부서 API

| Method | URL | 필요 권한 | 설명 |
|---|---|---|---|
| GET | `/api/departments` | 인증 | 전체 부서 목록 |
| GET | `/api/departments/{id}` | 인증 | 부서 단건 조회 |
| GET | `/api/departments/page` | 인증 | 부서 페이징 조회 |
| POST | `/api/departments` | 인증 | 부서 등록 |
| PUT | `/api/departments/{id}` | 인증 | 부서 수정 |
| DELETE | `/api/departments/{id}` | 인증 | 부서 삭제 |

---

## 3. 요청 / 응답 데이터 형식

### 3-1. 로그인

**요청**
```json
POST /userinfos/login
{
  "email": "admin@aa.com",
  "password": "pwd1"
}
```

**응답** — JWT 토큰 문자열 (plain text)
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbi4uLiIsImlhdCI6...
```

> 응답 `Content-Type`이 `text/plain`입니다. JSON이 아니므로 `response.data`로 바로 사용합니다.

---

### 3-2. 회원 가입

**요청**
```json
POST /userinfos/new
{
  "name": "홍길동",
  "email": "user@aa.com",
  "password": "pwd1",
  "roles": "ROLE_USER"
}
```

> `roles` 값은 반드시 `ROLE_USER` 또는 `ROLE_ADMIN` 형식으로 입력해야 합니다.
> 복수 권한은 `"ROLE_ADMIN,ROLE_USER"` 형식으로 입력합니다.

---

### 3-3. 직원 (EmployeeDto)

```typescript
interface EmployeeDto {
  id?: number;
  firstName: string;   // 필수
  lastName: string;    // 필수
  email: string;       // 필수
  departmentId: number; // 필수 (양수)
  departmentDto?: DepartmentDto; // GET /api/employees/departments 응답에 포함
}
```

### 3-4. 부서 (DepartmentDto)

```typescript
interface DepartmentDto {
  id?: number;
  departmentName: string;        // 필수
  departmentDescription: string; // 필수
}
```

### 3-5. 페이징 응답 (PageResponse)

```typescript
interface PageResponse<T> {
  content: T[];
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
```

**페이징 요청 파라미터**
```
GET /api/employees/page?pageNo=0&pageSize=10&sortBy=id&sortDir=asc
```

---

## 4. 에러 응답 형식

### 4-1. 표준 에러 (ErrorObject)

```typescript
interface ErrorObject {
  statusCode: number;
  message: string;
  timestamp: string; // "2026-03-25 18:57:05 수 오후"
}
```

### 4-2. 에러 코드별 응답 예시

**401 — 인증 실패 (토큰 없음 / 만료 / 잘못된 자격증명)**
```json
{
  "statusCode": 401,
  "message": "Full authentication is required to access this resource",
  "timestamp": "2026-03-25 18:57:05 수 오후"
}
```

**401 — 만료/위변조 토큰 (필터에서 직접 반환)**
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token"
}
```

**403 — 권한 부족**
```json
{
  "statusCode": 403,
  "message": "Access Denied",
  "timestamp": "2026-03-25 18:57:05 수 오후"
}
```

**400 — 입력값 검증 실패 (ValidationErrorResponse)**
```json
{
  "status": 400,
  "message": "입력항목 검증 오류",
  "timestamp": "2026-03-25T18:57:05",
  "errors": {
    "firstName": "직원 firstName은 필수 입력 항목입니다.",
    "departmentId": "직원의 부서코드는 필수 입력 항목입니다."
  }
}
```

---

## 5. JWT 토큰 구조

로그인 성공 후 발급된 토큰을 `jwt.io`에서 디코딩하면:

```json
// Header
{ "alg": "HS256" }

// Payload
{
  "sub": "admin@aa.com",   // 사용자 이메일 (username)
  "iat": 1742745600,       // 발급 시간 (Unix timestamp)
  "exp": 1742749200        // 만료 시간 (발급 + 3600초)
}
```

> **주의**: 토큰 Payload에 roles 정보가 없습니다.
> 클라이언트에서 역할(role) 정보가 필요하면 로그인 응답과 별도로 관리해야 합니다.

---

## 6. React + Zustand 구현

### 6-1. 패키지 설치

```bash
npm install zustand axios
```

### 6-2. Axios 인스턴스 (`src/api/axiosInstance.js`)

모든 API 요청에 자동으로 Bearer 토큰을 추가하고 401/403을 중앙 처리합니다.

```javascript
import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
});

// 요청 인터셉터 — 모든 요청에 Authorization 헤더 자동 추가
axiosInstance.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터 — 401/403 중앙 처리
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // 토큰 만료 또는 미인증 → 로그아웃 후 로그인 페이지 이동
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }

    if (status === 403) {
      // 권한 부족 → 접근 거부 페이지 이동
      window.location.href = '/forbidden';
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
```

### 6-3. Zustand 인증 스토어 (`src/store/authStore.js`)

```javascript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,       // JWT 토큰 문자열
      email: null,       // 로그인한 사용자 이메일
      roles: [],         // 권한 목록 ["ROLE_ADMIN", "ROLE_USER"]

      // 로그인 성공 시 호출
      login: (token, email, roles) => set({ token, email, roles }),

      // 로그아웃
      logout: () => set({ token: null, email: null, roles: [] }),

      // 특정 권한 보유 여부 확인
      hasRole: (role) => {
        const roles = useAuthStore.getState().roles;
        return roles.includes(role);
      },

      // 인증 여부 확인
      isAuthenticated: () => !!useAuthStore.getState().token,
    }),
    {
      name: 'auth-storage', // localStorage 키 이름
    }
  )
);
```

### 6-4. API 서비스 (`src/api/authApi.js`)

```javascript
import axios from 'axios';

const BASE_URL = 'http://localhost:8080';

// 로그인 — axiosInstance 미사용 (토큰 불필요 경로)
export const login = async (email, password) => {
  const response = await axios.post(`${BASE_URL}/userinfos/login`, {
    email,
    password,
  });
  return response.data; // JWT 토큰 문자열
};

// 회원 가입
export const register = async (name, email, password, roles = 'ROLE_USER') => {
  const response = await axios.post(`${BASE_URL}/userinfos/new`, {
    name,
    email,
    password,
    roles,
  });
  return response.data;
};
```

### 6-5. 직원 API 서비스 (`src/api/employeeApi.js`)

```javascript
import axiosInstance from './axiosInstance';

// ROLE_ADMIN 필요
export const getAllEmployees = () =>
  axiosInstance.get('/api/employees');

// ROLE_USER 필요
export const getEmployeeById = (id) =>
  axiosInstance.get(`/api/employees/${id}`);

// ROLE_USER 필요
export const getEmployeeByEmail = (email) =>
  axiosInstance.get(`/api/employees/email/${email}`);

// 인증 필요 (직원 + 부서 정보 포함)
export const getAllEmployeesWithDepartment = () =>
  axiosInstance.get('/api/employees/departments');

// 페이징
export const getEmployeesPage = (pageNo = 0, pageSize = 10, sortBy = 'id', sortDir = 'asc') =>
  axiosInstance.get('/api/employees/page', {
    params: { pageNo, pageSize, sortBy, sortDir },
  });

export const createEmployee = (employeeDto) =>
  axiosInstance.post('/api/employees', employeeDto);

export const updateEmployee = (id, employeeDto) =>
  axiosInstance.put(`/api/employees/${id}`, employeeDto);

export const deleteEmployee = (id) =>
  axiosInstance.delete(`/api/employees/${id}`);
```

### 6-6. 로그인 컴포넌트 (`src/pages/LoginPage.jsx`)

```jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import { jwtDecode } from 'jwt-decode';

export default function LoginPage() {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const navigate                = useNavigate();
  const loginStore              = useAuthStore((state) => state.login);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const token = await login(email, password);

      // 토큰에서 이메일(sub) 추출
      // roles는 토큰에 없으므로 별도 관리 필요 (아래 참고)
      const decoded = jwtDecode(token);

      loginStore(token, decoded.sub, []); // roles는 별도 처리
      navigate('/employees');
    } catch (err) {
      const status = err.response?.status;
      if (status === 401) {
        setError('이메일 또는 비밀번호가 올바르지 않습니다.');
      } else {
        setError('로그인 중 오류가 발생했습니다.');
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="이메일"
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="비밀번호"
      />
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <button type="submit">로그인</button>
    </form>
  );
}
```

> **jwt-decode 설치**: `npm install jwt-decode`

### 6-7. Protected Route (`src/components/ProtectedRoute.jsx`)

인증 여부 및 역할(role)로 접근을 제어합니다.

```jsx
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

// requiredRole: 'ROLE_ADMIN' | 'ROLE_USER' | undefined (인증만 필요)
export default function ProtectedRoute({ children, requiredRole }) {
  const token = useAuthStore((state) => state.token);
  const roles = useAuthStore((state) => state.roles);

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !roles.includes(requiredRole)) {
    return <Navigate to="/forbidden" replace />;
  }

  return children;
}
```

### 6-8. 라우터 설정 (`src/App.jsx`)

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage          from './pages/LoginPage';
import EmployeeListPage   from './pages/EmployeeListPage';
import ForbiddenPage      from './pages/ForbiddenPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forbidden" element={<ForbiddenPage />} />

        {/* 인증만 필요 */}
        <Route
          path="/employees/:id"
          element={
            <ProtectedRoute requiredRole="ROLE_USER">
              <EmployeeDetailPage />
            </ProtectedRoute>
          }
        />

        {/* ROLE_ADMIN 필요 */}
        <Route
          path="/employees"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMIN">
              <EmployeeListPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
```

### 6-9. 역할 기반 조건부 렌더링

```jsx
import { useAuthStore } from '../store/authStore';

export default function Navbar() {
  const roles  = useAuthStore((state) => state.roles);
  const email  = useAuthStore((state) => state.email);
  const logout = useAuthStore((state) => state.logout);

  const isAdmin = roles.includes('ROLE_ADMIN');

  return (
    <nav>
      <span>{email}</span>
      {isAdmin && <a href="/employees">직원 관리 (Admin)</a>}
      <button onClick={logout}>로그아웃</button>
    </nav>
  );
}
```

---

## 7. roles 처리 주의사항

**백엔드 토큰 Payload에는 `roles`가 포함되지 않습니다.**

클라이언트에서 역할 정보를 관리하는 방법 두 가지:

**방법 A — 로그인 후 별도 API 호출 (권장)**

현재 백엔드에 `/userinfos/me` 같은 현재 사용자 정보 API가 없으므로,
로그인 폼에서 role을 직접 입력받거나, 로그인 이후 사용자 정보를 조회하는
API를 백엔드에 추가하는 방법이 안전합니다.

**방법 B — 프론트에서 role 입력 후 저장**

로그인 폼에서 role을 선택하거나, 백엔드 응답을 확장하여 토큰과 함께
`{ token, roles }` 형태로 반환하도록 백엔드를 수정합니다.

> 어떤 방법을 사용하더라도, 실제 권한 검증은 **백엔드에서만 신뢰**해야 합니다.
> 프론트엔드의 roles는 UI 표시 목적으로만 사용합니다.

---

## 8. 전체 인증 흐름

```
[LoginPage]
  ① POST /userinfos/login { email, password }
        │
        ▼
  ② 응답: JWT 토큰 문자열
        │
        ▼
  ③ jwtDecode(token) → sub(email) 추출
        │
        ▼
  ④ useAuthStore.login(token, email, roles) → localStorage 저장
        │
        ▼
  ⑤ navigate('/employees')

[API 요청 시]
  axiosInstance.interceptors.request
    → localStorage에서 token 읽기
    → Authorization: Bearer <token> 헤더 자동 추가

[에러 처리]
  응답 401 → logout() + /login 이동
  응답 403 → /forbidden 이동

[페이지 새로고침]
  zustand persist → localStorage에서 token 자동 복원
  ProtectedRoute → token 없으면 /login 리다이렉트
```

---

## 9. 토큰 만료 처리

토큰은 발급 후 **3600초(60분)** 뒤 만료됩니다.

만료된 토큰으로 요청 시 백엔드는 아래를 반환합니다:
```json
{ "error": "Unauthorized", "message": "Invalid or expired JWT token" }
```

Axios 인터셉터가 `401`을 감지하면 자동으로 로그아웃 처리합니다.

```javascript
// axiosInstance.js — 401 처리
if (status === 401) {
  useAuthStore.getState().logout(); // localStorage 토큰 제거
  window.location.href = '/login';  // 로그인 페이지 이동
}
```

---

## 10. 에러 처리 헬퍼 (`src/utils/errorHandler.js`)

```javascript
export const parseErrorMessage = (error) => {
  const data = error.response?.data;
  if (!data) return '서버에 연결할 수 없습니다.';

  // 표준 ErrorObject: { statusCode, message }
  if (data.statusCode && data.message) return data.message;

  // 필터 레벨 에러: { error, message }
  if (data.error && data.message) return data.message;

  // 검증 에러: { errors: { field: message } }
  if (data.errors) {
    return Object.values(data.errors).join(', ');
  }

  return '알 수 없는 오류가 발생했습니다.';
};
```

**사용 예시**
```javascript
import { parseErrorMessage } from '../utils/errorHandler';

try {
  await createEmployee(employeeDto);
} catch (error) {
  const message = parseErrorMessage(error);
  setError(message);
}
```
