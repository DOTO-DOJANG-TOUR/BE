package com.doto.global.common;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/** TSID 기반 PK를 발급하는 Aggregate Root 공통 상위 클래스 */
@Getter
@MappedSuperclass
public abstract class BaseTsidEntity extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "id")
    private Long id;

}
