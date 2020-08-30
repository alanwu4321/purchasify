package com.dgs.v1.controller;

import com.dgs.v1.AsyncConfiguration;
import com.dgs.v1.DbPoolConfiguration;
import com.dgs.v1.JedisPoolConfiguration;
import com.dgs.v1.model.Category;
import com.dgs.v1.model.Product;
import com.dgs.v1.model.ResponseBody;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

@CrossOrigin(origins = {"http://dgs.alan-wu.com", "http://localhost:8000"}, allowedHeaders = "*")
@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForecastController.class);
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

    @Autowired
    private AsyncConfiguration asyncConfiguration;

    private void setPools(Context ctx) {
        ctx.setJedisPool(jc.getPool());
        ctx.setDbPool(dc.getPool(ctx));
    }

    @RequestMapping(path = "/product/thread", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public CompletableFuture<Object> getProductBySelectedThread(
            @RequestBody Context ctx,
            @RequestParam("limit") Integer limit
    ) {
        setPools(ctx);

        LOGGER.info(String.format("Fetching %s Products...", limit));
        List<Product> products = new ArrayList<>();
        if (ctx.getSelectedProducts() != null) {
            products = productService.getBySelected(ctx, ctx.getSelectedProducts(), limit);
        } else if (ctx.getToProduct() != null && ctx.getFromProduct() != null) {
            products = productService.getByRange(ctx, ctx.getFromProduct(), ctx.getToProduct(), limit);
        } else if (ctx.getSupplier() != null) {
            products = productService.getByColumn(ctx, "scustno", ctx.getSupplier(), limit);
        } else if (ctx.getCategory() != null) {
            products = productService.getByColumn(ctx, "sclassno", ctx.getCategory(), limit);
        }
        LOGGER.info(String.format("Fetched %s Products ", products.size()));

        ThreadPoolTaskExecutor executorTask;
        if(asyncConfiguration.isShutdown())
            asyncConfiguration.createPool(20);
        executorTask = asyncConfiguration.getThreadPoolTaskExecutor();

        // Queue task into Queue
        List<CompletableFuture<ResponseBody>> pageContentFutures = new ArrayList<>();
        for (Product p : products) {
            CompletableFuture<ResponseBody> allCars = responseBodyService.getResponseBodyForProductThread(ctx, p, executorTask, false);
            pageContentFutures.add(allCars);
        }

        CompletableFuture[] hi = pageContentFutures.toArray(new CompletableFuture[pageContentFutures.size()]);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(hi);
        /*
            thenApply would register a callback to allFutures when all future are done
            When all the Futures are completed, call `future.join()` to get their results and collect the results in a list
        */
        List<Product> finalProducts = products;
        CompletableFuture<Object> allPageContentsFuture = allFutures.thenApply(v -> {
            List<ResponseBody> list = new ArrayList<>();
            Integer completedTaskCount = 0;
            for (CompletableFuture<ResponseBody> pageContentFuture : pageContentFutures) {
                try {
                    ResponseBody join = pageContentFuture.join();
                    if(join != null) {
                        list.add(join);
                        completedTaskCount++;
                    }
                }catch (Exception e){
                    LOGGER.error("Failed to join" + e.getMessage());
                }
            }
            LOGGER.info("Task Completed: " + completedTaskCount + " / " + finalProducts.size());
            if(completedTaskCount <  finalProducts.size()) {
                LOGGER.error("Task Completed is lower than the Task Requested! Shutting down Pool..." );
                //shut down thread pool to force interrupt thread that is blocking
                asyncConfiguration.shutdown();
            }
            return list;
        });

        return allPageContentsFuture;
    }



    @RequestMapping(path = "/excel", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, Object> getExcelThread(
            @RequestBody Context ctx,
            @RequestParam("limit") Integer limit) {
        setPools(ctx);
        List<Product> products = new ArrayList<>();
        //TODO switch on different ways to get products
        if (ctx.getSelectedProducts() != null) {
            products = productService.getBySelected(ctx, ctx.getSelectedProducts(), limit);
        } else if (ctx.getToProduct() != null && ctx.getFromProduct() != null) {
            products = productService.getByRange(ctx, ctx.getFromProduct(), ctx.getToProduct(), limit);
        } else if (ctx.getSupplier() != null) {
            products = productService.getByColumn(ctx, "scustno", ctx.getSupplier(), limit);
        } else if (ctx.getCategory() != null) {
            products = productService.getByColumn(ctx, "sclassno", ctx.getCategory(), limit);
        }

        //populate Excel with config value ex. lead time, order frequency
        ctx.populateExcelTitle();

        LOGGER.info(String.format("Fetched %s products", products.size()));

        ThreadPoolTaskExecutor executorTask;
        if(asyncConfiguration.isShutdown())
            asyncConfiguration.createPool(30);
        executorTask = asyncConfiguration.getThreadPoolTaskExecutor();

        // Download contents of all the web pages asynchronously
        List<CompletableFuture<ResponseBody>> pageContentFutures = new ArrayList<>();
        for (Product p : products) {
            CompletableFuture<ResponseBody> allCars = responseBodyService.getResponseBodyForProductThread(ctx, p, executorTask,  true);
            pageContentFutures.add(allCars);
        }

        CompletableFuture[] hi = pageContentFutures.toArray(new CompletableFuture[pageContentFutures.size()]);

        //Returns a new CompletableFuture that is completed when all the given CompletableFutures complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(hi);
        String salted = "purchase";
        //TODO Have user input file name
        //String salted = getSaltString();
        /*
            thenApply would register a callback to allFutures when all future are done
            When all the Futures are completed, call `future.join()` to get their results and collect the results in a list
         */
        List<Product> finalProducts = products;
        CompletableFuture<Object> allPageContentsFuture = allFutures.thenApply(v -> {
            List<ResponseBody> list = new ArrayList<>();
            Integer completedTaskCount = 0;
            for (CompletableFuture<ResponseBody> pageContentFuture : pageContentFutures) {
                try {
                    ResponseBody join = pageContentFuture.join();
                    if(join != null) {
                        list.add(join);
                        completedTaskCount++;
                    }
                }catch (Exception e){
                    LOGGER.error("Failed to join" + e.getMessage());
                }
            }
            LOGGER.info("Task Completed: " + completedTaskCount + " / " + finalProducts.size());
            if(completedTaskCount <  finalProducts.size()) {
                LOGGER.error("Task Completed is lower than the Task Requested! Shutting down Pool..." );
                //shut down thread pool to force interrupt thread that is blocking
                asyncConfiguration.shutdown();
            }
            LOGGER.info("Creating Excel Output");
            try {
//                String path = System.getProperty("user.dir") + "/src/main/resources/asset/" + salted + ".xlsx";
                String path = System.getProperty("user.dir") + "\\asset\\" + salted + ".xlsx";
                LOGGER.info("Writing to " + path);
//                new File(path).createNewFile();
                FileOutputStream outputStream = new FileOutputStream(path);
                ctx.getWorkbook().write(outputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        });

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("fileName", salted + ".xlsx");
        try {
            // Block and wait for the future to complete
            result.put("data", allPageContentsFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            //empty fall back value
            result.put("data", new String[]{});
        }
        LOGGER.info("[ Pool Status ]" + asyncConfiguration.getThreadPoolTaskExecutor().getThreadPoolExecutor().toString());
        return result;
    }

    @RequestMapping(path = "/excel/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> getExcelDownload(
            HttpServletRequest request,
            @RequestParam("salted") String salted
    ) throws MalformedURLException {
//        String path = System.getProperty("user.dir") + "/src/main/resources/asset/";
        String path = System.getProperty("user.dir") + "\\asset\\";
        Path _path = Paths.get(path).toAbsolutePath().normalize();
        Path filePath = _path.resolve(salted).normalize();
        UrlResource resource = new UrlResource(filePath.toUri());

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    protected String getSaltString() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwsyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }


    @RequestMapping(path = "/product", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ResponseBody> getProductBySelected(
            @RequestBody Context ctx,
            @RequestParam("limit") Integer limit) {
        setPools(ctx);
        List<ResponseBody> responseBody = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        //TODO switch on different ways to get products
        LOGGER.info("Getting products");
        LOGGER.info(ctx.getToProduct());
        if (ctx.getSelectedProducts() != null) {
            products = productService.getBySelected(ctx, ctx.getSelectedProducts(), limit);
        }
        if (ctx.getToProduct() != null && ctx.getFromProduct() != null) {
            products = productService.getByRange(ctx, ctx.getFromProduct(), ctx.getToProduct(), limit);
        }
        LOGGER.info("Fetched products");
        products.forEach(p -> {
            responseBody.add(responseBodyService.getResponseBodyForProduct(ctx, p));
        });
        return responseBody;
    }


    @RequestMapping(path = "/supplier", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Supplier> getSupplierByPrefix(
            @RequestBody Context ctx,
            @RequestParam("prefix") String prefix,
            @RequestParam("limit") Integer limit) {
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
            @RequestParam("limit") Integer limit) {
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
        List<HashMap<String, Object>> responseBody = null;
        responseBody = responseBodyService.getResponseBodyForAllProducts(ctx);
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

    @RequestMapping("/error")
    public void Error() {
        LOGGER.error("Error Log Trigger");
    }

    @RequestMapping("/health")
    public HashMap<String, Object> getHealth() {
        StatsFilter.healthCheck(jc, dc);
        HashMap<String, Object> res = new HashMap<>();
        res.put("Redis Active ", jc.getPool().getNumActive());
        res.put("Redis Idle " , jc.getPool().getNumIdle());
        res.put("Redis Waiting " , jc.getPool().getNumWaiters());
        try {
            res.put("DB Connection ", dc.getPool().getNumConnections());
            res.put("DB Idle Conn ", dc.getPool().getNumIdleConnections());
            res.put("DB Thread Pool Size ", dc.getPool().getThreadPoolSize());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        res.put("Thread Pool Queue " , asyncConfiguration.getThreadPoolTaskExecutor().getThreadPoolExecutor().toString());
        return res;
    }

    @PostConstruct
    public void initialize() {
        this.val = 0;
    }

    @PreDestroy
    public void destroy() {
        LOGGER.warn("destroying callback closing pools=> " + val);
        jc.getPool().close();
        dc.getPool().close();
        LOGGER.warn("Pools closed=> " + val);
//        healthCheck(LOGGER, jc, dc);
    }

}
