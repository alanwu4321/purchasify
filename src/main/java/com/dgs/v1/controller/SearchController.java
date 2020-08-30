package com.dgs.v1.controller;


import com.dgs.v1.DbPoolConfiguration;
import com.dgs.v1.JedisPoolConfiguration;
import com.dgs.v1.model.Category;
import com.dgs.v1.model.Product;
import com.dgs.v1.model.Supplier;
import com.dgs.v1.persist;
import com.dgs.v1.service.*;
import com.dgs.v1.util.ExcelReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = {"http://dgs.alan-wu.com", "http://localhost:8000"}, allowedHeaders = "*")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    ExcelReader er;
    private Integer val;
    @Autowired
    private DbPoolConfiguration dc;

    @Autowired
    private JedisPoolConfiguration jc;

    @Autowired
    private persist p;

    @Autowired
    private ResponseBodyService responseBodyService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;


    @Autowired
    private CategoryService categoryService;

    private void setPools(Context ctx) {
        ctx.setJedisPool(jc.getPool());
        ctx.setDbPool(dc.getPool(ctx));
    }

    @RequestMapping(path = "/product", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Product> getProductByPrefix(
            @RequestBody Context ctx,
            @RequestParam("prefix") String prefix,
            @RequestParam("limit") Integer limit)
    {
        setPools(ctx);
        List<Product> responseBody = null;
        try {
            responseBody = productService.getByPrefix(ctx, prefix, limit);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return responseBody;
    }

    @RequestMapping(path = "/supplier", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public  List<Supplier> getSupplierByPrefix(
            @RequestBody Context ctx,
            @RequestParam("prefix") String prefix,
            @RequestParam("limit") Integer limit)
    {
        setPools(ctx);
        List<Supplier> responseBody = null;
        try {
            responseBody = supplierService.getByPrefix(ctx, prefix, limit);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return responseBody;
    }

    @RequestMapping(path = "/category", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Category> getCategoryByPrefix(
            @RequestBody Context ctx,
            @RequestParam("prefix") String prefix,
            @RequestParam("limit") Integer limit)
    {
        setPools(ctx);
        List<Category> responseBody = null;
        try {
            responseBody = categoryService.getByPrefix(ctx, prefix, limit);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return responseBody;
    }


    @RequestMapping(path = "/all", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<HashMap<String, Object>> postControllerAll(@RequestBody Context ctx) {
        setPools(ctx);
        List<HashMap<String, Object>> responseBody = responseBodyService.getResponseBodyForAllProducts(ctx);
        List<HashMap<String, Object>> sublist = responseBody.subList(100, 500);  //inclusive index 0, exclusive index 3
        return sublist;
    }

    //TODO create another interface for context body
    @RequestMapping("/readExcel")
    public List<HashMap<String, String>> readExcelController() throws IOException, InvalidFormatException {
//        List<HashMap<String, String>> res = er.readExcel("fileLocation");
//        Product p1 = new Product("a", "a", "a", "a", "a");
//        Product[] res = new Product[]{p1};
        Jedis conn = jc.getConnection();
        Gson gson = new Gson();
        Set<String> set = conn.zrangeByScore("excel:index", 100, 105);
        List<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();
        set.forEach(
                products -> {
                    HashMap<String, String> product = gson.fromJson(products, new TypeToken<HashMap<String, String>>() {
                    }.getType());
                    res.add(product);
                }
        );
        conn.close();
        return res;

    }

    @PostMapping("/uploadExcelFile")
    public String uploadFile(Model model, MultipartFile file) throws IOException, InvalidFormatException {
        InputStream in = file.getInputStream();
        File currDir = new File(".");

        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "asset/" + file.getOriginalFilename();
        FileOutputStream f = new FileOutputStream(fileLocation);
        int ch = 0;
        while ((ch = in.read()) != -1) {
            f.write(ch);
        }
        f.flush();
        f.close();
        model.addAttribute("message", "File: " + file.getOriginalFilename()
                + " has been uploaded successfully!");

        System.out.println(model);
        System.out.println(fileLocation);
        er.readExcel(fileLocation);

        return "excel";
    }

    @RequestMapping("/getVal")
    public Integer getVal() {
        p.setConnection();
        return p.getConnection();
    }

    @RequestMapping("/health")
    public void getHealth() {
        StatsFilter.healthCheck(jc, dc);
    }

    @PostConstruct
    public void initialize() {
        this.val = 0;
    }

    @PreDestroy
    public void destroy() {
        LOGGER.warn("destroying callback => " + val);
        jc.getPool().close();
        dc.getPool().close();
//        healthCheck(LOGGER, jc, dc);
    }

}