package com.dgs.v1.service;

import com.dgs.v1.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

//TODO Business logic

@Service
public class ForecastService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForecastService.class);

    protected ForecastService() {
        //TODO FIX IT
        super(Product.class);
    }

    public ExponentialSmoothingModel getMonthlyDemandForecastByProduct(Context ctx, Product product) {
        Delegate<ProductDemand> productDemandDelegate = new ProductDemandDelegate(ctx);
        List<ProductDemand> productDemands = productDemandDelegate.getById(product.getSstkno());
        //extract demand from last 12 months (excluding current month) to feed to the model
        Double[] demands = new Double[12];
        String[] dates = new String[12];

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        //pd will be at most 11 which is the max size of productDemands => 12
        int pd = 0;
        Boolean emptyDemands = productDemands.size() == 0;
        for (int i = 1; i < 13; i++) {
            String curDate = date.minusMonths(13 - i).format(formatter);
            //default value for the demand
            Double curDemand = 0.0;
            //check pd only if the demands are empty
            if (!emptyDemands && productDemands.get(pd).getDate().equals(curDate)) {
                curDemand = productDemands.get(pd).getMonthlydemand();
                //move on to the next item in productDemands only if the current demands are used and size is smaller
                //check index out of range before incrementing
                if (pd < productDemands.size() - 1) pd++;
            }
            //default negative number to zero
            demands[i - 1] = Math.max(curDemand, 0);
            dates[i - 1] = curDate;
        }

        ExponentialSmoothingModel model = new ExponentialSmoothingModel(ctx, demands, dates);
        model.fit1();
        //adjust demand based on the forecast from 1st run
        model.adjust();
        // use adjust demand to fit the model again
        model.fit();
        return model;
    }

    //TODO CONTEXT PASSED FROM CONTROLLER VENDOR FOR FORECAST CALCULATION used by response body
    public ExponentialSmoothingModel getMonthlyDemandForecastByPid(Context ctx, String pid) {
//        final long start = System.currentTimeMillis();
//        LOGGER.info("Request to get a list of cars");
//
//
//        //TODO REMOVE THIS MAP BULLSHIT USE STRUCT
//        List<HashMap<String, Object>> monthlyDemand = ctx.getProductDelegate().getMonthlyDemandByProductID(pid);
//
//        //extract demand from the payload to feed to ES model
//        Double[] demands = new Double[monthlyDemand.size()];
//        String[] datetime = new String [monthlyDemand.size()];
//
//        //TODO Remove current month since data is incomplete
//        for (int i = 0; i < monthlyDemand.size(); i++) {
//            demands[i] = DbUtils.getDouble(monthlyDemand.get(i).get("monthly_demand"));
//            //TODO Rename date format column
//            datetime[i] = (String) monthlyDemand.get(i).get("date");
//        }
//        ExponentialSmoothingModel model = ctx.getExponentialSmoothingModel(demands, datetime);
//        model.fit();
//        LOGGER.info("Elapsed time: {}", (System.currentTimeMillis() - start));
        return null;
    }
}
