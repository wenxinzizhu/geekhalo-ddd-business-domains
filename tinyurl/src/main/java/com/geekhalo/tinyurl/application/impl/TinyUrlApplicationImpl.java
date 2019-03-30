package com.geekhalo.tinyurl.application.impl;

import com.geekhalo.tinyurl.application.TinyUrlApplication;
import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import com.geekhalo.tinyurl.repository.TargetUrlRepositoryRegistry;
import com.geekhalo.tinyurl.service.NumberEncoder;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.geekhalo.tinyurl.service.RequestEncoder;
import com.geekhalo.tinyurl.service.impl.DefaultRequestEncoder;
import com.geekhalo.tinyurl.service.impl.RadixBasedNumberEncoder;
import com.geekhalo.tinyurl.service.NumberEncoder;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.geekhalo.tinyurl.service.impl.RedisBasedBatchNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TinyUrlApplicationImpl implements TinyUrlApplication {
    private NumberEncoder numberEncoder = new RadixBasedNumberEncoder(35);

    @Autowired
    private RequestEncoder requestEncoder;

    @Autowired
    private RedisBasedBatchNumberGenerator numberGenerator;

    @Autowired
    private TargetUrlRepositoryRegistry targetUrlRepositoryRegistry;

    @Override
    public String shortUrl(String strategy, String url) {
        // 生成 Number Key
        Long key = getNumberGenerator(url)
                .nextNumber(NumberType.TINY_URL);
        // 构建并持久化 Target Url
        TargetUrl targetUrl = TargetUrl.builder()
                            .id(key)
                            .url(url)
                            .build();

        // 获取策略对于的 TargetUrlRepository
        TargetUrlRepository targetUrlRepository = this.targetUrlRepositoryRegistry.getRepositoryByStrategyName(strategy);
        targetUrlRepository.save(targetUrl);

        // 对 key 进行编码，获得更短的 code
        String code =  numberEncoder.encode(key);

        // 对策略和code 进行再次编码
        String request = requestEncoder.encode(strategy, code);
        // 与短链域名进行拼接，获得最终的短链接
        return getTinyUrlDomain() + "/" + request;
    }

    @Override
    public String getTargetUrl(String request) {
        RequestEncoder.StrategyAndCode strategyAndCode = this.requestEncoder.decode(request);

        // 获取策略对于的 TargetUrlRepository
        TargetUrlRepository targetUrlRepository = this.targetUrlRepositoryRegistry.getRepositoryByStrategyName(strategyAndCode.getStrategy());

        // 对 Code 进行解密，获得 Key
        Long key = numberEncoder.decode(strategyAndCode.getCode());


        // 从存储中获取 Target Url
        TargetUrl targetUrl = targetUrlRepository.getById(key);

        // 返回目标 URL 地址
        return targetUrl != null  ? targetUrl.getUrl() : null;
    }


    public NumberGenerator getNumberGenerator(String url){
        return numberGenerator;
    }


    public String getTinyUrlDomain(){
        return "geekhalo.com";
    }
}
