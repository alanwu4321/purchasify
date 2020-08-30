package com.dgs.v1.util;

import com.dgs.v1.JedisPoolConfiguration;
import com.google.gson.Gson;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


@Component
public class ExcelReader {

    //TODO EXCEL HAS TO BE SORTED
    public static final String SAMPLE_XLSX_FILE_PATH = "./sample-xlsx-file.xlsx";
    @Autowired
    private JedisPoolConfiguration jc;

    public ArrayList<HashMap<String, String>> readExcel(String fileLocation) throws IOException, InvalidFormatException {

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File("/Users/alwu/Desktop/new coding stuff/v1/asset/stock.xls"));

        // Retrieving the number of sheets in the Workbook
        System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");


        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        System.out.println("Retrieving Sheets using Iterator");
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            System.out.println("=> " + sheet.getSheetName());
        }


        Sheet sheet = workbook.getSheetAt(0);

        /*TODO
            - Key: Brand / Value: JSON List<Map>
            - Key Supplier / Value: JSON List<Map>
            - Key Category / Value: JSON List<Map>
            - Key A-Z / Value: JSON List<Map>
            ======
            LookUp:
            - SortedSets => Score => index / Value: JSON <Map>
         */

        HashMap<String, ArrayList<HashMap<String, String>>> alphabetMap = new HashMap<String, ArrayList<HashMap<String, String>>>();
        HashMap<String, ArrayList<HashMap<String, String>>> brandMap = new HashMap<String, ArrayList<HashMap<String, String>>>();
        HashMap<String, ArrayList<HashMap<String, String>>> supplierMap = new HashMap<String, ArrayList<HashMap<String, String>>>();
        HashMap<String, ArrayList<HashMap<String, String>>> classMap = new HashMap<String, ArrayList<HashMap<String, String>>>();

        Iterator<Row> rowI = sheet.rowIterator();
        ArrayList<HashMap<String, String>> allProducts = new ArrayList<>();

        ArrayList<String> columns = new ArrayList<>();
        rowI.next().forEach(
                cell -> columns.add(cell.getStringCellValue())
        );

        Integer productIndex = 0;
        while (rowI.hasNext()) {
            final Row row = rowI.next();
            Iterator<Cell> cellI = row.cellIterator();
            HashMap<String, String> product = new HashMap<String, String>();
            product.put("index", String.valueOf(productIndex));
            columns.forEach(
                    col -> {
                        if (cellI.hasNext())
                            product.put(col, String.valueOf(cellI.next()));
                    }
            );

            //first letter of stock number
            String alphabetMapKey = Character.toString(product.get("sstkno").toLowerCase().charAt(0));
            addItemToMap(alphabetMap, alphabetMapKey, product);

            //supplier number
            String supplierMapKey = product.get("scustno");
            addItemToMap(supplierMap, supplierMapKey, product);

            //brand name
            String brandMapKey = product.get("sstkuse");
            addItemToMap(brandMap, brandMapKey, product);

            //class number
            String classMapKey = product.get("sclassno");
            addItemToMap(classMap, classMapKey, product);

            //store to all products
            allProducts.add(product);

            productIndex++;
        }

        Jedis jedis = jc.getConnection();
        try {
            indexAllProducts("index", allProducts, jedis);
            storeItemFromMap("alpha", alphabetMap, jedis);
            storeItemFromMap("supplier", supplierMap, jedis);
            storeItemFromMap("brand", brandMap, jedis);
            storeItemFromMap("class", classMap, jedis);
        } catch (Exception e) {
            jedis.close();
        }

        return allProducts;
    }

    private void indexAllProducts(String prefix, ArrayList<HashMap<String, String>> allProducts, Jedis jedis) {
        Gson gson = new Gson();
        allProducts.forEach(v -> {
            jedis.zadd("excel" + prefix, Double.parseDouble(v.get("index")), gson.toJson(v));
        });
    }

    private void storeItemFromMap(String prefix, HashMap<String, ArrayList<HashMap<String, String>>> map, Jedis jedis) {
        Gson gson = new Gson();
        map.forEach((k, v) -> {
            jedis.set("excel:" + prefix + k, gson.toJson(v));
        });
    }

    private void addItemToMap(HashMap<String, ArrayList<HashMap<String, String>>> map, String key, HashMap<String, String> product) {
        if (map.containsKey(key)) {
            map.get(key).add(product);
            return;
        }
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        list.add(product);
        map.put(key, list);
    }
}