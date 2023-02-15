package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository
public class AlphaImpl implements Alpha{
    @Override
    public String select() {
        return "alpha";
    }
}
