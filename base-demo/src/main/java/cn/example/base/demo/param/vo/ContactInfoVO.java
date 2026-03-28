package cn.example.base.demo.param.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class ContactInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private String email;

    private String phone;

    private String company;

}
