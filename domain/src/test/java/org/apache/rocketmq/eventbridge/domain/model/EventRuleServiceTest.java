/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.eventbridge.domain.model;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.rocketmq.eventbridge.domain.model.bus.EventBusService;
import org.apache.rocketmq.eventbridge.domain.model.rule.EventRule;
import org.apache.rocketmq.eventbridge.domain.model.rule.EventRuleService;
import org.apache.rocketmq.eventbridge.domain.repository.EventRuleRepository;
import org.apache.rocketmq.eventbridge.exception.EventBridgeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.WARN)
public class EventRuleServiceTest {

    @InjectMocks
    private EventRuleService eventRuleService;

    @Mock
    private EventRuleRepository eventRuleRepository;

    @Mock
    private EventBusService eventBusService;

    @BeforeEach
    public void before() {
        doNothing().when(eventBusService)
            .checkExist(any(), any());
    }

    @Test
    public void testCreateEventRule_exception1() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventRuleRepository.createEventRule(any(), any(), any(), any(), any(), any())).thenThrow(
                new DuplicateKeyException(""));
            eventRuleService.createEventRule("123456", "demo", "demo-rule", "description", "{}");
        });
        assertTrue(exception.getMessage().contains("The event rule [demo-rule] of event bus [demo] already existed!"));
    }

    @Test
    public void testCreateEventRule_exception2() {
        Throwable exception = assertThrows(EventBridgeException.class, () ->
            eventRuleService.createEventRule("123456", "demo", "$demo-rule", "description", "{}"));
        assertTrue(exception.getMessage().contains("The event rule name [$demo-rule] is invalid!"));
    }

    @Test
    public void testGetEventRule_exception1() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventRuleRepository.getEventRule(any(), any(), any())).thenReturn(null);
            eventRuleService.getEventRule("123456", "demo", "demo-rule");
        });
        assertTrue(exception.getMessage().contains("The event rule [demo-rule] of event bus [demo] not existed!"));
    }

    @Test
    public void testDeleteEventRule_exception1() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventRuleRepository.getEventRule(any(), any(), any())).thenReturn(null);
            eventRuleService.deleteEventRule("123456", "demo", "demo-source");
        });
        assertTrue(exception.getMessage().contains("The event rule [demo-source] of event bus [demo] not existed!"));
    }

    @Test
    public void testListEventRules() {
        EventRule eventRule = EventRule.builder()
            .accountId("123456")
            .eventBusName("demo")
            .name("demo-rule")
            .build();
        List<EventRule> eventRules = Lists.newArrayList(eventRule);
        when(eventRuleRepository.getEventRulesCount(any(), any())).thenReturn(1);
        when(eventRuleRepository.listEventRules(any(), any(), any(), anyInt())).thenReturn(eventRules);
        PaginationResult<List<EventRule>> paginationResult = eventRuleService.listEventRules("123456", "demo", "0", 10);
        Assertions.assertEquals(1, paginationResult.getTotal());
        Assertions.assertEquals(null, paginationResult.getNextToken());
        Assertions.assertEquals(1, paginationResult.getData()
            .size());
        Assertions.assertEquals("demo-rule", paginationResult.getData()
            .get(0)
            .getName());
    }

}
