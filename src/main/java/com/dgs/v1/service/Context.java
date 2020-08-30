package com.dgs.v1.service;

import com.dgs.v1.model.Delegate;
import com.dgs.v1.model.ExponentialSmoothingModel;
import com.dgs.v1.model.InventoryDelegate;
import com.dgs.v1.model.ProductDelegate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Context {

    public static final String TAIWAN = "taiwan";
    public static final String CHINA = "china";

    /*
             Model Payload
    */

    private final String[] contextColumn = new String[]{"服務水準(%)", "訂購頻率(次/年)", "前置期(月)", "修正參數>=1.2"};
    //alpha threshold to adjust tracking signal
    public Double alphaThreshold;
    //number of rows to init forecast and MAD
    public Integer initWindow;
    //adjust demand
    public Double demandCoefficientThreshold;
    //read from the chart to convert to service coefficient
    public Double serviceLevel;//times per year
    public Double orderFrequencyPerYear;
    public Double orderFrequencyInMonth;
    public Double leadTime;
    public Double beta;
    public Double phi;
    /*
        Product Payload
     */
    public String[] selectedProducts;
    public String fromProduct;
    public String toProduct;
    /*
        Supplier Payload
     */
    public String supplier;
    /*
        Category Payload
     */
    public String category;
    /*
        Config Payload
    */
    public String[] warehouseFilter;
    public boolean refreshCache;
    public String pid;
    public String company;
    /*
        Datasource Config
    */
    public long expirary;
    private boolean showCache;
    private boolean showQuery;
    private JedisPool jedisPool;
    private ComboPooledDataSource dbPool;
    /*
        Excel Config
    */
    private Semaphore mutex;
    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    private Integer row;
    private String[] columns;

    //TODO ADD CONNECTION HERE ONE THREAD ONE CONNECTION
    public Context() {
        this.alphaThreshold = 0.4;
        this.initWindow = 6;
        this.demandCoefficientThreshold = 1.5;
        this.serviceLevel = 94.0;
        this.orderFrequencyPerYear = 1.0;
        this.orderFrequencyInMonth = 12 / orderFrequencyPerYear;
        this.leadTime = 2.0;
        this.beta = 0.1;
        this.phi = 0.1;
        // Datasource Config
        this.expirary = 86400;
        this.warehouseFilter = new String[]{"A", "B", "C"};
        this.refreshCache = false;
        this.showCache = false;
        this.showQuery = false;
        this.company = "taiwan";
        // Excel Config
        this.mutex = new Semaphore(1);
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet("Sheet");
        this.row = 3;
        this.columns = new String[]{
                "貨品編號", "貨品名稱", "採購類型", "A倉存量", "B倉存量", "C倉存量", "目前存量", "建議訂購量", "已採未交(-)", "已訂未交(+)",
                "實際需購量", "預估月需求 ", "安全庫存 ", "檢視水準", "目標庫存水準", "建議需求量/月", "實際需購量(月)", "安全庫存 (月)",
                "調整後需購量", "調整後目標庫存水準", "調整後目標庫存水準(月)", "調整後需購量(月)", "調整前後差異", "新服務水準係數", "新服務水準 （％)"};
    }

    public void populateExcelTitle() {
        int co = 0;
        Row newRow = this.sheet.createRow(0);
        for (String col : this.contextColumn) {
            newRow.createCell(co++)
                    .setCellValue(col);
        }
        newRow = this.sheet.createRow(1);
        newRow.createCell(0).setCellValue(this.serviceLevel);
        newRow.createCell(1).setCellValue(this.orderFrequencyPerYear);
        newRow.createCell(2).setCellValue(this.leadTime);
        newRow.createCell(3).setCellValue(this.demandCoefficientThreshold);

        co = 0;
        newRow = this.sheet.createRow(2);
        for (String col : this.columns) {
            newRow.createCell(co++)
                    .setCellValue(col);
        }
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public boolean isShowCache() {
        return showCache;
    }

    public void setShowCache(boolean showCache) {
        this.showCache = showCache;
    }

    @JsonIgnore
    public String[] getContextColumn() {
        return contextColumn;
    }

    public long getExpirary() {
        return expirary;
    }

    public void setExpirary(long expirary) {
        this.expirary = expirary;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    @JsonIgnore
    public String[] getColumns() {
        return columns;
    }

    @JsonIgnore
    public Semaphore getMutex() {
        return mutex;
    }

    @JsonIgnore
    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }

    @JsonIgnore
    public XSSFSheet getSheet() {
        return sheet;
    }

    public void setSheet(XSSFSheet sheet) {
        this.sheet = sheet;
    }

    @JsonIgnore
    public XSSFWorkbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    @JsonIgnore
    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }


    /*
        Context Delegate Factory
     */

    @JsonIgnore
    public ExponentialSmoothingModel getExponentialSmoothingModel(Double[] demand, String[] datetime) {
        return new ExponentialSmoothingModel(this, demand, datetime);
    }

    @JsonIgnore
    public ProductDelegate getProductDelegate() {
        return new ProductDelegate(this);
    }

    @JsonIgnore
    public InventoryDelegate getInventoryDelegate() {
        return new InventoryDelegate(this);
    }



     /*
        Data source pool factory
     */

    @JsonIgnore
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    @JsonIgnore
    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @JsonIgnore
    public ComboPooledDataSource getDbPool() {
        return dbPool;
    }

    @JsonIgnore
    public void setDbPool(ComboPooledDataSource dbPool) {
        this.dbPool = dbPool;
    }

    /*
        Getter and Setting
    */

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isShowQuery() {
        return showQuery;
    }

    public void setShowQuery(boolean showQuery) {
        this.showQuery = showQuery;
    }

    public String getFromProduct() {
        return fromProduct;
    }

    public void setFromProduct(String fromProduct) {
        this.fromProduct = fromProduct;
    }

    public String getToProduct() {
        return toProduct;
    }

    public void setToProduct(String toProduct) {
        this.toProduct = toProduct;
    }

    public String[] getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(String[] selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public boolean isRefreshCache() {
        return refreshCache;
    }

    public void setRefreshCache(boolean refreshCache) {
        this.refreshCache = refreshCache;
    }

    public Double getAlphaThreshold() {
        return alphaThreshold;
    }

    public void setAlphaThreshold(Double alphaThreshold) {
        this.alphaThreshold = alphaThreshold;
    }

    public Integer getInitWindow() {
        return initWindow;
    }

    public void setInitWindow(Integer initWindow) {
        this.initWindow = initWindow;
    }

    public Double getDemandCoefficientThreshold() {
        return demandCoefficientThreshold;
    }

    public void setDemandCoefficientThreshold(Double demandCoefficientThreshold) {
        this.demandCoefficientThreshold = demandCoefficientThreshold;
    }

    public Double getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(Double serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public Double getOrderFrequencyPerYear() {
        return orderFrequencyPerYear;
    }

    public void setOrderFrequencyPerYear(Double orderFrequencyPerYear) {
        this.orderFrequencyPerYear = orderFrequencyPerYear;
    }

    public Double getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(Double leadTime) {
        this.leadTime = leadTime;
    }

    public Double getBeta() {
        return beta;
    }

    public void setBeta(Double beta) {
        this.beta = beta;
    }

    public Double getPhi() {
        return phi;
    }

    public void setPhi(Double phi) {
        this.phi = phi;
    }

    public String[] getWarehouseFilter() {
        return warehouseFilter;
    }

    public void setWarehouseFilter(String[] warehouseFilter) {
        this.warehouseFilter = warehouseFilter;
    }

    public Double getOrderFrequencyInMonth() {
        return orderFrequencyInMonth;
    }

    public void setOrderFrequencyInMonth(Double orderFrequencyInMonth) {
        this.orderFrequencyInMonth = orderFrequencyInMonth;
    }

    @Override
    public String toString() {
        return "Context{" +
                "alphaThreshold=" + alphaThreshold +
                ", initWindow=" + initWindow +
                ", demandCoefficientThreshold=" + demandCoefficientThreshold +
                ", serviceLevel=" + serviceLevel +
                ", orderFrequency=" + orderFrequencyPerYear +
                ", leadTime=" + leadTime +
                ", beta=" + beta +
                ", phi=" + phi +
                ", warehouse=" + Arrays.toString(warehouseFilter) +
                '}';
    }


    public Delegate getDelegate(Object t) {
        System.out.println(t);
        System.out.println(t.getClass());
        return null;
    }
}