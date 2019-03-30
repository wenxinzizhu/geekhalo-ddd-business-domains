package com.geekhalo.dataimport.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;


@Data   // 自动生成getter setter equals hashCode等方法
@NoArgsConstructor // 生成无参构造函数
@AllArgsConstructor // 生成包含所有参数的构造函数
@Builder // 创建builder模式

@Entity // 标记为实体
@Table(name = "tb_user") // 标准映射的表名
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long uid;
    private String name;
    private Date birthAt;
}
