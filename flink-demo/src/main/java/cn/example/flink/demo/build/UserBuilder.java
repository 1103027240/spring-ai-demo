package cn.example.flink.demo.build;

import cn.example.flink.demo.param.UserDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UserBuilder {

    public static List<UserDto> buildUserDto() {
        List<UserDto> list = new ArrayList<>();

        UserDto user1 = UserDto.builder().userId("1").userName("张三").phone("13800138001")
                .age(20).sex(1).email("zhangsan@qq.com").salary(new BigDecimal(10000)).build();

        UserDto user2 = UserDto.builder().userId("2").userName("李四").phone("13800138002")
                .age(22).sex(1).email("lisi@qq.com").salary(new BigDecimal(12000)).build();

        UserDto user3 = UserDto.builder().userId("3").userName("王五").phone("13800138003")
                .age(25).sex(1).email("wangwu@qq.com").salary(new BigDecimal(19000)).build();

        UserDto user4 = UserDto.builder().userId("4").userName("赵六").phone("13800138004")
                .age(35).sex(1).email("zhaoliu@qq.com").salary(new BigDecimal(18000)).build();

        UserDto user5 = UserDto.builder().userId("5").userName("钱七").phone("13800138005")
                .age(30).sex(1).email("qianqi@qq.com").salary(new BigDecimal(15000)).build();

        UserDto user6 = UserDto.builder().userId("6").userName("小明").phone("13800138006")
                .age(32).sex(1).email("xiaoming@qq.com").salary(new BigDecimal(16000)).build();

        UserDto user7 = UserDto.builder().userId("7").userName("Mary").phone("13800138007")
                .age(26).sex(2).email("mary@qq.com").salary(new BigDecimal(17000)).build();

        UserDto user8 = UserDto.builder().userId("8").userName("Lucy").phone("13800138008")
                .age(23).sex(2).email("lucy@qq.com").salary(new BigDecimal(14000)).build();

        UserDto user9 = UserDto.builder().userId("9").userName("Anty").phone("13800138009")
                .age(34).sex(2).email("anty@qq.com").salary(new BigDecimal(13000)).build();

        UserDto user10 = UserDto.builder().userId("10").userName("Tom").phone("13800138010")
                .age(29).sex(1).email("tom@qq.com").salary(new BigDecimal(20000)).build();

        UserDto user11 = UserDto.builder().userId("11").userName("Lili").phone("13800138010")
                .age(21).sex(1).email("lili@qq.com").salary(new BigDecimal(21000)).build();

        UserDto user12 = UserDto.builder().userId("12").userName("小强").phone("13800138010")
                .age(27).sex(1).email("xiaoqiang@qq.com").salary(new BigDecimal(16000)).build();

        list.add(user1);
        list.add(user2);
        list.add(user3);
        list.add(user4);
        list.add(user5);
        list.add(user6);
        list.add(user7);
        list.add(user8);
        list.add(user9);
        list.add(user10);
        list.add(user11);
        list.add(user12);

        return list;
    }

}
