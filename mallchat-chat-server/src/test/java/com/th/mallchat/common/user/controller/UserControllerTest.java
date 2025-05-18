package com.th.mallchat.common.user.controller;


import com.th.mallchat.common.common.thread.MyUncaughtExceptionHandler;
import com.th.mallchat.common.common.utils.JwtUtils;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.mapper.UserMapper;
import com.th.mallchat.common.user.service.LoginService;
import com.th.mallchat.common.user.service.UserService;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void login() {
        String token = jwtUtils.createToken(1L);
        String token2 = jwtUtils.createToken(1L);
        System.out.println(token);
        System.out.println(token2);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Test
    public void redis() {
        redisTemplate.opsForValue().set("name","卷心菜");
        stringRedisTemplate.opsForValue().set("sex","男");
        String name = (String) redisTemplate.opsForValue().get("name");
        String sex = stringRedisTemplate.opsForValue().get("sex");
        Pepole pepole = new Pepole();
        pepole.setName("张三");
        pepole.setId(321321321L);
        pepole.setPassword("dsafsadsad");
        redisTemplate.opsForValue().set("p1",pepole);
        Pepole o = (Pepole)redisTemplate.opsForValue().get("p1");
        System.out.println(o);
        System.out.println(name); //卷心菜
        System.out.println(sex);
    }


    @Test
    public void ThreadPool() {

        threadPoolTaskExecutor.execute(()->{
            if (1==1){
                throw new RuntimeException("错误");
            }
        });
    }

    @Test
    public void ThreadPoolUncaughtEWxception() throws InterruptedException {
        Thread thread = new Thread(()->{
            if (1==1){
                throw new RuntimeException("错误");
            }
        });
        thread.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        thread.start();
        thread.sleep(1000);
    }




}

@Data
class Pepole{
    private String name;
    private Long id;
    private String password;
}