package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String index(Model model) {
        List<CategoryEntity> categoryEntityList = indexService.queryCategoriesByPid();

        model.addAttribute("categories", categoryEntityList);

        return "index";
    }
}
