package com.doto.global.common;

import com.github.f4b6a3.tsid.TsidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

/** TSID 기반 PK를 발급하는 Aggregate Root 공통 상위 클래스 */
@Getter
@MappedSuperclass
public abstract class BaseTsidEntity extends BaseTimeEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = TsidCreator.getTsid().toLong();
        }
    }

}
