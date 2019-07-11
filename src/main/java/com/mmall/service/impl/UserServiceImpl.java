package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Alvin
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMsg("Username is not existed!");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);


        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMsg("Password is wrong!");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Login Success!", user);
    }

    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);

        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMsg("Register failed!");
        }

        return ServerResponse.createBySuccess("Register success!");
    }

    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMsg("Username is already existed!");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMsg("Email is already existed!");
                }
            }
        } else {
            return ServerResponse.createByErrorMsg("Wrong parameter!");
        }
        return ServerResponse.createBySuccess("Check Valid!");
    }

    public ServerResponse selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMsg("User is not existed!");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (!StringUtils.isNotBlank(question)) {
            return ServerResponse.createByErrorMsg("Question to find password is empty!");
        }
        return ServerResponse.createBySuccess(question);
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount>0){
            String forgetToken= UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMsg("Answer is wrong!");
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMsg("Parameter wrong, need to pass token!");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMsg("User is not existed!");
        }
        String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMsg("token is invalid or expired!");
        }
        if(StringUtils.equals(token,forgetToken)){
            String md5Password=MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount=userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount>0){
                return ServerResponse.createBySuccess("Reset password successful!");
            }
        }
        else{
            return ServerResponse.createByErrorMsg("Token is wrong!");
        }
        return ServerResponse.createByErrorMsg("Failed to reset password!");
    }

    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount==0){
            return ServerResponse.createByErrorMsg("Old password is wrong!");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updatedCount=userMapper.updateByPrimaryKeySelective(user);
        if(updatedCount>0){
            return ServerResponse.createBySuccess("Reset password successful!");
        }
        return ServerResponse.createByErrorMsg("Reset password failed!");
    }

    public ServerResponse<User> updateInformation(User user){
        int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return ServerResponse.createByErrorMsg("Email has already been existed, please change another one!");
        }
        User updatedUser=new User();
        updatedUser.setId(user.getId());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setPhone(user.getPhone());
        updatedUser.setQuestion(user.getQuestion());
        updatedUser.setAnswer(user.getAnswer());

        int updatedCount=userMapper.updateByPrimaryKeySelective(updatedUser);
        if(updatedCount>0){
            return ServerResponse.createBySuccess("Update user information successful!",updatedUser);
        }
        return ServerResponse.createByErrorMsg("Update user information failed!");
    }

    public ServerResponse<User> getInformation(Integer userId){
        User user=userMapper.selectByPrimaryKey(userId);
        if(user==null){
            return ServerResponse.createByErrorMsg("Cannot find current user!");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }
}
