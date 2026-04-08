package cn.example.flink.demo.job.init;

import cn.example.flink.demo.job.param.UserVisitorDto;
import java.util.ArrayList;
import java.util.List;

public class UserVisitorBuilder {

    public static List<UserVisitorDto> buildUserVisitorDto() {
        List<UserVisitorDto> list = new ArrayList<>();

        UserVisitorDto visitor1 = UserVisitorDto.builder().userId("1").url("/home").timestamp(1000L).build();

        UserVisitorDto visitor2 = UserVisitorDto.builder().userId("1").url("/api/product").timestamp(7000L).build();

        UserVisitorDto visitor3 = UserVisitorDto.builder().userId("2").url("/home").timestamp(1000L).build();

        UserVisitorDto visitor4 = UserVisitorDto.builder().userId("2").url("/api/cart").timestamp(6000L).build();

        UserVisitorDto visitor5 = UserVisitorDto.builder().userId("3").url("/home").timestamp(4000L).build();

        UserVisitorDto visitor6 = UserVisitorDto.builder().userId("3").url("/api/order").timestamp(3000L).build();

        list.add(visitor1);
        list.add(visitor2);
        list.add(visitor3);
        list.add(visitor4);
        list.add(visitor5);
        list.add(visitor6);
        return list;
    }

}
