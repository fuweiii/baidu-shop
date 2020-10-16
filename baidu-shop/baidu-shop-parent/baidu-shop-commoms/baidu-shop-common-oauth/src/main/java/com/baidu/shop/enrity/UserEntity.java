package com.baidu.shop.enrity;
import com.baidu.shop.utils.ValidateGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
/**
 * @ClassName UserEntity
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Table(name = "tb_user")
@Data
@ApiModel(value = "用户实体类")
public class UserEntity {

    @Id
    @ApiModelProperty(value = "用户主键",example = "1")
    @NotNull(message = "主键不能为空",groups = {ValidateGroup.Update.class})
    private Integer id;

    @ApiModelProperty(value = "用户名")
    @NotBlank(message = "用户名不能为空",groups = {ValidateGroup.Add.class})
    private String username;

    @ApiModelProperty(value = "密码")
    @NotBlank(message = "密码不能为空",groups = {ValidateGroup.Update.class})
    private String password;

    private String phone;

    @ApiModelProperty(hidden = true)
    private Date created;
    @ApiModelProperty(hidden = true)
    private String salt;
}
