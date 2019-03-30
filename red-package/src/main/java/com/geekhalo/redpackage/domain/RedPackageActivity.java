package com.geekhalo.redpackage.domain;

import com.geekhalo.redpackage.util.IDUtils;
import lombok.Data;

import java.util.function.Consumer;

@Data
public class RedPackageActivity {
    private String id;
    // 红包总金额
    private Integer totalAmount;
    // 红包总数量
    private Integer totalNumber;

    // 版本，用于控制并发
    private int version;

    // 红包s剩余金额
    private Integer surplusAmount;
    // 红包剩余数量
    private Integer surplusNumber;

    public RedPackageActivity(){
        setId(IDUtils.genId());
    }

    /**
     * 初始化 <br />
     * 最初时，剩余金额和剩余数量与总金额和总数量一致
     * @param totalAmount
     * @param totalNumber
     */
    public void init(Integer totalAmount, Integer totalNumber){
        if (totalAmount < totalNumber){
            throw new IllegalArgumentException();
        }
        setTotalAmount(totalAmount);
        setTotalNumber(totalNumber);
        // 最开始的时候，剩余金额和剩余数量与总金额和总数量一致
        setSurplusAmount(totalAmount);
        setSurplusNumber(totalNumber);
    }

    /**
     * 抽取所有红包
     * @param consumer
     */
    public void draw(Consumer<RedPackage> consumer){
        draw(getSurplusNumber(), consumer);
    }

    /**
     * 抽取size数量的红包
     * @param size
     * @param consumer
     */
    public void draw(int size, Consumer<RedPackage> consumer){
        for (int i=0;i<size;i++){
            consumer.accept(draw());
        }
    }

    /**
     * 抽取红包 <br />
     * 抽取成功后，需要更新剩余数量
     * @return
     */
    private RedPackage draw(){
        if (hasNext()) {
            RedPackage redPackage = new RedPackage();
            redPackage.setActivityId(getId());
            redPackage.setId(IDUtils.genId());
            redPackage.setAmount(nextAmount());
            redPackage.init();
            // 更新剩余数量
            updateSurplus(redPackage);
            return redPackage;
        }else {
            return null;
        }
    }

    /**
     * 更新剩余金额和剩余红包数
     * @param redPackage
     */
    private void updateSurplus(RedPackage redPackage) {
        setSurplusNumber(getSurplusNumber() - 1);
        setSurplusAmount(getSurplusAmount() - redPackage.getAmount());
    }

    /**
     * 计算红包金额 <br />
     * 计算红包金额不是系统的重点，在这使用平均分配法
     * @return
     */
    private int nextAmount() {
        if (surplusNumber == totalNumber -1){
            return totalAmount - surplusAmount;
        }else {
            return totalAmount / totalNumber;
        }
    }

    /**
     * 是否还有红包
     * @return
     */
    private boolean hasNext() {
        return surplusNumber > 0;
    }

    /**
     * 是否含有余额
     * @return
     */
    public boolean hasBalance() {
        return hasNext();
    }
}
