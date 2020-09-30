package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName specgroupEntity
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/3
 * @Version V1.0
 **/
@Table(name = "tb_spec_group")
@Data
public class SpecgroupEntity {

    @Id
    private Integer id;

    private Integer cid;

    private String name;
}
