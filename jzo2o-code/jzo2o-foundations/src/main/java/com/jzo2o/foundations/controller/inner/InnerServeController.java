package com.jzo2o.foundations.controller.inner;

import com.jzo2o.api.foundations.ServeApi;
import com.jzo2o.api.foundations.dto.response.ServeAggregationResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 内部接口 - 服务相关接口（根据服务Id查询详情，供订单等模块调用）
 */
@RestController
@RequestMapping("/inner/serve")
@Api(tags = "内部接口 - 服务相关接口")
public class InnerServeController implements ServeApi {

    @Resource
    private IServeService serveService;

    @Override
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询服务详情")
    public ServeAggregationResDTO findById(@PathVariable("id") Long id) {
        return serveService.findServeDetailById(id);
    }
}
