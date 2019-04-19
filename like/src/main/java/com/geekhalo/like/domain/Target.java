package com.geekhalo.like.domain;

import com.geekhalo.ddd.lite.domain.ValueObject;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class Target implements ValueObject {
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "target_type", updatable = false, nullable = false)
    @Convert(converter = CodeBasedTargetTypeConverter.class)
    private TargetType type;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "target_id", updatable = false, nullable = false)
    private Long id;

    /**
     * 创建类型为 News 的 Target
     * @param newsId
     * @return
     */
    public static Target applyNews(Long newsId){
        Preconditions.checkArgument(newsId != null);
        Target target = new Target();
        target.setType(TargetType.NEWS);
        target.setId(newsId);
        return target;
    }
}
