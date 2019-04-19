package com.geekhalo.like.controller;

import com.geekhalo.ddd.lite.spring.mvc.ResultVo;
import com.geekhalo.like.application.TargetCountApplication;
import com.geekhalo.like.domain.Target;
import com.geekhalo.like.domain.count.TargetCount;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("target-count")
public class TargetCountController {
    @Autowired
    private TargetCountApplication countApplication;

    @ResponseBody
    @PostMapping("/count-of-targets")
    public ResultVo<List<TargetCount>> countOfTarget(@RequestBody Targets targets){
        return ResultVo.success(this.countApplication.countOfTargets(targets.getValues()));
    }

    @Data
    static class Targets{
        private List<Target> values;
    }
}
