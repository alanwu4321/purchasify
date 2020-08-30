package com.dgs.v1.model;

import com.dgs.v1.service.Context;
import com.dgs.v1.service.ForecastService;
import com.dgs.v1.util.MathUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ExponentialSmoothingModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialSmoothingModel.class);

    //TODO String to purchaseMode
    private Double[] demands;
    private String[] dates;
    private Double[] adjustedDemands;
    private double[] oldForecasts;
    private double forecast;
    private double error;

    private double _forecast;
    private double _error;
    private double _alpha;
    //tracking signal
    private double _TS;
    private double _MAD;
    private double _SE;
    private final Context ctx;
    //record down each value by month
    private final SmoothedMonthCalc[] calc;

    public ExponentialSmoothingModel(Context ctx, Double[] demands, String[] dates) {

        //TODO Figure a better way to represent key
        this.ctx = ctx;
        this.demands = demands;
        this.dates = dates;
        this.adjustedDemands = Arrays.copyOf(demands, demands.length);
        this.oldForecasts = new double[12];
        calc = new SmoothedMonthCalc[demands.length];
    }

    /*
       validate input before proceeding
    */
    public Boolean isValid() {
        return demands.length > 0;
    }

    /*
       Init 1st values for:
       forecast (Mean of initWindow)
       Error (1st demand - 1st forecast)
       MAD (AvgDev of initWindow)
       The rest are default to zero
    */

    //init values including map
    public void initValues() {
        _forecast = MathUtils.Mean(adjustedDemands, ctx.getInitWindow());
        _error = adjustedDemands[0] - _forecast;
        _MAD = MathUtils.meanAbsDevtion(adjustedDemands, ctx.getInitWindow());
        _SE = 0;
        _TS = 0;
        _alpha = 0;
        loadSmoothedMonth(0);
    }

    //init values including map
    public void initValues1() {
        // increment init by adding 1 only if the sum of demands is not zero
        if(MathUtils.Sum(demands) > 0) demands[0] += 1;
        _forecast = MathUtils.Mean(demands, ctx.getInitWindow());
        _error = demands[0] - _forecast;
        _MAD = MathUtils.meanAbsDevtion(demands, ctx.getInitWindow());
        oldForecasts[0] = _forecast;
        loadSmoothedMonth(0);
    }

    public ExponentialSmoothingModel fit1() {
        if (!isValid()) LOGGER.error("Error in param");
        initValues1();
        final int len = demands.length;
        for (int t = 1; t < len; t++) {
            _forecast = _forecast + _alpha * _error;
            _error = demands[t] - _forecast;
            _SE = _SE + ctx.getPhi() * (_error - _SE);
            _MAD = _MAD + ctx.getBeta() * (Math.abs(_error) - _MAD);
            _TS = _SE / _MAD;
            //tracking signal should be below the alpha threshold
            _alpha =  Math.min(Math.abs(_TS), ctx.getAlphaThreshold());
            oldForecasts[t] = _forecast;
            loadSmoothedMonth(t);
        }
        return this;
    }

    public ExponentialSmoothingModel adjust(){
        for (int t = 0; t < demands.length; t++) {
            //adjust actual demand if it exceeds old forecast multiplied by demandCoefficientThreshold
            adjustedDemands[t] = Math.min(oldForecasts[t] * ctx.getDemandCoefficientThreshold(), demands[t]);
        }
        return this;
    }

    public ExponentialSmoothingModel fit() {
        if (!isValid()) LOGGER.error("Error in param");
        initValues();

        for (int t = 1; t < adjustedDemands.length; t++) {
            _forecast = _forecast + _alpha * _error;
            _error = adjustedDemands[t] - _forecast;
            _SE = _SE + ctx.getPhi() * (_error - _SE);
            _MAD = _MAD + ctx.getBeta() * (Math.abs(_error) - _MAD);
            _TS = _SE / _MAD;
            //tracking signal should be below the alpha threshold
            _alpha =  Math.min(Math.abs(_TS), ctx.getAlphaThreshold());
            loadSmoothedMonth(t);
        }
        //forecast the next demand
        forecast = _forecast + _alpha * _error;
        error = _MAD;

        return this;
    }

    private void loadSmoothedMonth(int t) {
        calc[t] = new SmoothedMonthCalc(t);
    }

    public SmoothedMonthCalc[] getCalc() {
        return calc;
    }

    public void setForecast(double forecast) {
        this.forecast = forecast;
    }

    public void setError(double error) {
        this.error = error;
    }

    public double getForecast() {
        return forecast;
    }

    public double getError() {
        return error;
    }

    @JsonIgnore
    public Double[] getAdjustedDemands() {
        return adjustedDemands;
    }

    @JsonIgnore
    public Double[] getDemands() {
        return demands;
    }

    public class SmoothedMonthCalc {
        private final double demand;
        private final String datetime;
        private final double adjustedDemand;
        private final double forecast;
        private final double error;
        private final double alpha;
        //tracking signal
        private final double TS;
        private final double MAD;
        private final double SE;

        public SmoothedMonthCalc(int t) {
            this.demand = demands[t];
            this.datetime = dates[t];
            this.adjustedDemand = adjustedDemands[t];
            this.forecast = _forecast;
            this.error = _error;
            this.alpha = _alpha;
            this.TS = _TS;
            this.MAD = _MAD;
            this.SE = _SE;
        }

        public double getDemand() {
            return demand;
        }

        public String getDatetime() {
            return datetime;
        }

        public double getAdjustedDemand() {
            return adjustedDemand;
        }

        public double getForecast() {
            return forecast;
        }

        public double getError() {
            return error;
        }

        public double getAlpha() {
            return alpha;
        }

        public double getTS() {
            return TS;
        }

        public double getMAD() {
            return MAD;
        }

        public double getSE() {
            return SE;
        }
    }

}
