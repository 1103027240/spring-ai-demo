package cn.example.flink.demo.build;

import cn.example.flink.demo.param.UserVisitorDto;
import java.util.ArrayList;
import java.util.List;

public class UserVisitorBuilder {

    public static List<UserVisitorDto> buildUserVisitorDto() {
        List<UserVisitorDto> list = new ArrayList<>();

        UserVisitorDto visitor1 = UserVisitorDto.builder().userId("1").url("/home").timestamp(1000L).build();
        UserVisitorDto visitor2 = UserVisitorDto.builder().userId("1").url("/api/product").timestamp(12000L).build();
        UserVisitorDto visitor3 = UserVisitorDto.builder().userId("1").url("/api/order").timestamp(7000L).build();
        UserVisitorDto visitor4 = UserVisitorDto.builder().userId("1").url("/api/cart").timestamp(21000L).build();
        UserVisitorDto visitor5 = UserVisitorDto.builder().userId("2").url("/home").timestamp(11000L).build();
        UserVisitorDto visitor6 = UserVisitorDto.builder().userId("2").url("/api/cart").timestamp(16000L).build();
        UserVisitorDto visitor7 = UserVisitorDto.builder().userId("2").url("/api/pay").timestamp(13000L).build();
        UserVisitorDto visitor8 = UserVisitorDto.builder().userId("2").url("/api/fulfillment").timestamp(22000L).build();
        UserVisitorDto visitor9 = UserVisitorDto.builder().userId("3").url("/home").timestamp(4000L).build();
        UserVisitorDto visitor10 = UserVisitorDto.builder().userId("3").url("/api/product").timestamp(15000L).build();
        UserVisitorDto visitor11 = UserVisitorDto.builder().userId("3").url("/api/order").timestamp(27000L).build();
        UserVisitorDto visitor12 = UserVisitorDto.builder().userId("3").url("/api/stock").timestamp(17000L).build();

        list.add(visitor1);
        list.add(visitor2);
        list.add(visitor3);
        list.add(visitor4);
        list.add(visitor5);
        list.add(visitor6);
        list.add(visitor7);
        list.add(visitor8);
        list.add(visitor9);
        list.add(visitor10);
        list.add(visitor11);
        list.add(visitor12);

        return list;
    }

}
