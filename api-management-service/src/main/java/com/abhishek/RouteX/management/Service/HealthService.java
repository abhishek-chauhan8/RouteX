package com.abhishek.RouteX.management.Service;

import com.abhishek.RouteX.management.Dto.responseDto.HealthResponse;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class HealthService {

    private final JdbcTemplate jdbcTemplate;

    public String getHealth(){
        String  databaseStatus = "DOWN";

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if(Integer.valueOf(1).equals(result)){
                databaseStatus = "UP";
            }
        }
        catch (Exception e){
            System.out.println("MySQL health check failed" + e.getMessage());
        }
        return  databaseStatus;
    }
}
