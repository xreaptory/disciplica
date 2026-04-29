package model.domain.contract;

import model.domain.model.Reward;
public interface Completable {
    boolean complete();
    Reward getReward();
}


