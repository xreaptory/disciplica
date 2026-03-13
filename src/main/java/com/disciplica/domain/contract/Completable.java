package com.disciplica.domain.contract;

import com.disciplica.domain.model.Reward;
public interface Completable {
    boolean complete();
    Reward getReward();
}


