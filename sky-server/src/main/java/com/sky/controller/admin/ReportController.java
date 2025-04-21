package com.sky.controller.admin;

import cn.hutool.core.collection.ListUtil;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Tag(name = "数据统计相关接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @Operation(summary = "营业额统计接口")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }
}
