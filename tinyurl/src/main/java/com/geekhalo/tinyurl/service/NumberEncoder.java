package com.geekhalo.tinyurl.service;

public interface NumberEncoder {
    /**
     * 对 Number 进行编码
     * @param id
     * @return
     */
    String encode(Long id);

    /**
     * 对 Number 进行解密
     * @param str
     * @return
     */
    Long decode(String str);
}
