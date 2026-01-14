package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.PointPolicy;
import com.musinsa.pointsystem.infra.persistence.entity.PointPolicyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointPolicyMapper 테스트")
class PointPolicyMapperTest {

    private PointPolicyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PointPolicyMapper();
    }

    @Test
    @DisplayName("Entity → Domain 변환")
    void shouldConvertEntityToDomain() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        String policyKey = "MAX_BALANCE";
        Long policyValue = 10000000L;
        String description = "최대 보유 가능 포인트";

        PointPolicyEntity entity = createEntity(id, policyKey, policyValue, description);

        // when
        PointPolicy domain = mapper.toDomain(entity);

        // then
        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.policyKey()).isEqualTo(policyKey);
        assertThat(domain.policyValue()).isEqualTo(policyValue);
        assertThat(domain.description()).isEqualTo(description);
    }

    @Test
    @DisplayName("null description 처리")
    void shouldHandleNullDescription() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        PointPolicyEntity entity = createEntity(id, "TEST_KEY", 100L, null);

        // when
        PointPolicy domain = mapper.toDomain(entity);

        // then
        assertThat(domain.description()).isNull();
    }

    private PointPolicyEntity createEntity(UUID id, String policyKey, Long policyValue, String description) throws Exception {
        Constructor<PointPolicyEntity> constructor = PointPolicyEntity.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        PointPolicyEntity entity = constructor.newInstance();

        setField(entity, "id", id);
        setField(entity, "policyKey", policyKey);
        setField(entity, "policyValue", policyValue);
        setField(entity, "description", description);

        return entity;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            throw e;
        }
    }
}
