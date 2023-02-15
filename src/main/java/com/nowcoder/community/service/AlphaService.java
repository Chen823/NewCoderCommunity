package com.nowcoder.community.service;

import com.nowcoder.community.dao.Alpha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlphaService {
    @Autowired
    private Alpha alpha;

    public String find(){
        return alpha.select();
    }
}
