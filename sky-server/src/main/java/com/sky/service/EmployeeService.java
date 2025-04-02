package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService extends IService<Employee>
{

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /*
    * 员工分页查询
    * */
    PageResult pageQueryEmployee(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);
}
