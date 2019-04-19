package com.geekhalo.like.controller;

import com.geekhalo.ddd.lite.spring.mvc.ResultVo;
import com.geekhalo.like.application.LikeApplication;
import com.geekhalo.like.application.OwnerAndTarget;
import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;
import com.geekhalo.like.domain.like.Like;
import com.geekhalo.like.queue.RedisBasedQueue;
import io.swagger.annotations.Api;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("LikeApi")
@RestController
@RequestMapping("like")
public class LikeController {
    @Autowired
    private LikeApplication likeApplication;

    @Autowired
    private RedisBasedQueue redisBasedQueue;

    @ResponseBody
    @PostMapping("click")
    public ResultVo<Void> click(@RequestBody OwnerAndTarget ownerAndTarget){
        this.redisBasedQueue.pushClickCommand(ownerAndTarget);
        return ResultVo.success();
    }

    @ResponseBody
    @PostMapping("get-by-targets")
    public ResultVo<List<Like>> getByTargets(@RequestBody OwnerAndTargets ownerAndTargets){
        return ResultVo.success(this.likeApplication.getByOwnerAndTargets(ownerAndTargets.getOwner(), ownerAndTargets.getTargets()));
    }


    @Data
    public static class OwnerAndTargets{
        private Owner owner;
        private List<Target> targets;
    }
}
