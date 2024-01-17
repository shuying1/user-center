package com.yupi.usercenter.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.usercenter.Exception.BusinessException;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";


    @Resource
    private UserMapper userMapper;


    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        // 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账户过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");

        }
        if (planetCode.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");

        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能包含特殊字符");

        }
        // 密码和校验密码是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不一致");

        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Integer count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");

        }

        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号重复");

        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"插入数据时出现异常");

        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        // todo 修改为自定义异常
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号长度小于4");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码小于8");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能包含特殊字符");
        }

        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User originUser = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (originUser == null) {
            log.error("user login failed, userAccount not match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        // 3.用户脱敏
        User safetyUser = getSafetyUser(originUser);

        // 4.记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"原始用户不存在");

        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setUserPassword(originUser.getUserPassword());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除用户登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;

    }

    /**
     * 根据标签搜索用户
     * @param tagNameList 用户的标签名
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList){
        long start=System.currentTimeMillis();
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper=queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);

//        for (User user : userList) {
//            getSafetyUser(user);
//        }
//        return userList;

        userList.forEach(user->{
            getSafetyUser(user);
        });
        log.info("memory query time"+(System.currentTimeMillis()-start));
        return userList;

//        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());


    }

    @Override
    public List<User> searchUsersByTags2(List<String> tagNameList) {
        long start=System.currentTimeMillis();
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().filter(user->{
            String tagStr= user.getTags();

            if (tagStr==null){
                return false;
            }
            // 使用Gson将json字符串转化为String
            Gson gson=new Gson();
            Set<String> tagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
            }.getType());
            tagNameSet=Optional.ofNullable(tagNameSet).orElse(new HashSet<>());

            for (String tagName : tagNameSet) {
                if(!tagNameSet.contains(tagName)){
                    return false;
                }
            }
            log.info("memery query time"+(System.currentTimeMillis()-start));
            return true;

        }).map(this::getSafetyUser).collect(Collectors.toList());



    }


}




