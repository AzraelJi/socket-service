package com.yang.controller;

import com.yang.service.WebSocket;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Api(value = "socket rest", tags = { "websocket controller" })
@RestController
@RequestMapping("/sendMsg")
public class SendMsgController {

    @Resource
    private WebSocket webSocket;

    @ApiOperation(value = "单点发送msg", httpMethod = "POST")
    @PostMapping("/sendOne")
    @ResponseBody
    public void sendOne(@ApiParam(name = "uid", value = "user id", required = true) @NotNull String uid,
            @ApiParam(name = "message", value = "msg content", required = true) String message) {
        webSocket.sendOneMessage(uid, message);
    }

    @ApiOperation(value = "发送多人msg", httpMethod = "POST")
    @PostMapping("/sendMoreMessage")
    @ResponseBody
    public void sendMore(@ApiParam(name = "userIds", value = "users array", required = true) @NotNull String users,
            @ApiParam(name = "message", value = "message content", required = true) String message) {
        String[] split = users.split(",");
        webSocket.sendMoreMessage(split, message);

    }
}
