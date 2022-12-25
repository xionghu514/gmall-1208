package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/16 12:59
 * @Email: 1796235969@qq.com
 */
@Controller
public class IndexController {
    @Autowired
    private IndexService indexService;


    @GetMapping("/**")
    public String index(Model model, @RequestHeader(value = "userId", required = false) Long userId) {
        System.out.println("userId = " + userId);
        List<CategoryEntity> categoryEntityList = indexService.queryCategoriesByPid();

        model.addAttribute("categories", categoryEntityList);

        return "index";
    }

    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl23CategoriesByPid(@PathVariable("pid") Long pid) {
        List<CategoryEntity> categoryEntities = indexService.queryLvl23CategoriesByPid(pid);

        return ResponseVo.ok(categoryEntities);
    }
}
