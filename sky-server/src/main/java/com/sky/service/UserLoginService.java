package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

public interface UserLoginService extends IService<User> {
    UserLoginVO wxLogin(UserLoginDTO userLoginDTO);
}
