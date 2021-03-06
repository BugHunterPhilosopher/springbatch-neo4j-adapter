package com.github.valb3r.springbatch.adapters.neo4j.ogm.entity;

import com.github.valb3r.springbatch.adapters.neo4j.dao.CycleAvoidingMappingContext;
import com.github.valb3r.springbatch.adapters.neo4j.dao.converters.ExecutionContextConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import com.github.valb3r.springbatch.adapters.neo4j.dao.converters.ExitStatusConverter;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.valb3r.springbatch.adapters.neo4j.ogm.BatchRelationshipConst.PARENT;


@Getter
@Setter
@NodeEntity
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
public class Neo4jStepExecution {

    public static final Neo4jStepExecution.FromBatch MAP = Mappers.getMapper(Neo4jStepExecution.FromBatch.class);

    @Id
    @GeneratedValue
    private Long id;
    
    private String stepName;

    @Builder.Default
    private BatchStatus status = BatchStatus.STARTING;

    private int readCount;
    private int writeCount;
    private int commitCount;
    private int rollbackCount;
    private int readSkipCount;
    private int processSkipCount;
    private int writeSkipCount;

    @Builder.Default
    private Date startTime = new Date(System.currentTimeMillis());

    private Date endTime;
    private Date lastUpdated;

    @Builder.Default
    @Convert(ExitStatusConverter.class)
    private ExitStatus exitStatus = ExitStatus.EXECUTING;

    private boolean terminateOnly;
    private int filterCount;

    @Relationship(type = PARENT)
    private Neo4jJobExecution jobExecution;

    @Convert(ExecutionContextConverter.class)
    private Map<String, Object> executionContext;

    private Integer version;

    @Mapper
    public interface FromBatch {

        Neo4jStepExecution map(StepExecution source);

        StepExecution map(Neo4jStepExecution source, @MappingTarget StepExecution target);

        default StepExecution map(Neo4jStepExecution source) {
            if (null != source.getId()) {
                return map(
                    source,
                    new StepExecution(
                        source.getStepName(),
                        Neo4jJobExecution.MAP.map(source.getJobExecution(), new CycleAvoidingMappingContext()),
                        source.getId()
                    )
                );
            }

            return map(
                source,
                new StepExecution(
                    source.getStepName(),
                    Neo4jJobExecution.MAP.map(source.getJobExecution(), new CycleAvoidingMappingContext())
                )
            );
        }

        default StepExecution map(Neo4jStepExecution source, JobExecution execution) {
            if (null != source.getId()) {
                return map(
                    source,
                    new StepExecution(
                        source.getStepName(),
                        execution,
                        source.getId()
                    )
                );
            }

            return map(
                source,
                new StepExecution(
                    source.getStepName(),
                    execution
                )
            );
        }

        default Map<String, Object> map(ExecutionContext value) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<String, Object> me : value.entrySet()) {
                result.put(me.getKey(), me.getValue());
            }

            return result;
        }

        default ExecutionContext map(Map<String, Object> value) {
            return new ExecutionContext(value);
        }
    }
}
