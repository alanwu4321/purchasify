package com.dgs.v1;


import org.springframework.stereotype.Component;

@Component
public class persist {
        private Integer cpds = 0;

        public Integer getConnection() {
            return cpds;
        }

    public Integer setConnection() {
        return cpds++;
    }

}
