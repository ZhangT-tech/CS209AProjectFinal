package com.example.springproject.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "contributor")
public class Contributor implements Serializable {

    @TableId(value = "id")
    private Long id;

    @TableField(value = "login")
    private String login;

    @TableField(value = "contributions")
    private Long contributions;

    @TableField(value = "avatar_url")
    private String avatar_url;

    @TableField(value = "html_url")
    private String html_url;

    public Contributor(Long id, String login, Long contributions, String avatar_url, String html_url) {
        this.id = id;
        this.login = login;
        this.contributions = contributions;
        this.avatar_url = avatar_url;
        this.html_url = html_url;
    }
}
