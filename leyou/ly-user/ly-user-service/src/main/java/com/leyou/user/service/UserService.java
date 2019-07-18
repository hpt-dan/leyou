package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.Queue.SMS_VERIFY_CODE_QUEUE;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 验证用户名和手机号是否唯一
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUserData(String data, Integer type) {

        User user = new User();
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        int count = userMapper.selectCount(user);

        return count == 0;
    }


    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String KEY_PREFIX = "user:code:phone:";

    /**
     * 发送验证码
     * @param phone
     */
    public void sendCode(String phone) {


        // 生成6位验证码
        String code = RandomStringUtils.randomNumeric(6);
        //封装成map
        Map<String, String> map = new HashMap<>();
        //发送到消息队列
        map.put("phone", phone);
        map.put("code", code);
        //从redis中存入验证码
        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME, VERIFY_CODE_KEY, map);

        //发送成功保存到redis中
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 10, TimeUnit.MINUTES);
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    /**
     * 用户注册
     * @param user
     * @param code
     */
    public void register(User user, String code) {

        //1：验证验证码是否正确
        String key = KEY_PREFIX + user.getPhone();
        String cacheCode = redisTemplate.opsForValue().get(key);
        if(!StringUtils.equals(code,cacheCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //2：对密码进行加密。
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //3：将user存到数据库中
        int count = userMapper.insertSelective(user);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }


    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    public UserDTO queryUserByUsernameAndPassword(String username, String password) {
        // 1根据用户名查询
        User u = new User();
        u.setUsername(username);
        User user = userMapper.selectOne(u);
        // 2判断是否存在
        if (user == null) {
            // 用户名错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        // 3校验密码
        if(!passwordEncoder.matches(password, user.getPassword())){
            // 密码错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return BeanHelper.copyProperties(user, UserDTO.class);
    }
}