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

package org.apache.rocketmq.eventbridge.domain.service;

import org.apache.rocketmq.eventbridge.domain.model.bus.EventBusService;
import org.apache.rocketmq.eventbridge.domain.model.rule.EventRuleService;
import org.apache.rocketmq.eventbridge.domain.model.source.EventSourceService;
import org.apache.rocketmq.eventbridge.exception.EventBridgeException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.WARN)
public class EventBusDomainServiceTest {

    @InjectMocks
    private EventBusDomainService eventBusDomainService;
    @Mock
    private EventSourceService eventSourceService;
    @Mock
    private EventRuleService eventRuleService;
    @Mock
    private EventBusService eventBusService;

    @Test
    public void deleteEventBusCheckDependencies_exception1() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventRuleService.getEventRulesCount(any(), any())).thenReturn(1);
            eventBusDomainService.deleteEventBusCheckDependencies("123456", "demo");
        });
        assertTrue(exception.getMessage().contains("The rules of eventbus [demo] exist, please delete them before delete event bus."));
    }

    @Test
    public void deleteEventBusCheckDependencies_exception2() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventSourceService.getEventSourceCount(any(), any())).thenReturn(1);
            eventBusDomainService.deleteEventBusCheckDependencies("123456", "demo");
        });
        assertTrue(exception.getMessage().contains("The source of eventbus  [demo] exist, please delete them before delete event bus."));
    }

}