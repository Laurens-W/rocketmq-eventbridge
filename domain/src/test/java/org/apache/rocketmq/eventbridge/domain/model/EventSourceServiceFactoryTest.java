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
import org.apache.rocketmq.eventbridge.domain.model.source.EventSource;
import org.apache.rocketmq.eventbridge.domain.model.source.EventSourceService;
import org.apache.rocketmq.eventbridge.domain.repository.EventSourceRepository;
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
public class EventSourceServiceFactoryTest {

    @InjectMocks
    private EventSourceService eventSourceService;

    @Mock
    private EventSourceRepository eventSourceRepository;

    @Mock
    private EventBusService eventBusService;

    @BeforeEach
    public void before() {
        doNothing().when(eventBusService)
            .checkExist(any(), any());
    }

    @Test
    public void testCreateEventSource_exception1() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventSourceRepository.createEventSource(any(), any(), any(), any(), any(), any(), any(), any())).thenThrow(
                new DuplicateKeyException(""));
            eventSourceService.createEventSource("123456", "demo", "demo-source", "description", null, null);
        });
        assertTrue(exception.getMessage().contains("The event source [demo-source] of event bus [demo] already existed!"));
    }

    @Test
    public void testCreateEventSource_exception2() {
        Throwable exception = assertThrows(EventBridgeException.class, () ->
            eventSourceService.createEventSource("123456", "demo", "$demo-source", "description", null, null));
        assertTrue(exception.getMessage().contains("The event source name [$demo-source] is invalid!"));
    }

    @Test
    public void testGetEventSource_exception1() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventSourceRepository.getEventSource(any(), any(), any())).thenReturn(null);
            eventSourceService.getEventSource("123456", "demo", "demo-source");
        });
        assertTrue(exception.getMessage().contains("The event source [demo-source] of event bus [demo] not existed!"));
    }

    @Test
    public void testDeleteEventSource_exception1() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            when(eventSourceRepository.getEventSource(any(), any(), any())).thenReturn(null);
            eventSourceService.deleteEventSource("123456", "demo", "demo-source");
        });
        assertTrue(exception.getMessage().contains("The event source [demo-source] of event bus [demo] not existed!"));
    }

    @Test
    public void testListEventSources() {
        EventSource eventSource = EventSource.builder()
            .accountId("123456")
            .eventBusName("demo")
            .name("demo-source")
            .build();
        List<EventSource> eventSources = Lists.newArrayList(eventSource);
        when(eventSourceRepository.getEventSourceCount(any(), any())).thenReturn(1);
        when(eventSourceRepository.listEventSources(any(), any(), any(), anyInt())).thenReturn(eventSources);
        PaginationResult<List<EventSource>> paginationResult = eventSourceService.listEventSources("123456", "demo",
            "0", 10);
        Assertions.assertEquals(1, paginationResult.getTotal());
        Assertions.assertEquals(null, paginationResult.getNextToken());
        Assertions.assertEquals(1, paginationResult.getData()
            .size());
        Assertions.assertEquals("demo-source", paginationResult.getData()
            .get(0)
            .getName());
    }

}
