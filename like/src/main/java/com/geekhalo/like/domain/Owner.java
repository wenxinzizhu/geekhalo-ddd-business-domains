package com.geekhalo.like.domain;

import com.geekhalo.ddd.lite.domain.ValueObject;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

@Embeddable
@Data
public class Owner implements ValueObject {

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "owner_type", updatable = false, nullable = false)
    @Convert(converter = CodeBasedOwnerTypeConverter.class)
    private OwnerType type;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "owner_id", updatable = false, nullable = false)
    private Long id;

    /**
     * 创建类型为 User 的 Owner
     * @param id
     * @return
     */
    public static Owner applyUser(Long id){
        Preconditions.checkArgument(id != null);
        Owner owner = new Owner();
        owner.setType(OwnerType.USER);
        owner.setId(id);
        return owner;
    }
}
